package cn.booslink.llm.common.network.adapter;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cn.booslink.llm.common.model.CBMSemantic;
import cn.booslink.llm.common.utils.GsonProvider;

public class CBMSemanticAdapter implements JsonDeserializer<CBMSemantic> {
    @Override
    public CBMSemantic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String jsonStr = json.getAsString();
        if (TextUtils.isEmpty(jsonStr)) return null;
        return GsonProvider.instance().fromJson(jsonStr, CBMSemantic.class);
    }
}
