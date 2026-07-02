package cn.booslink.llm.common.utils;

import android.os.Build;
import android.os.Environment;

public class DeviceUtils {

    public static boolean unSupportInstallServiceRunWhenAppInBackgroundAboveAndroidQ() {
        String deviceName = Build.BRAND + " " + Build.MODEL;
        return deviceName.contains("A133");
    }

    public static boolean isAndroid11And3568Device() {
        String deviceName = Build.BRAND + " " + Build.MODEL;
        return deviceName.contains("3568") && Build.VERSION.SDK_INT == Build.VERSION_CODES.R;
    }
}
