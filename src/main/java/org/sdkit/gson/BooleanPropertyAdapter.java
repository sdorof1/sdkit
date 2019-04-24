package org.sdkit.gson;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BooleanPropertyAdapter implements JsonSerializer<BooleanProperty>, JsonDeserializer<BooleanProperty> {
  @Override
  public JsonElement serialize(BooleanProperty property, Type type,
      JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(property.getValue());
  }

  @Override
  public BooleanProperty deserialize(JsonElement json, Type type,
      JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    return new SimpleBooleanProperty(json.getAsJsonPrimitive().getAsBoolean());
  }
}
