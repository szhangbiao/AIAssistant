package cn.booslink.llm.common.cache.convert;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import cn.booslink.llm.common.model.AppSummary;
import cn.booslink.llm.common.utils.GsonProvider;

public class AppSummaryListConverter implements CacheConverter<List<AppSummary>> {

    private final Gson gson = GsonProvider.instance();

    @Override
    public List<AppSummary> deserialize(@NonNull String rawString) {
        try {
            return gson.fromJson(rawString, new TypeToken<ArrayList<AppSummary>>() {
            }.getType());
        } catch (JsonParseException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public String serialize(@NonNull List<AppSummary> categoryMenus) {
        return gson.toJson(categoryMenus);
    }
}
