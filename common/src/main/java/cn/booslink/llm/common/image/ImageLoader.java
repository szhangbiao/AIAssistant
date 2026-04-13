package cn.booslink.llm.common.image;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.request.RequestOptions;

import cn.booslink.llm.common.image.listener.OnImageLoadListener;

public interface ImageLoader {

    void loadImage(@NonNull ImageView imageView, @NonNull String url);

    void loadImageWithOption(@NonNull ImageView imageView, @NonNull String url, RequestOptions options);

    void loadImageWithOption(@NonNull ImageView imageView, @NonNull String url, RequestOptions options, OnImageLoadListener loadListener);


    void preloadImage(@NonNull String url);

    void onTrimMemory(Context context, int level);

    void clearMemoryCache(Context context);

    void clearDiskCache(Context context);
}
