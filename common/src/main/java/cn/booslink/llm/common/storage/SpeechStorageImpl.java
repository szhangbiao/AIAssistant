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
    private static final String KEY_LAST_UPDATE_CHECK_TIME = "last_update_check_time";
    private static final String KEY_AUTH_EXPERIENCED = "auth_experienced";
    private static final String KEY_AUTH_HOST = "auth_host";
    //---------- Preference Key ----------

    private final SharedPreferences mPrefs;
    private final SharedPreferences.Editor mEditor;

    @Inject
    public SpeechStorageImpl(@ApplicationContext Context context) {
        this.mPrefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        this.mEditor = mPrefs.edit();
    }

    @Override
    public boolean shouldShowLeaveConfirm() {
        return mPrefs.getBoolean(KEY_SHOW_LEAVE_CONFIRM, true);
    }

    @Override
    public void setShowLeaveConfirm(boolean show) {
        mEditor.putBoolean(KEY_SHOW_LEAVE_CONFIRM, show).apply();
    }

    @Override
    public long getLastUpdateCheckTime() {
        return mPrefs.getLong(KEY_LAST_UPDATE_CHECK_TIME, 0L);
    }

    @Override
    public void setLastUpdateCheckTime(long time) {
        mEditor.putLong(KEY_LAST_UPDATE_CHECK_TIME, time).apply();
    }

    @Override
    public boolean isAuthSuccess() {
        return mPrefs.getBoolean(KEY_AUTH_EXPERIENCED, false);
    }

    @Override
    public void setAuthSuccess(boolean success) {
        mEditor.putBoolean(KEY_AUTH_EXPERIENCED, success).apply();
    }

    @Override
    public String getAuthHost() {
        return mPrefs.getString(KEY_AUTH_HOST, "");
    }

    @Override
    public void setAuthHost(String host) {
        mEditor.putString(KEY_AUTH_HOST, host).apply();
    }
}
