package org.sdkit.jfx.util;

import javafx.collections.ListChangeListener.Change;
import javafx.event.EventHandler;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;

public class ToggleUtil {

  private ToggleUtil() {}

  public static EventHandler<MouseEvent> consumeMouseEventfilter = (MouseEvent mouseEvent) -> {
    if (((Toggle) mouseEvent.getSource()).isSelected()) {
      mouseEvent.consume();
    }
  };

  public static void addAlwaysOneSelectedSupport(final ToggleGroup toggleGroup) {
    toggleGroup.getToggles().addListener((Change<? extends Toggle> c) -> {
      while (c.next()) {
        for (final Toggle addedToggle : c.getAddedSubList()) {
          addConsumeMouseEventfilter(addedToggle);
        }
      }
    });
    toggleGroup.getToggles().forEach(t -> {
      addConsumeMouseEventfilter(t);
    });
  }

  private static void addConsumeMouseEventfilter(Toggle toggle) {
    ((ToggleButton) toggle).addEventFilter(MouseEvent.MOUSE_PRESSED, consumeMouseEventfilter);
    ((ToggleButton) toggle).addEventFilter(MouseEvent.MOUSE_RELEASED, consumeMouseEventfilter);
    ((ToggleButton) toggle).addEventFilter(MouseEvent.MOUSE_CLICKED, consumeMouseEventfilter);
  }

}
