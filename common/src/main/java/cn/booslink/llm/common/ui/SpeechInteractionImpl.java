package cn.booslink.llm.common.ui;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.enums.EmoteState;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.widget.AIRootLayout;
import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

public class SpeechInteractionImpl implements ISpeechInteraction {

    private static final String TAG = "SpeechInteraction";

    @Inject
    Lazy<AIRootLayout> mRootLayoutRef;

    private final Context mContext;
    private final FrameLayout mParentView;
    private final MutableLiveData<EmoteState> mEmoteStateLiveData;
    private final MutableLiveData<String> mVoiceInputLiveData;
    private final MutableLiveData<String> mNplResponseLiveData;
    private final MutableLiveData<ApkDownload> mApkDownloadLiveData;

    private boolean isAttached = false;

    @Inject
    public SpeechInteractionImpl(@ApplicationContext Context context) {
        this.mContext = context;
        this.mParentView = new FrameLayout(context);
        this.mEmoteStateLiveData = new MutableLiveData<>(EmoteState.IDLE);
        this.mVoiceInputLiveData = new MutableLiveData<>("您好，我是Bobo！");
        this.mNplResponseLiveData = new MutableLiveData<>("");
        this.mApkDownloadLiveData = new MutableLiveData<>(ApkDownload.empty());
    }

    @Override
    public void attachToWindow() {
        if (isViewAttached()) {
            Timber.tag(TAG).d("View already attached to window");
            return;
        }
        try {
            WindowManager wm = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            //params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.gravity = Gravity.TOP | Gravity.END;
            int width = ContextUtils.dp2px(mContext, 554);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = width;
            params.format = PixelFormat.RGBA_8888;
            setupRootViewParams();
            wm.addView(mParentView, params);
            Timber.tag(TAG).d("View attached to window");
            isAttached = true;
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "add view to window manager failed!");
        }
    }

    @Override
    public void detachFromWindow() {
        if (!isViewAttached()) {
            Timber.tag(TAG).d("View not attached to window");
            return;
        }
        unBindData(mRootLayoutRef.get());
        try {
            WindowManager wm = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
            wm.removeView(mParentView);
            Timber.tag(TAG).d("View detached from window");
            isAttached = false;
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "remove view from window manager failed!");
            // 确保失败时状态正确
            isAttached = false;
        }
    }

    @Override
    public void attachToActivity(Activity activity) {
        if (isViewAttached()) {
            Timber.tag(TAG).d("View already attached to window");
            return;
        }
        try {
            WindowManager wm = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            //params.z = 1000;
            params.token = activity.getWindow().getDecorView().getWindowToken();
            params.gravity = Gravity.TOP | Gravity.END;
            int width = ContextUtils.dp2px(mContext, 554);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = width;
            params.format = PixelFormat.RGBA_8888;
            setupRootViewParams();
            wm.addView(mParentView, params);
            isAttached = true;
            Timber.tag(TAG).d("View attached to activity");
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Add view to activity window failed!");
            // 确保失败时状态正确
            isAttached = false;
        }
    }

    @Override
    public void detachFromActivity(Activity activity) {
        if (!isViewAttached()) {
            Timber.tag(TAG).d("View not attached to window");
            return;
        }
        unBindData(mRootLayoutRef.get());
        try {
            WindowManager wm = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
            wm.removeView(mParentView);
            Timber.tag(TAG).d("View detached from activity");
            isAttached = false;
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "remove view from activity window failed!");
        }
    }

    @Override
    public void destroyView() {
        unBindData(mRootLayoutRef.get());
        mParentView.removeAllViews();
        if (mParentView.getParent() != null) {
            ((ViewManager) mParentView.getParent()).removeView(mParentView);
        }
        isAttached = false;
    }

    @Override
    public void updateQuery(@Nullable String voiceQuery) {
        if (TextUtils.isEmpty(voiceQuery)) return;
        mVoiceInputLiveData.postValue(voiceQuery);
    }

    @Override
    public void nlpAnswer(String nlpReply) {
        if (TextUtils.isEmpty(nlpReply)) return;
        mNplResponseLiveData.postValue(nlpReply);
    }

    @Override
    public void semanticAnswer(String category, Object answer) {

    }

    @Override
    public void downloadUpdate(ApkDownload download) {
        if (download == null) return;
        mApkDownloadLiveData.postValue(download);
    }

    /**
     * 检查View是否已添加到WindowManager
     */
    private boolean isViewAttached() {
        // 方法1：使用状态标记（最可靠）
        if (isAttached) {
            return true;
        }
        // 方法2：检查View的parent
        if (mParentView.getParent() != null) {
            isAttached = true;
            return true;
        }
        return false;
    }

    private void setupRootViewParams() {
        int width = ContextUtils.dp2px(mContext, 554);
        mParentView.removeAllViews();
        FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT);
        childParams.gravity = Gravity.TOP | Gravity.END;
        childParams.topMargin = ContextUtils.dp2px(mContext, 32);
        AIRootLayout rootLayout = mRootLayoutRef.get();
        //mParentView.setBackgroundColor(Color.BLUE);
        if (rootLayout != null) {
            bindData(rootLayout);
            mParentView.addView(rootLayout, childParams);
        }
    }

    private void bindData(AIRootLayout rootLayout) {
        rootLayout.observeData(mEmoteStateLiveData, mVoiceInputLiveData, mNplResponseLiveData, mApkDownloadLiveData);
    }

    private void unBindData(AIRootLayout rootLayout) {
        if (rootLayout == null) return;
        rootLayout.unObserveData(mEmoteStateLiveData, mVoiceInputLiveData, mNplResponseLiveData, mApkDownloadLiveData);
    }
}
