package cn.booslink.llm.common.ui;

import android.app.Activity;

import javax.annotation.Nullable;

public interface ISpeechInteraction {

    void attachToWindow();

    void detachFromWindow();

    void attachToActivity(Activity activity);

    void detachFromActivity(Activity activity);
}
