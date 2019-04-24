package org.sdkit.gson;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class ListStringPropertyAdapter implements JsonDeserializer<ListProperty<String>> {

  @Override
  public ListProperty<String> deserialize(JsonElement json, Type type,
      JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    Gson gson = new Gson();
    String[] arr = gson.fromJson(json.getAsJsonArray().toString(), String[].class);
    return new SimpleListProperty<>(FXCollections.observableArrayList(arr));
  }
}
