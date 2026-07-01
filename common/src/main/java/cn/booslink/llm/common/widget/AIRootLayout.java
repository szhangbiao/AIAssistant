package cn.booslink.llm.common.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.libpag.PAGFile;
import org.libpag.PAGView;

import javax.inject.Inject;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.di.CommonEntryPoint;
import cn.booslink.llm.common.loader.IPAGLoader;
import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceQuery;
import cn.booslink.llm.common.model.WeatherUI;
import cn.booslink.llm.common.model.enums.EmoteState;
import cn.booslink.llm.common.model.enums.QueryState;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

public class AIRootLayout extends ConstraintLayout {

    private static final String TAG = "RootLayout";

    private ImageView ivMascot;
    private View vContent;
    private PAGView pagAnimation;
    private AIInteractionLayout clInteraction;
    private AILeaveLayout flLeave;
    private FrameLayout flContent;

    private final Observer<EmoteState> mEmoteStateObserver = this::changeUIWithState;
    private final Observer<VoiceQuery> mVoiceInputObserver = this::changeUIWithVoiceInput;
    private final Observer<String> mNplResponseObserver = this::changeUIWithNplResponse;
    private final Observer<ApkDownload> mApkDownloadObserver = this::changeUIWithApkDownload;
    private final Observer<UIResponse> mUIResponseObserver = this::changeUIWithUIResponse;

    private EmoteState mCurrentEmoteState = null;
    private QueryState mCurrentQueryState = null;

    @Inject
    public AIRootLayout(@ApplicationContext Context context) {
        super(context);
        inflateLayout(context);
        initWidgets();
    }

    public AIRootLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AIRootLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateLayout(context);
        initWidgets();
        setKeepScreenOn(true);
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_root, this, true);
    }

    private void initWidgets() {
        ivMascot = findViewById(R.id.iv_mascot);
        vContent = findViewById(R.id.v_content);
        pagAnimation = findViewById(R.id.pag_animation);
        clInteraction = findViewById(R.id.ll_interaction);
        flLeave = findViewById(R.id.fl_leave);
        flContent = findViewById(R.id.fl_content);
        if (pagAnimation != null) {
            pagAnimation.setRepeatCount(-1);
            pagAnimation.setMaxFrameRate(30f);
        }
    }

    public void observeData(LiveData<EmoteState> emoteStateLiveData, LiveData<VoiceQuery> voiceInputLiveData, LiveData<String> nplResponseLiveData, LiveData<ApkDownload> apkDownloadLiveData, LiveData<UIResponse> uiResponseLiveData) {
        emoteStateLiveData.observeForever(mEmoteStateObserver);
        voiceInputLiveData.observeForever(mVoiceInputObserver);
        nplResponseLiveData.observeForever(mNplResponseObserver);
        apkDownloadLiveData.observeForever(mApkDownloadObserver);
        uiResponseLiveData.observeForever(mUIResponseObserver);
    }

    public void unObserveData(LiveData<EmoteState> emoteStateLiveData, LiveData<VoiceQuery> voiceInputLiveData, LiveData<String> nplResponseLiveData, LiveData<ApkDownload> apkDownloadLiveData, LiveData<UIResponse> uiResponseLiveData) {
        emoteStateLiveData.removeObserver(mEmoteStateObserver);
        voiceInputLiveData.removeObserver(mVoiceInputObserver);
        nplResponseLiveData.removeObserver(mNplResponseObserver);
        apkDownloadLiveData.removeObserver(mApkDownloadObserver);
        uiResponseLiveData.removeObserver(mUIResponseObserver);
    }

    public void startWakeupAnimation() {
        View mascotView = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? pagAnimation : ivMascot;
        // mascotView: scale 0.5 -> 1.0, duration 500ms
        startScaleAnim(mascotView, 0.3f, 1.0f, 400);
        // flContent: alpha 0.6 -> 1.0, duration 300ms, delay 200ms
        startAlphaAnim(flContent, 0.6f, 1.0f, 300, 100, null);
        // vContent: alpha 0.6 -> 1.0, duration 300ms, delay 200ms
        startAlphaAnim(vContent, 0.6f, 1.0f, 300, 100, null);
    }

    public void startHideAnimation(Runnable runnable) {
        View mascotView = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? pagAnimation : ivMascot;
        // mascotView: scale 1.0 -> 0.4, duration 300ms
        startScaleAnim(mascotView, 1.0f, 0.3f, 300);
        // flContent: alpha 1.0 -> 0.6, duration 300ms
        startAlphaAnim(flContent, 1.0f, 0.6f, 300, 0, null);
        // vContent: alpha 1.0 -> 0.6, duration 300ms
        startAlphaAnim(vContent, 1.0f, 0.6f, 300, 0, runnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pagAnimation != null) {
            pagAnimation.pause();
            pagAnimation.setComposition(null); // 切断底层强引用
            pagAnimation.freeCache();
        }
    }

    private void changeUIWithState(EmoteState emoteState) {
        if (mCurrentEmoteState != null && mCurrentEmoteState == emoteState) return;
        mCurrentEmoteState = emoteState;
        Timber.tag(TAG).d("changeUIWithState, state = %s", emoteState);
        //processLoadingState(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            populateMascotAnimation(emoteState);
        } else {
            populateMascotImage(emoteState);
        }
    }

    private void changeUIWithVoiceInput(VoiceQuery query) {
        flLeave.setVisibility(View.GONE);
        clInteraction.setVisibility(View.VISIBLE);
        String voiceInput = query.getQuery();
        if (!TextUtils.isEmpty(voiceInput)) {
            Timber.tag(TAG).d("changeUIWithVoiceInput, input = %s", voiceInput);
            clInteraction.voiceInput(voiceInput);
        }
        if (mCurrentQueryState != null && mCurrentQueryState == query.getState()) return;
        mCurrentQueryState = query.getState();
        Timber.tag(TAG).d("changeUIWithVoiceInput, state = %s", query);
        switch (query.getState()) {
            case IDLE:
                clInteraction.updateTipTitle(getContext().getString(R.string.speech_initial_help));
                break;
            case WAKE_UP:
                clInteraction.updateTipTitle(getContext().getString(R.string.speech_voice_listening));
                clInteraction.showWakeup();
                break;
            case QUERYING:
                clInteraction.updateTipTitle(getContext().getString(R.string.speech_voice_querying));
                break;
            case EMPTY:
            case DOWNLOADING:
            case DONE:
                clInteraction.updateTipTitle(getContext().getString(R.string.speech_voice_result));
                break;
            case FAILED:
            case ERROR:
                clInteraction.updateTipTitle(getContext().getString(R.string.speech_voice_sorry));
                break;
            default:
                break;
        }
        processLoadingState(mCurrentQueryState);
    }

    private void changeUIWithNplResponse(String nplText) {
        flLeave.setVisibility(View.GONE);
        clInteraction.setVisibility(View.VISIBLE);
        clInteraction.nplReply(nplText);
    }

    private void changeUIWithApkDownload(ApkDownload apkDownload) {
        flLeave.setVisibility(View.GONE);
        clInteraction.setVisibility(View.VISIBLE);
        clInteraction.showDownloadProcess(apkDownload);
    }

    private void changeUIWithUIResponse(UIResponse response) {
        switch (response.getCategory()) {
            case WEATHER:
                if (response.getWeathers() == null) return;
                WeatherUI weatherUI = WeatherUI.Companion.fromWeatherList(response.getQueryDate(), response.getWeathers());
                flLeave.setVisibility(View.GONE);
                clInteraction.setVisibility(View.VISIBLE);
                clInteraction.showWeatherList(weatherUI);
                break;
            case SLEEP:
                //int sleepType = response.getSleepType() != null ? response.getSleepType() : 0;
                clInteraction.stopAnimIfNeed();
                clInteraction.setVisibility(View.GONE);
                flLeave.setVisibility(View.VISIBLE);
            default:
                break;
        }
    }

    private void populateMascotAnimation(EmoteState emoteState) {
        if (pagAnimation.isPlaying()) {
            pagAnimation.pause();
        }
        IPAGLoader pagLoader = getPagLoader();
        PAGFile targetFile = pagLoader != null ? pagLoader.getPagFile(emoteState.getFileKey()) : null;
        if (targetFile != null) {
            pagAnimation.setComposition(targetFile);
        } else {
            String pagFileName = emoteState.getFileKey();
            pagAnimation.setPathAsync("assets://" + pagFileName, pagFile -> {
                if (pagLoader != null) {
                    pagLoader.putPagFile(pagFileName, pagFile);
                }
            });
        }
        pagAnimation.play();
    }

    private IPAGLoader mPagLoader;

    private IPAGLoader getPagLoader() {
        if (mPagLoader == null) {
            CommonEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(getContext().getApplicationContext(), CommonEntryPoint.class);
            mPagLoader = hiltEntryPoint.lazyPAGLoader().get();
        }
        return mPagLoader;
    }

    private void populateMascotImage(EmoteState emoteState) {
        switch (emoteState) {
            case WEATHER_SUNNY:
                ivMascot.setImageResource(R.drawable.ic_mascot_sunny);
                break;
            case WEATHER_CLOUDY:
                ivMascot.setImageResource(R.drawable.ic_mascot_cloudy);
                break;
            case WEATHER_FOG:
                ivMascot.setImageResource(R.drawable.ic_mascot_fog);
                break;
            case WEATHER_OVERCAST:
                ivMascot.setImageResource(R.drawable.ic_mascot_overcast);
                break;
            case WEATHER_RAINSTORM:
                ivMascot.setImageResource(R.drawable.ic_mascot_rainstorm);
                break;
            case WEATHER_SANDSTORM:
                ivMascot.setImageResource(R.drawable.ic_mascot_sandstorm);
                break;
            case WEATHER_SMALL_RAIN:
                ivMascot.setImageResource(R.drawable.ic_mascot_small_rain);
                break;
            case WEATHER_SNOW:
                ivMascot.setImageResource(R.drawable.ic_mascot_snow);
                break;
            case NORMAL:
                ivMascot.setImageResource(R.drawable.ic_mascot_normal);
                break;
            case CRYING:
                ivMascot.setImageResource(R.drawable.ic_mascot_crying);
                break;
            case LAUGHING:
                ivMascot.setImageResource(R.drawable.ic_mascot_laughing);
                break;
            case THINKING:
                ivMascot.setImageResource(R.drawable.ic_mascot_thinking);
                break;
            case IDLE:
                ivMascot.setImageResource(R.drawable.ic_mascot_hello);
            default:
                break;
        }
    }

    private void processLoadingState(QueryState queryState) {
        boolean isQuerying = queryState == QueryState.QUERYING;
        boolean shouldShowLoading = queryState == QueryState.QUERYING || queryState == QueryState.IDLE;
        clInteraction.showAnimLoading(isQuerying);
        clInteraction.showTextLoading(shouldShowLoading);

    }

    private void startScaleAnim(View animView, float startScale, float endScale, long duration) {
        animView.setScaleX(startScale);
        animView.setScaleY(startScale);
        TimeInterpolator interpolator = startScale < endScale ? new AccelerateInterpolator() : new DecelerateInterpolator();
        animView.animate()
                .scaleX(endScale)
                .scaleY(endScale)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start();
    }

    private void startAlphaAnim(View animView, float startAlpha, float endAlpha, long duration, long delay, Runnable runnable) {
        animView.setAlpha(startAlpha);
        animView.animate()
                .alpha(endAlpha)
                .setDuration(duration)
                .setStartDelay(delay)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                })
                .setInterpolator(new LinearInterpolator())
                .start();
    }
}
