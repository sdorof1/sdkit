package org.sdkit.util;

import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides text resources of an application from a properties file.
 *
 */
public class AppResources {
  private static final Logger logger = LogManager.getLogger();

  private ResourceBundle resourceBundle;

  /**
   * Creates the AppResources by loading the specified ResourceBundle.
   *
   * @param resourceBundleName name of the ResourceBundle
   */
  public AppResources(final String resourceBundleName) {
    this.resourceBundle = ResourceBundle.getBundle(resourceBundleName);
  }

  /**
   * Returns the loaded ResourceBundle.
   *
   * @return ResourceBundle
   */
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  /**
   * Returns the String resource for the specified key.
   *
   * @param key resource key
   * @param arguments list of objects which needs to be inserted in the message
   *        text (optional)
   * @return String resource value
   */
  public String getString(final String key, final Object... arguments) {
    try {
      String stringValue = resourceBundle.getString(key);

      // replace placeholders in message with arguments if specified
      if (arguments != null && arguments.length > 0) {
        stringValue = String.format(stringValue, arguments);
      }
      return stringValue;
    } catch (Exception e) {
      logger.error("Failed to get string resource for key '" + key + "'!");
      return "???";
    }
  }
}
