package org.sdkit.gson;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class IntegerPropertyAdapter implements JsonSerializer<IntegerProperty>, JsonDeserializer<IntegerProperty> {
  @Override
  public JsonElement serialize(IntegerProperty property, Type type,
      JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(property.getValue());
  }

  @Override
  public IntegerProperty deserialize(JsonElement json, Type type,
      JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    return new SimpleIntegerProperty(json.getAsJsonPrimitive().getAsInt());
  }
}
