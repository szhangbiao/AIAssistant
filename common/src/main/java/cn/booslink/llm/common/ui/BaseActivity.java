package cn.booslink.llm.common.ui;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.booslink.llm.common.utils.ScreenAdapter;
import cn.booslink.llm.common.utils.ScreenUtils;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = ScreenUtils.fixFontScale(newBase);
        Context appContext = context.getApplicationContext();
        if (appContext instanceof Application) {
            ScreenAdapter.adapt((Application) appContext, context.getResources());
        }
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Context appContext = getApplicationContext();
        if (appContext instanceof Application) {
            ScreenAdapter.adapt((Application) appContext, getResources());
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ScreenUtils.setupFullScreen(getWindow());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
    }
}
