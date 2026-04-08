package cn.booslink.llm.common.utils;

import android.os.Build;
import android.os.Environment;

public class DeviceUtils {
    public static boolean unSupportPackageInstallerUnInstallDevice() {
        String deviceName = Build.BRAND + " " + Build.MODEL;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || deviceName.contains("A133") || deviceName.contains("H713");
    }

    public static boolean unSupportInstallServiceRunWhenAppInBackgroundAboveAndroidQ() {
        String deviceName = Build.BRAND + " " + Build.MODEL;
        return deviceName.contains("A133");
    }

    public static boolean supportAndroid7_1ExternalStorage() {
        boolean isAndroid7_1 = Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1;
        if (!isAndroid7_1) return false;
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean supportGifUnFriendly() {
        String deviceName = Build.BRAND + " " + Build.MODEL;
        return deviceName.contains("3128H") || deviceName.contains("BSL_312X") || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean shouldUseRealSize() {
        return true;
    }

    public static boolean supportDeviceInsetsChange() {
        String deviceName = Build.BRAND + " " + Build.MODEL;
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 || deviceName.equalsIgnoreCase("alps TW_TY001");
    }
}
