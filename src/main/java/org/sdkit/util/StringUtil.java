package org.sdkit.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * This util class contains several String helper methods.
 *
 */
public final class StringUtil {

  private StringUtil() {}

  /**
   * Returns the trimmed version of the specified text. If the passed text is null
   * or if the trimmed text is empty, then null will be returned.
   *
   * @param text text to trim
   * @return trimmed text or null
   */
  public static String getTrimmedTextOrNull(String text) {
    if (text == null) {
      return null;
    }

    final String trimmed = text.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * Returns the passed text if it is not null. If it is null, then an empty
   * string is returned.
   *
   * @param text the text to check
   * @return the passed text or an empty string
   */
  public static String getTextOrEmptyString(String text) {
    return text == null ? "" : text;
  }

  /**
   * Returns the trimmed first line of the specified text or the complete text
   * when there is no line break.
   *
   * @param text the text to fit
   * @return the first line of text or null when text was null
   */
  public static String getFirstLineOfText(final String text) {
    if (text == null) {
      return null;
    } else {
      int indexNewLine = text.indexOf('\n');
      if (indexNewLine == -1) {
        return text.trim();
      } else {
        return text.substring(0, indexNewLine).trim();
      }
    }
  }

  /**
   * Returns true when the specified text is null or when the trimmed text is
   * empty.
   *
   * @param input text to check
   * @return when null or empty
   */
  public static boolean isNullOrEmpty(String input) {
    return input == null || input.trim().isEmpty();
  }

  public static String[] splitByNewLine(String text) {
    return text.split("\\r?\\n|\\r");
  }

  public static String escapeBackslash(String input) {
    return input.replaceAll("\\\\", "\\\\\\\\");
  }

  public static String separatorsToUnix(String input) {
    if (File.separatorChar == '\\') {
      return input.replace('\\', '/');
    }
    return input;
  }

  public static String quote(String input) {
    return String.format("'%s'", input);
  }

  public static String quote(Path path) {
    return quote(path.toString());
  }

  public static String qquote(String input) {
    return String.format("\"%s\"", input);
  }

  public static String qquote(Path path) {
    return qquote(path.toString());
  }

  public static String logArray(double[] arr, int left, int right) {

    if (arr == null) {
      return "null";
    }

    if (left + right > arr.length) {
      return arr.toString();
    }

    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < left; ++i) {
      builder.append(String.format("%s, ", arr[i]));
    }
    builder.append("... ");
    for (int i = right; i > 0; --i) {
      builder.append(String.format(", %s", arr[arr.length - i]));
    }
    builder.append("]");
    return builder.toString();
  }

  public static String logArray(double[] arr, int nb) {

    if (arr == null) {
      return "null";
    }

    if (nb > arr.length) {
      return Arrays.toString(arr);
    }

    if (nb == 2) {
      return String.format("[%s, %s...]", arr[0], arr[1]);
    }

    if (nb == 4) {
      return String.format("[%s, %s, %s, %s...]", arr[0], arr[1], arr[2], arr[3]);
    }

    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < nb; ++i) {
      builder.append(String.format("%s", arr[i]));
      if (i < nb - 1) builder.append(", ");
    }
    builder.append("...]");
    return builder.toString();
  }

}
