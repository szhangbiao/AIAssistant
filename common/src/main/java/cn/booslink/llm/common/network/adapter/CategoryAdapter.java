package cn.booslink.llm.common.network.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cn.booslink.llm.common.model.enums.Category;

public class CategoryAdapter implements JsonDeserializer<Category> {
    @Override
    public Category deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String categoryValue = json.getAsString();
        return Category.fromString(categoryValue);
    }
}
