package cn.booslink.llm.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonProvider {

    // 添加通用的Adapter与Network里的区分开
    private final Gson instance = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static Gson instance() {
        return SingletonHolder.INSTANCE.instance;
    }

    public static GsonBuilder builder() {
        return SingletonHolder.INSTANCE.instance.newBuilder();
    }

    private static class SingletonHolder {
        private static final GsonProvider INSTANCE = new GsonProvider();
    }
}
