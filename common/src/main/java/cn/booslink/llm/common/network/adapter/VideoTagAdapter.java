package cn.booslink.llm.common.network.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cn.booslink.llm.common.model.enums.VideoTag;

public class VideoTagAdapter implements JsonDeserializer<VideoTag> {
    @Override
    public VideoTag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String jsonStr = json.getAsString();
        return VideoTag.fromString(jsonStr);
    }
}
