package cn.booslink.llm;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.multidex.MultiDex;

import com.bytedance.boost_multidex.BoostMultiDex;

public class AIApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            BoostMultiDex.install(base);
        } else {
            MultiDex.install(base);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
    }
}
