package cn.booslink.llm.common.utils;

import android.app.Application;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

public class ScreenAdapter {

    public static final int RADIO_16_9 = 0;
    public static final int RADIO_16_10 = 1;
    public static final int RADIO_4_3 = 2;

    private static final String TAG = "ScreenAdapter";

    private static float sNonCompatDensity;
    private static float curDensity = 1.0F;
    private static float hwRadio = 0.0F;

    public static Resources adapt(Application application, Resources superResources) {
        if (hwRadio == 0.0F) {
            DisplayMetrics metrics = superResources.getDisplayMetrics();
            int height = Math.min(metrics.heightPixels, metrics.widthPixels);
            int width = Math.max(metrics.heightPixels, metrics.widthPixels);
            hwRadio = (float) height / (float) width;
        }
        int adaptRatio = getAdaptRatio();
        if (RADIO_4_3 == adaptRatio) {
            return adaptWidth(application, superResources, 1024.0F);
        } else {
            return RADIO_16_10 == adaptRatio ? adaptHeight(application, superResources, 800.0F) : adaptHeight(application, superResources, 720.0F);
        }
    }

    public static int getAdaptRatio() {
        if (hwRadio == 0.0F) {
            throw new IllegalStateException("hwRadio not init, getResources must be called at least once before call getAdaptRatio");
        } else if (hwRadio < 0.57F) {
            return RADIO_16_9;
        } else {
            return hwRadio < 0.72F ? RADIO_16_10 : RADIO_4_3;
        }
    }

    public static Resources adaptWidth(Application application, Resources superResources, float width) {
        try {
            DisplayMetrics appDisplayMetrics = ScreenUtils.getWindowMetrics(application.getApplicationContext());
            DisplayMetrics superDisplayMetrics = superResources.getDisplayMetrics();
            int w = Math.max(appDisplayMetrics.heightPixels, appDisplayMetrics.widthPixels);
            float targetDensity = (float) w / width;
            if (sNonCompatDensity == 0.0F) {
                sNonCompatDensity = appDisplayMetrics.density;
                Log.d(TAG, "adaptWidth>>targetDensity=" + targetDensity + ", original density = " + appDisplayMetrics.density);
            }
            adaptResources(appDisplayMetrics, superDisplayMetrics, targetDensity, superResources);
        } catch (Exception e) {
            Log.e(TAG, "adaptWidth exception", e);
        }

        return superResources;
    }

    public static Resources adaptHeight(Application application, Resources superResources, float height) {
        try {
            try {
                DisplayMetrics appDisplayMetrics = ScreenUtils.getWindowMetrics(application.getApplicationContext());
                DisplayMetrics superDisplayMetrics = superResources.getDisplayMetrics();
                float h = (float) Math.min(appDisplayMetrics.heightPixels, appDisplayMetrics.widthPixels);
                Log.d(TAG, "adaptHeight>>screen height = " + h);
                float targetDensity = h / height;
                if (sNonCompatDensity == 0.0F) {
                    sNonCompatDensity = appDisplayMetrics.density;
                    Log.d(TAG, "adaptHeight>>targetDensity= " + targetDensity + ", super height is " + h + ", original density = " + sNonCompatDensity);
                }
                adaptResources(appDisplayMetrics, superDisplayMetrics, targetDensity, superResources);
            } catch (Exception e) {
                Log.e(TAG, "adaptHeight exception", e);
            }
            return superResources;
        } finally {
            ;
        }
    }

    public static Resources adaptClose(Application application, Resources superResources, boolean isApplication) {
        try {
            try {
                Resources appResources;
                if (isApplication) {
                    appResources = superResources;
                } else {
                    appResources = application.getResources();
                }

                DisplayMetrics appDisplayMetrics = appResources.getDisplayMetrics();
                DisplayMetrics superDisplayMetrics = superResources.getDisplayMetrics();
                if (sNonCompatDensity == 0.0F) {
                    return superResources;
                }

                adaptResources(appDisplayMetrics, superDisplayMetrics, sNonCompatDensity, superResources);
            } catch (Exception e) {
                Log.d(TAG, "adaptWidth exception", e);
            }
            return superResources;
        } finally {
            ;
        }
    }

    public static Resources adaptClose(Application application, Resources superResources) {
        return adaptClose(application, superResources, false);
    }

    private static void adaptResources(DisplayMetrics appDisplayMetrics, DisplayMetrics superDisplayMetrics, float targetDensity, Resources superResource) {
        //Log.d(TAG, "adaptResources>>targetDensity = " + targetDensity);
        float originalScaledDensity = appDisplayMetrics.scaledDensity / appDisplayMetrics.density;
        int targetDensityDpi = (int) (160.0F * targetDensity);
        // Application 下的 display metrics
        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetDensity * originalScaledDensity;

        appDisplayMetrics.densityDpi = targetDensityDpi;
        // Activity 下的 display metrics
        superDisplayMetrics.density = targetDensity;
        superDisplayMetrics.scaledDensity = targetDensity * originalScaledDensity;

        superDisplayMetrics.densityDpi = targetDensityDpi;
        superResource.getConfiguration().densityDpi = targetDensityDpi;
        curDensity = targetDensity;
    }

    public static float getCurDensity() {
        return curDensity;
    }
}
