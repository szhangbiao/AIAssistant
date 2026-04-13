package cn.booslink.llm.common.image;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import javax.inject.Inject;

import cn.booslink.llm.common.image.listener.NormalRequestListener;
import cn.booslink.llm.common.image.listener.OnImageLoadListener;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class ImageLoaderImpl implements ImageLoader {

    private final Context mContext;

    @Inject
    public ImageLoaderImpl(@ApplicationContext Context context) {
        this.mContext = context;
    }


    @Override
    public void loadImage(@NonNull ImageView imageView, @NonNull String url) {
        if (imageView.getWindowToken() == null) return;
        Glide.with(mContext)
                .load(url)
                .into(imageView);
    }

    @Override
    public void loadImageWithOption(@NonNull ImageView imageView, @NonNull String url, RequestOptions options) {
        if (imageView.getWindowToken() == null) return;
        RequestOptions newOptions = GlideOptionsConfig
                .defaultOptions()
                .apply(options);
        Glide.with(mContext)
                .load(url)
                .apply(newOptions)
                .into(imageView);
    }

    @Override
    public void loadImageWithOption(@NonNull ImageView imageView, @NonNull String url, RequestOptions options, OnImageLoadListener loadListener) {
        if (imageView.getWindowToken() == null) return;
        RequestOptions newOptions = GlideOptionsConfig
                .defaultOptions()
                .apply(options);
        Glide.with(mContext)
                .load(url)
                .apply(newOptions)
                .listener(new NormalRequestListener(loadListener))
                .into(imageView);
    }

    @Override
    public void preloadImage(@NonNull String url) {
        if (mContext == null) return;
        Glide.with(mContext)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .preload();
    }

    @Override
    public void onTrimMemory(Context context, int level) {
        if (context == null) return;
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(context).clearMemory();
        }
        Glide.get(context).onTrimMemory(level);
    }

    @Override
    public void clearMemoryCache(Context context) {
        if (context == null) return;
        Glide.get(context).clearMemory();
    }

    @Override
    public void clearDiskCache(Context context) {
        if (context == null) return;
        Glide.get(context).clearDiskCache();
    }
}
