package org.sdkit.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class UncaughtExceptionRule implements TestRule {

  @Override
  public Statement apply(Statement statement, Description description) {
    return new UncaughtExceptionStatement(statement);
  }

  private static class UncaughtExceptionStatement extends Statement {
    private final Statement statement;

    private Throwable rethrownException = null;

    public UncaughtExceptionStatement(Statement aStatement) {
      statement = aStatement;
    }

    @Override
    public void evaluate() throws Throwable {
      Thread.currentThread()
          .setUncaughtExceptionHandler((thread, exception) -> rethrownException = exception);
      statement.evaluate();

      // if an exception was thrown by the statement during evaluation,
      // then re-throw it to fail the test
      if (rethrownException != null) {
        throw rethrownException;
      }
    }

  }

}
