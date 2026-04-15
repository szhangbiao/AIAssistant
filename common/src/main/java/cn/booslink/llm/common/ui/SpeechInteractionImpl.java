package cn.booslink.llm.common.ui;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceQuery;
import cn.booslink.llm.common.model.Weather;
import cn.booslink.llm.common.model.enums.Category;
import cn.booslink.llm.common.model.enums.EmoteState;
import cn.booslink.llm.common.model.enums.QueryState;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.WeatherExtKt;
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
    private final MutableLiveData<VoiceQuery> mVoiceInputLiveData;
    private final MutableLiveData<String> mNplResponseLiveData;
    private final MutableLiveData<ApkDownload> mApkDownloadLiveData;
    private final MutableLiveData<UIResponse> mUIResponseLiveData;

    private boolean isAttached = false;
    private boolean isActive = false;

    @Inject
    public SpeechInteractionImpl(@ApplicationContext Context context) {
        this.mContext = context;
        this.mParentView = new FrameLayout(context);
        this.mEmoteStateLiveData = new MutableLiveData<>(EmoteState.IDLE);
        this.mVoiceInputLiveData = new MutableLiveData<>(VoiceQuery.Companion.startup());
        this.mNplResponseLiveData = new MutableLiveData<>("");
        this.mApkDownloadLiveData = new MutableLiveData<>(ApkDownload.empty());
        this.mUIResponseLiveData = new MutableLiveData<>(UIResponse.Companion.empty());
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
            // 允许触摸事件，但不获取焦点，不影响下方应用操作
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // 如果需要完全透明且不拦截触摸，可以使用下面的配置
            // params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.gravity = Gravity.TOP | Gravity.END;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
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
            // 允许触摸事件，但不获取焦点，不影响下方应用操作
            // params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            // 如果需要完全透明且不拦截触摸，可以使用下面的配置
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.token = activity.getWindow().getDecorView().getWindowToken();
            params.gravity = Gravity.TOP | Gravity.END;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
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
    public void updateQuery(VoiceQuery query) {
        mVoiceInputLiveData.postValue(query);
        QueryState state = query.getState();
        Timber.tag(TAG).d("updateQuery, state = %s", state);
        switch (state) {
            case IDLE:
                mEmoteStateLiveData.postValue(EmoteState.IDLE);
                break;
            case QUERYING:
                mEmoteStateLiveData.postValue(EmoteState.THINKING);
                break;
            case DOWNLOADING:
                mEmoteStateLiveData.postValue(EmoteState.NORMAL);
                break;
            case DONE:
                UIResponse response = mUIResponseLiveData.getValue();
                if (response != null && !response.isEmpty()) return;
                mEmoteStateLiveData.postValue(EmoteState.LAUGHING);
                break;
            case FAILED:
            case EMPTY:
            case ERROR:
                mEmoteStateLiveData.postValue(EmoteState.CRYING);
            case WAKE_UP:
            default:
                mEmoteStateLiveData.postValue(EmoteState.NORMAL);
                break;
        }
    }

    @Override
    public void nlpAnswer(String nlpReply) {
        if (TextUtils.isEmpty(nlpReply)) return;
        UIResponse response = mUIResponseLiveData.getValue();
        if (response != null && !response.isEmpty()) return;
        mNplResponseLiveData.postValue(nlpReply);
    }

    @Override
    public void semanticAnswer(UIResponse response) {
        mUIResponseLiveData.postValue(response);
        if (response.getCategory() == Category.WEATHER) {
            if (response.getWeathers() == null || response.getWeathers().isEmpty()) return;
            Weather weather = response.getWeathers().get(0);
            mEmoteStateLiveData.postValue(WeatherExtKt.getEmoteState(weather));
        }
    }

    @Override
    public void downloadUpdate(ApkDownload download) {
        if (download == null) return;
        mApkDownloadLiveData.postValue(download);
    }

    @Override
    public void UIWakeup() {
        if (!isAttached || isActive) return;
        AIRootLayout rootLayout = mRootLayoutRef.get();
        if (rootLayout != null) {
            rootLayout.startWakeupAnimation();
        }
        mParentView.setVisibility(View.VISIBLE);
        isActive = true;
    }

    @Override
    public void UISleep() {
        if (!isAttached || !isActive) return;
        AIRootLayout rootLayout = mRootLayoutRef.get();
        if (rootLayout != null) {
            rootLayout.startHideAnimation(() -> mParentView.setVisibility(View.GONE));
        }
        isActive = false;
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
        mParentView.setBackgroundResource(R.drawable.bg_full_screen);
        mParentView.setVisibility(View.GONE);
        if (rootLayout != null) {
            bindData(rootLayout);
            mParentView.addView(rootLayout, childParams);
        }
    }

    private void bindData(AIRootLayout rootLayout) {
        rootLayout.observeData(mEmoteStateLiveData, mVoiceInputLiveData, mNplResponseLiveData, mApkDownloadLiveData, mUIResponseLiveData);
    }

    private void unBindData(AIRootLayout rootLayout) {
        if (rootLayout == null) return;
        rootLayout.unObserveData(mEmoteStateLiveData, mVoiceInputLiveData, mNplResponseLiveData, mApkDownloadLiveData, mUIResponseLiveData);
    }
}
