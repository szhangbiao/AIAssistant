package cn.booslink.llm.common.image;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import cn.booslink.llm.common.utils.Constants;
import cn.booslink.llm.common.utils.ContextUtils;

public class GlideOptionsConfig {

    private static final int DEFAULT_RADIUS = 8;

    public static void applyCommonOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // 内存缓存暂不设置，使用默认配置
        // 设置磁盘缓存目录跟缓存大小
        int diskCacheSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            diskCacheSize = Constants.DISK_HIGH_CACHE_SIZE;
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            diskCacheSize = Constants.DISK_MEDIUM_CACHE_SIZE;
        } else {
            diskCacheSize = Constants.DISK_LOW_CACHE_SIZE;
        }
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, Constants.GLIDE_DISK_CACHE_DIR, diskCacheSize));
        // 设置日志级别
        builder.setLogLevel(Log.ERROR);

        int radius = ContextUtils.dp2px(context, DEFAULT_RADIUS);
        RequestOptions defaultOptions = defaultOptions().transform(new RoundedCorners(radius));
        builder.setDefaultRequestOptions(defaultOptions);
    }

    public static RequestOptions defaultOptions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)
                    .disallowHardwareConfig() // 确保启用硬件加速
                    //.transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        } else {
            return new RequestOptions().format(DecodeFormat.PREFER_RGB_565)
                    .dontAnimate() // 禁用动画
                    .downsample(DownsampleStrategy.AT_LEAST) // 降低图片采样率
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform(); // 避免不必要的图像转换和预加载
        }
    }

    public static RequestOptions gifExtOptions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new RequestOptions()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .disallowHardwareConfig() // 确保启用硬件加速
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        } else {
            //不使用 format、dontAnimate 和 downsample：确保 GIF 动画能够正常解码和播放
            return new RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .disallowHardwareConfig() // 确保启用硬件加速
                    .skipMemoryCache(true)
                    .downsample(DownsampleStrategy.AT_LEAST)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform(); // 避免不必要的图像转换和预加载
        }
    }

    public static RequestOptions gifOptions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new RequestOptions()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .disallowHardwareConfig() // 确保启用硬件加速
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        } else {
            //不使用 format、dontAnimate 和 downsample：确保 GIF 动画能够正常解码和播放
            return new RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .disallowHardwareConfig() // 确保启用硬件加速
                    .skipMemoryCache(true)
                    .downsample(DownsampleStrategy.AT_LEAST)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform(); // 避免不必要的图像转换和预加载
        }
    }
}
