package org.sdkit.jfx.util;

import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

public class FxUtil {

  private FxUtil() {}

  public static String toRGBCode(Color color) {
    return String.format("#%02X%02X%02X",
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
  }

  public static String linearGradient(int startX, int startY, int endX, int endY, Stream<Stop> stops) {
    return String.format("linear-gradient(from %s%% %s%% to %s%% %s%%, %s)", startX, startY, endX, endY,
        stops.map(s -> String.format("%s %s%%", FxUtil.toRGBCode(s.getColor()), (int) (s.getOffset() * 100)))
            .collect(joining(", ")));
  }
}
