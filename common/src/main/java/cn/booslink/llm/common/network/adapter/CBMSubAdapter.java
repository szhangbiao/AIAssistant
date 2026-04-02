package cn.booslink.llm.common.network.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import cn.booslink.llm.common.model.enums.CBMSub;

public class CBMSubAdapter implements JsonDeserializer<CBMSub>, JsonSerializer<CBMSub> {

    @Override
    public CBMSub deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String subValue = json.getAsString();
        return CBMSub.fromString(subValue);
    }

    @Override
    public JsonElement serialize(CBMSub src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        return new JsonPrimitive(src.getSub());
    }
}
