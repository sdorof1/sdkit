package org.sdkit.gson;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class DoublePropertyAdapter implements JsonSerializer<DoubleProperty>, JsonDeserializer<DoubleProperty> {
  @Override
  public JsonElement serialize(DoubleProperty property, Type type,
      JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(property.getValue());
  }

  @Override
  public DoubleProperty deserialize(JsonElement json, Type type,
      JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    return new SimpleDoubleProperty(json.getAsJsonPrimitive().getAsDouble());
  }
}
