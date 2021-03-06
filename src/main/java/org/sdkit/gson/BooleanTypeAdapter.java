package org.sdkit.gson;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class BooleanTypeAdapter implements JsonDeserializer<Boolean> {

  @Override
  public Boolean deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
    return arg0.getAsBoolean();
  }
}
