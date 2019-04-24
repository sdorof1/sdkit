package org.sdkit.jfx.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

// FIXME move to util
public class DisposableListener<T> implements ChangeListener<T> {

  private final Runnable runnable;
  private final Consumer<T> consumer;
  private final List<T> values;

  @SafeVarargs
  private DisposableListener(Runnable action, Consumer<T> consumer, T... values) {
    this.runnable = action;
    this.consumer = consumer;
    this.values = Arrays.asList(values);
  }

  @SafeVarargs
  public DisposableListener(Runnable action, T... values) {
    this(action, null, values);
  }

  @SafeVarargs
  public DisposableListener(Consumer<T> action, T... values) {
    this(null, action, values);
  }

  @Override
  public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
    if (values.contains(newValue)) {
      observable.removeListener(this);
      if (runnable != null) {
        runnable.run();
      } else {
        consumer.accept(newValue);
      }
    }
  }
}
