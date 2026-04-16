package cn.booslink.llm.common.storage;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class SpeechStorageImpl implements ISpeechStorage {

    //---------- Preference Name ----------
    private static final String PREFERENCE_NAME = "llm_speech";
    //---------- Preference Key ----------
    private static final String KEY_SHOW_LEAVE_CONFIRM = "show_leave_confirm";

    //---------- Preference Key ----------

    private final SharedPreferences mPrefs;
    private final SharedPreferences.Editor mEditor;

    @Inject
    public SpeechStorageImpl(@ApplicationContext Context context) {
        this.mPrefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        this.mEditor = mPrefs.edit();
    }

    @Override
    public boolean shouldShowLeaveConfirm(int type) {
        return mPrefs.getBoolean(KEY_SHOW_LEAVE_CONFIRM + type, true);
    }

    @Override
    public void setShowLeaveConfirm(int type, boolean show) {
        mEditor.putBoolean(KEY_SHOW_LEAVE_CONFIRM + type, show);
        mEditor.apply();
    }
}
