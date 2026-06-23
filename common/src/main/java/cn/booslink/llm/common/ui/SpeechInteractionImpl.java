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
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.speech.ITTSSpeech;
import cn.booslink.llm.common.utils.ContextUtils;
import cn.booslink.llm.common.utils.WeatherExtKt;
import cn.booslink.llm.common.widget.AIRootLayout;
import cn.booslink.llm.common.widget.SpeechWindowLayout;
import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

public class SpeechInteractionImpl implements ISpeechInteraction {

    private static final String TAG = "SpeechInteraction";

    @Inject
    Lazy<AIRootLayout> mRootLayoutRef;
    @Inject
    Lazy<ISpeechAgent> mSpeechAgentLazy;

    private final Context mContext;
    private final ITTSSpeech mTTSSpeech;
    private final FrameLayout mParentView;
    private final MutableLiveData<String> mNplResponseLiveData;
    private final MutableLiveData<EmoteState> mEmoteStateLiveData;
    private final MutableLiveData<VoiceQuery> mVoiceInputLiveData;
    private final MutableLiveData<UIResponse> mUIResponseLiveData;
    private final MutableLiveData<ApkDownload> mApkDownloadLiveData;

    private boolean isAttached = false;
    private volatile boolean isActive = false;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;

    @Inject
    public SpeechInteractionImpl(@ApplicationContext Context context, ITTSSpeech ttsSpeech) {
        this.mContext = context;
        this.mTTSSpeech = ttsSpeech;
        this.mParentView = new SpeechWindowLayout(context);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                // 对于低于 Android 8.0 的系统，直接使用更稳定的 TYPE_SYSTEM_ALERT 级系统窗口，提高层级与渲染优先级
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            // 允许触摸事件传递到下方，同时监听外部触摸事件
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            // 在 Android 4.4 (KitKat) 及以下版本关闭硬件加速，防止在其它 App 启动时重叠区域产生渲染残影
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                params.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            }
            // 如果需要完全透明且不拦截触摸，可以使用下面的配置
            // params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.gravity = Gravity.TOP | Gravity.END;
            // 使用实际内容区域的大小，而不是全屏，这样不会阻挡下方的触摸事件
            int width = ContextUtils.dp2px(mContext, 554);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = width;
            params.format = PixelFormat.RGBA_8888;
            params.alpha = 0.99f; // 强制系统为悬浮窗启用透明混合通道，避免底色优化导致的旧图层残留
            setupRootViewParams();
            wm.addView(mParentView, params);
            mWindowManager = wm;
            mWindowParams = params;
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
            if (mWindowManager != null) {
                mWindowManager.removeView(mParentView);
            } else {
                WindowManager wm = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
                wm.removeView(mParentView);
            }
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
            // 允许触摸事件传递到下方，同时监听外部触摸事件
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            // 如果需要完全透明且不拦截触摸，可以使用下面的配置
            // params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.token = activity.getWindow().getDecorView().getWindowToken();
            params.gravity = Gravity.TOP | Gravity.END;
            // 使用实际内容区域的大小，而不是全屏，这样不会阻挡下方的触摸事件
            int width = ContextUtils.dp2px(mContext, 554);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = width;
            params.format = PixelFormat.RGBA_8888;
            params.alpha = 0.99f; // 强制系统为悬浮窗启用透明混合通道，避免底色优化导致的旧图层残留
            setupRootViewParams();
            wm.addView(mParentView, params);
            mWindowManager = wm;
            mWindowParams = params;
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
            if (mWindowManager != null) {
                mWindowManager.removeView(mParentView);
            } else {
                WindowManager wm = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
                wm.removeView(mParentView);
            }
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
        Timber.tag(TAG).d("updateQuery, text = %s, state = %s", query.getQuery(), state);
        switch (state) {
            case IDLE:
                mEmoteStateLiveData.postValue(EmoteState.IDLE);
                break;
            case QUERYING:
                mEmoteStateLiveData.postValue(EmoteState.THINKING);
                break;
            case DONE:
                mEmoteStateLiveData.postValue(EmoteState.LAUGHING);
                break;
            case FAILED:
            case EMPTY:
            case ERROR:
                mEmoteStateLiveData.postValue(EmoteState.CRYING);
                break;
            case WAKE_UP:
            case DOWNLOADING:
            default:
                mEmoteStateLiveData.postValue(EmoteState.NORMAL);
                break;
        }
    }

    @Override
    public void nlpAnswer(String nlpReply, boolean isStreamEnd) {
        if (TextUtils.isEmpty(nlpReply)) return;
        if (isStreamEnd) {
            mTTSSpeech.speak(nlpReply);
        }
        mNplResponseLiveData.postValue(nlpReply);
    }

    @Override
    public void customAnswer(String customReply) {
        if (TextUtils.isEmpty(customReply)) return;
        mTTSSpeech.speak(customReply);
        mNplResponseLiveData.postValue(customReply);
    }

    @Override
    public void semanticAnswer(UIResponse response) {
        Timber.tag(TAG).d("semanticAnswer, category = %s", response.getCategory());
        if (response.getCategory() == Category.WEATHER) {
            if (response.getWeathers() == null || response.getWeathers().isEmpty()) {
                Timber.tag(TAG).d("weather invalid");
                customAnswer("未找到相关内容");
                mEmoteStateLiveData.postValue(EmoteState.CRYING);
                return;
            }
            String ttsText = response.getWeatherTTSSpeechText();
            if (!TextUtils.isEmpty(ttsText)) {
                mTTSSpeech.speak(ttsText);
            }
            mUIResponseLiveData.postValue(response);
            Weather weather = response.getQueryDayWeather();
            if (weather != null) {
                mEmoteStateLiveData.postValue(WeatherExtKt.getEmoteState(weather));
            }
        } else if (response.getCategory() == Category.SLEEP) {
            mUIResponseLiveData.postValue(response);
            mEmoteStateLiveData.postValue(EmoteState.NORMAL);
        }
    }

    @Override
    public void downloadUpdate(ApkDownload download) {
        if (download == null) return;
        mApkDownloadLiveData.postValue(download);
        mEmoteStateLiveData.postValue(download.isDownloadComplete() ? EmoteState.LAUGHING : EmoteState.NORMAL);
    }

    @Override
    public void UIWakeup() {
        if (isActive) return;
        Timber.tag(TAG).d("UIWakeup");
        showAndAnimate();
        isActive = true;
    }

    @Override
    public void UISleep() {
        if (!isActive) return;
        Timber.tag(TAG).d("UISleep");
        AIRootLayout rootLayout = mRootLayoutRef.get();
        if (rootLayout != null) {
            rootLayout.startHideAnimation(this::hideWindow);
        } else {
            hideWindow();
        }
        isActive = false;
    }

    @Override
    public void authFailed(String failReason) {
        Timber.tag(TAG).d("authFailed");
        showAndAnimate();
        updateQuery(VoiceQuery.Companion.stateOnly(QueryState.FAILED));
        customAnswer(failReason);
    }

    @Override
    public void showWaitingAuth() {
        Timber.tag(TAG).d("showWaitingAuth");
        showAndAnimate();
        updateQuery(VoiceQuery.Companion.stateOnly(QueryState.IDLE));
        customAnswer("等待联网鉴权");
    }

    @Override
    public void forceWindowRefresh() {
        mParentView.post(() -> {
            if (isAttached && mWindowManager != null && mWindowParams != null) {
                try {
                    // 强制重绘根视图
                    mParentView.invalidate();
                    mParentView.requestLayout();

                    // 通过微调透明度（Alpha）而不是位置（X），强制 SurfaceFlinger 重新进行合成计算
                    // 透明度在 0.99 和 0.98 之间微调对人眼完全无感，但能完美触发图层刷新，避免 UI 产生物理抖动
                    final float originalAlpha = mWindowParams.alpha;
                    mWindowParams.alpha = originalAlpha - 0.01f;
                    mWindowManager.updateViewLayout(mParentView, mWindowParams);

                    // 在 App 启动转场的不同关键节点触发重新合成，确保底层被完全替换为新应用的画面
                    mParentView.postDelayed(() -> {
                        if (isAttached && mWindowManager != null && mWindowParams != null) {
                            mWindowParams.alpha = originalAlpha;
                            mWindowManager.updateViewLayout(mParentView, mWindowParams);
                        }
                    }, 150);

                    mParentView.postDelayed(() -> {
                        if (isAttached && mWindowManager != null && mWindowParams != null) {
                            mWindowParams.alpha = originalAlpha - 0.01f;
                            mWindowManager.updateViewLayout(mParentView, mWindowParams);
                        }
                    }, 350);

                    mParentView.postDelayed(() -> {
                        if (isAttached && mWindowManager != null && mWindowParams != null) {
                            mWindowParams.alpha = originalAlpha;
                            mWindowManager.updateViewLayout(mParentView, mWindowParams);
                        }
                    }, 600);
                } catch (Exception e) {
                    Timber.tag(TAG).e(e, "forceWindowRefresh failed");
                }
            }
        });
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
        // 在 Android 4.4 (KitKat) 及以下设备强制使用软件渲染层，彻底规避 GPU 双缓冲/三缓冲在转场时的残影 Bug
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mParentView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        int width = ContextUtils.dp2px(mContext, 554);
        mParentView.removeAllViews();
        FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT);
        childParams.gravity = Gravity.TOP | Gravity.END;
        childParams.topMargin = ContextUtils.dp2px(mContext, 32);
        AIRootLayout rootLayout = mRootLayoutRef.get();
        // mParentView.setBackgroundResource(R.drawable.bg_full_screen);
        ISpeechAgent speechAgent = mSpeechAgentLazy.get();
        if (speechAgent != null && speechAgent.isAIUIWorking()) {
            mParentView.setVisibility(View.VISIBLE);
        } else {
            mParentView.setVisibility(View.GONE);
        }
        if (rootLayout != null) {
            bindData(rootLayout);
            mParentView.addView(rootLayout, childParams);
            if (mParentView instanceof SpeechWindowLayout) {
                SpeechWindowLayout windowLayout = (SpeechWindowLayout) mParentView;
                windowLayout.clearTouchableViews();
                windowLayout.addTouchableView(rootLayout.findViewById(R.id.tv_npl));
                windowLayout.addTouchableView(rootLayout.findViewById(R.id.tv_positive));
                windowLayout.addTouchableView(rootLayout.findViewById(R.id.tv_negative));
            }
        }
    }

    private void bindData(AIRootLayout rootLayout) {
        rootLayout.observeData(mEmoteStateLiveData, mVoiceInputLiveData, mNplResponseLiveData, mApkDownloadLiveData, mUIResponseLiveData);
    }

    private void unBindData(AIRootLayout rootLayout) {
        if (rootLayout == null) return;
        rootLayout.unObserveData(mEmoteStateLiveData, mVoiceInputLiveData, mNplResponseLiveData, mApkDownloadLiveData, mUIResponseLiveData);
        resetLiveDataValue();
    }

    private void resetLiveDataValue() {
        mEmoteStateLiveData.postValue(EmoteState.IDLE);
        mVoiceInputLiveData.postValue(VoiceQuery.Companion.startup());
        mNplResponseLiveData.postValue("");
        mApkDownloadLiveData.postValue(ApkDownload.empty());
        mUIResponseLiveData.postValue(UIResponse.Companion.empty());
    }

    private void showAndAnimate() {
        boolean wasAttached = isAttached;
        boolean wasVisible = isAttached && (mParentView.getVisibility() == View.VISIBLE);
        if (!isAttached) {
            attachToWindow();
        } else {
            if (mWindowManager != null && mWindowParams != null) {
                try {
                    mWindowParams.width = ContextUtils.dp2px(mContext, 554);
                    mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        mWindowParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                    }
                    mWindowManager.updateViewLayout(mParentView, mWindowParams);
                } catch (Exception e) {
                    Timber.tag(TAG).e(e, "update view layout to restore failed!");
                }
            }
        }
        mParentView.setVisibility(View.VISIBLE);
        AIRootLayout rootLayout = mRootLayoutRef.get();
        if (rootLayout != null) {
            if (!wasAttached) {
                mParentView.post(rootLayout::startWakeupAnimation);
            } else if (!wasVisible) {
                rootLayout.startWakeupAnimation();
            }
        }
    }

    private void hideWindow() {
        mParentView.setVisibility(View.GONE);
        mParentView.clearFocus();
        if (isAttached && mWindowManager != null && mWindowParams != null) {
            try {
                mWindowParams.width = 0;
                mWindowParams.height = 0;
                mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                mWindowManager.updateViewLayout(mParentView, mWindowParams);
                Timber.tag(TAG).d("Window params hidden (0x0)");
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "update view layout to hide failed!");
            }
        }
        resetLiveDataValue();
    }
}
