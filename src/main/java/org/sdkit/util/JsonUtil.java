package org.sdkit.util;

import static java.util.stream.Collectors.toList;
import java.util.Arrays;
import java.util.List;

public class JsonUtil {

  private JsonUtil() {}

  public static List<String> keys(String json) {
    return Arrays.stream(json.split(","))
        .map(p -> p.replaceAll("[{} \"]", ""))
        .map(p -> p.split(":")[0])
        .collect(toList());
  }
}
