package cn.booslink.llm.common.utils;

import android.content.Context;
import android.os.Build;

public class DeviceUtils {

    public static boolean unSupportInstallServiceRunWhenAppInBackgroundAboveAndroidQ() {
        String deviceName = Build.BRAND + " " + Build.MODEL;
        return deviceName.contains("A133");
    }

    public static boolean isAndroid11And3568Device() {
        String deviceName = Build.BRAND + " " + Build.MODEL;
        return deviceName.contains("3568") && Build.VERSION.SDK_INT == Build.VERSION_CODES.R;
    }

    public static float adaptMarginTop(Context mContext) {
        int height = ScreenUtils.getScreenHeight(mContext);
        int width = ScreenUtils.getScreenWidth(mContext);
        float ratio = height / (width * 1.0f);
        if (ratio >= 0.625f) {
            return 32;
        } else if (ratio >= 0.6f) {
            return 18;
        } else {
            return 12;
        }
    }
}
