package cn.booslink.llm.common.ui;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class ToastImpl implements IToast {

    private final Context mContext;

    private final MutableLiveData<String> mToastLiveData;

    private final Observer<String> mToastObserver = this::toastMessage;

    @Inject
    public ToastImpl(@ApplicationContext Context context) {
        this.mContext = context;
        this.mToastLiveData = new MutableLiveData<>();
        mToastLiveData.observeForever(mToastObserver);
    }

    @Override
    public void showMessage(String message) {
        if (TextUtils.isEmpty(message)) return;
        mToastLiveData.postValue(message);
    }

    @Override
    public void release() {
        mToastLiveData.removeObserver(mToastObserver);
    }

    private void toastMessage(String message) {
        if (TextUtils.isEmpty(message)) return;
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}
