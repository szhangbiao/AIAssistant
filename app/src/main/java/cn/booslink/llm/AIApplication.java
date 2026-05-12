package cn.booslink.llm;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.multidex.MultiDex;

import cn.booslink.llm.common.model.DeviceInfo;
import cn.booslink.llm.common.utils.ScreenAdapter;
import cn.booslink.llm.common.utils.ScreenUtils;
import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

import com.bytedance.boost_multidex.BoostMultiDex;

import javax.inject.Inject;

@HiltAndroidApp
public class AIApplication extends Application {

    private static final String TAG = "AIApplication";
    @Inject
    DeviceInfo mDeviceInfo;

    @Override
    protected void attachBaseContext(Context base) {
        Context context = ScreenUtils.fixFontScale(base);
        super.attachBaseContext(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            BoostMultiDex.install(context);
        } else {
            MultiDex.install(context);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mDeviceInfo.isDevMode()) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.tag(TAG).d("onCreate");
    }

    @Override
    public Resources getResources() {
        return ScreenAdapter.adaptWidth(this, super.getResources(), 1280.0f);
    }
}
