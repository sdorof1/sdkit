package org.sdkit.jna;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sdkit.util.ObjectFactory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Ole32;

/**
 * This class represents a base service to provide a single thread apartment
 * (STA) model for thread safe access to the COM objects. It uses specified an
 * object's factory to create an underlying object on a background thread and
 * provides the methods to invoke the delegates on that thread.
 * 
 * @author Sergei Dorofeenko
 *
 * @param <T> the class of the object for which to provide STA
 */
public class COMService<T> {
  static private final Logger logger = LogManager.getLogger();

  protected interface ObjectRequest<T, R> {
    R query(T com);
  }

  protected interface VoidRequest<T> {
    void execute(T com);
  }

  protected enum Error {
    OK, UNINITIALIZED, BUSY, TIMEOUT, FAILED;
  }

//@formatter:off

  public interface COMObjectFactory<T> extends ObjectFactory<T> {
  }

  private final COMObjectFactory<T> objectFactory;

  private final SynchronousQueue<RequestHandler<T, ?>> qin = new SynchronousQueue<>();
  private final SynchronousQueue<RequestHandler<T, ?>> qout = new SynchronousQueue<>();

  private long requestCounter = 0;
  private long getNextRequestId() { return requestCounter++; }

  private AtomicReference<Exception> exception = new AtomicReference<Exception>(null);
  private void setException(Exception value) { logger.trace(value); exception.set(value); }
  public Exception getException() { return this.exception.get(); }
  
  private AtomicReference<Error> error = new AtomicReference<Error>(Error.UNINITIALIZED);
  private void setError(Error value) { if (getError() != Error.FAILED) { logger.trace(value); error.set(value); } else { throw new IllegalStateException("Reset FAILED state"); }}
  public Error getError() { return error.get(); }

  private long timeout = 60000; // ms
  public long getTimeout() { return timeout; }
  public void setTimeout(long value) { timeout = value; }

  private final ExecutorService executor;

//@formatter:on

  /**
   * Standard c'tor
   * 
   * @param objectFactory The factory of the underlying object. This object will
   *        be created on background thread.
   */
  protected COMService(COMObjectFactory<T> objectFactory) {
    this.objectFactory = objectFactory;
    this.executor = Executors.newSingleThreadExecutor();
  }

  protected void init() {
    if (executor.isShutdown()) {
      throw new IllegalStateException("Has been shut down");
    }
    logger.trace("Initialize");
    executor.execute(new Task());
  }

  protected void shutdown() {
    if (!executor.isShutdown()) {
      logger.trace("Shutdown executor");
      executor.shutdownNow();
    }
  }

  protected Boolean supplyBoolean(ObjectRequest<T, Boolean> request) {
    return supplyObject(request);
  }

  protected Integer supplyInt(ObjectRequest<T, Integer> request) {
    return supplyObject(request);
  }

  protected Long supplyLong(ObjectRequest<T, Long> request) {
    return supplyObject(request);
  }

  protected Double supplyDouble(ObjectRequest<T, Double> request) {
    return supplyObject(request);
  }

  protected String supplyString(ObjectRequest<T, String> request) {
    return supplyObject(request);
  }

  protected <R> R supplyObject(ObjectRequest<T, R> request) {
    return supplyObject(request, timeout);
  }

  protected <R> R supplyObject(ObjectRequest<T, R> request, long timeout) {
    return request(request, timeout);
  }

  protected void invokeNoReply(VoidRequest<T> request) {
    invokeNoReply(request, timeout);
  }

  protected void invokeNoReply(VoidRequest<T> request, long timeout) {
    request(request, timeout);
  }

  protected <R> R request(ObjectRequest<T, R> request, long timeout) {
    final ObjectRequestHandler<T, R> handler =
        new ObjectRequestHandler<>(getNextRequestId(), (ObjectRequest<T, R>) request);
    return request(handler, timeout);
  }

  @SuppressWarnings("unchecked")
  protected <R> R request(VoidRequest<T> request, long timeout) {
    final VoidRequestHandler<T> handler = new VoidRequestHandler<>(getNextRequestId(), request);
    return request((RequestHandler<T, R>) handler, timeout);
  }

  private final TerminatingRequest<T> TERMINATOR = new TerminatingRequest<>();

  protected <R> R request(RequestHandler<T, R> request, long timeout) {
    if (getError() == Error.FAILED) {
      // do not accept new requests if FAILED
      return null;
    }
    try {
      logger.trace("Submit {}", request);
      final boolean submitted = qin.offer(request, timeout, TimeUnit.MILLISECONDS);
      if (getError() == Error.FAILED) {
        throw new InterruptedException();
      }
      if (submitted) {
        logger.trace("Wait {}", request);
        final RequestHandler<T, ?> responded = qout.poll(timeout, TimeUnit.MILLISECONDS);
        if (responded == null) {
          setError(Error.TIMEOUT);
        } else if (responded == TERMINATOR) {
          throw new InterruptedException();
        } else if (responded != request) {
          throw new IllegalStateException("Request lost");
        } else {
          final R result = request.getResult().orElse(null);
          logger.trace("{} -> {}", request, result);
          setError(Error.OK);
          return result;
        }
      } else {
        setError(Error.BUSY);
      }
    } catch (InterruptedException e) {
      if (getError() == Error.FAILED) {
        // ignore interrupt from background task failed
      } else {
        Thread.currentThread().interrupt();
      }
    }
    logger.debug("{} -> {}", request, getError());
    return null;
  }


  private final class Task implements Runnable {

    @Override
    public void run() {
      CoInitialize();
      T com = objectFactory.create();
      logger.trace("Start request processing");
      try {
        requestProcessing(com);
      } catch (InterruptedException e) {
        //
      } catch (Exception e) {
        setException(e);
      } finally {
        CoUninitialize();
        setError(Error.FAILED);
        while (true) {
          try {
            while (qin.poll(5, TimeUnit.MILLISECONDS) != null) {
            }
            while (qout.offer(TERMINATOR, 5, TimeUnit.MILLISECONDS)) {
            }
            break;
          } catch (InterruptedException e) {
            //
          }
        }
        logger.trace("Finished request processing");
        if (getException() != null) {
          // shut down executor on exception
          shutdown();
        }
      }
    }

    private void requestProcessing(T com) throws InterruptedException {
      setError(Error.OK);
      while (true) {
        final RequestHandler<T, ?> handler = qin.take();
        logger.trace("Process {}", handler);
        handler.handle(com);
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        // will be false if timeout expired
        boolean responded = qout.offer(handler, 5, TimeUnit.MILLISECONDS);
        logger.trace("Responded {} -> {}", handler, responded);
      }
    }
  }

  protected void CoInitialize() {
    logger.debug("Initialize COM library for current thread");
    Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
  }

  protected void CoUninitialize() {
    logger.debug("Release COM resources the thread maintains");
    Ole32.INSTANCE.CoUninitialize();
  }

  private static abstract class RequestHandler<T, R> {
    private final long id;

    private Optional<R> result;

    public Optional<R> getResult() {
      return result;
    }

    protected void setResult(R value) {
      this.result = Optional.ofNullable(value);
    }

    public RequestHandler(long id) {
      this.id = id;
    }

    abstract public void handle(T com);

    @Override
    public String toString() {
      return String.format("Request [id=%s]", id);
    }

  }


  private static class ObjectRequestHandler<T, R> extends RequestHandler<T, R> {
    private final ObjectRequest<T, R> request;

    public ObjectRequestHandler(long id, ObjectRequest<T, R> request) {
      super(id);
      this.request = request;
    }

    @Override
    public void handle(T com) {
      R result = request.query(com);
      setResult(result);
    }
  }

  private static class VoidRequestHandler<T> extends RequestHandler<T, Void> {
    private final VoidRequest<T> request;

    public VoidRequestHandler(long id, VoidRequest<T> request) {
      super(id);
      this.request = request;
    }

    @Override
    public void handle(T com) {
      request.execute(com);
      setResult(null);
    }
  }

  private static class TerminatingRequest<T> extends RequestHandler<T, Void> {

    public TerminatingRequest() {
      super(-1);
    }

    @Override
    public void handle(T com) {
      // TODO Auto-generated method stub

    }
  }

}
