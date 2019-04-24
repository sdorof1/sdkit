package org.sdkit.junit;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class JavaFXThreadingRunner {
  private static JavaFXThreadingRunner INSTANCE;

  public static void runJavaFX(Runnable command) throws Throwable {
    if (Objects.isNull(INSTANCE)) {
      INSTANCE = new JavaFXThreadingRunner();
    }
    INSTANCE.execute(command);
  }

  /**
   * Flag for setting up the JavaFX, we only need to do this once for all tests.
   */
  private static boolean jfxIsSetup;

  private Throwable rethrownException = null;

  public void execute(Runnable command) throws Throwable {

    if (!jfxIsSetup) {
      setupJavaFX();

      jfxIsSetup = true;
    }

    final CountDownLatch countDownLatch = new CountDownLatch(1);

    Platform.runLater(() -> {
      try {
        command.run();
      } catch (Throwable e) {
        rethrownException = e;
      }
      countDownLatch.countDown();
    });

    countDownLatch.await();

    // if an exception was thrown by the statement during evaluation,
    // then re-throw it to fail the test
    if (rethrownException != null) {
      throw rethrownException;
    }
  }

  protected void setupJavaFX() throws InterruptedException {

    long timeMillis = System.currentTimeMillis();

    final CountDownLatch latch = new CountDownLatch(1);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // initializes JavaFX environment
        new JFXPanel();

        latch.countDown();
      }
    });

    System.out.println("javafx initialising...");
    latch.await();
    System.out
        .println("javafx is initialised in " + (System.currentTimeMillis() - timeMillis) + "ms");
  }

}
