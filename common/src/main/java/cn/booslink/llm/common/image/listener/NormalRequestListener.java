package cn.booslink.llm.common.image.listener;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;


public class NormalRequestListener implements RequestListener<Drawable> {

    private final OnImageLoadListener mImageLoadListener;

    public NormalRequestListener(OnImageLoadListener imageLoadListener) {
        this.mImageLoadListener = imageLoadListener;
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
        if (mImageLoadListener != null) {
            mImageLoadListener.onFailed();
        }
        return false;
    }

    @Override
    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
        if (mImageLoadListener != null) {
            mImageLoadListener.onSuccess();
        }
        return false;
    }
}
