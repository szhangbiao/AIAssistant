package cn.booslink.llm.common.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.libpag.PAGImageView;

import javax.inject.Inject;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.enums.EmoteState;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class AIRootLayout extends FrameLayout {

    private ImageView ivMascot;
    private PAGImageView pagAnimation;
    private View vContent;
    private FrameLayout flContent;
    private AIInteractionLayout llInteraction;
    private AILeaveLayout flLeave;

    private final Observer<EmoteState> mEmoteStateObserver = this::changeUIWithState;
    private final Observer<String> mVoiceInputObserver = this::changeUIWithVoiceInput;
    private final Observer<String> mNplResponseObserver = this::changeUIWithNplResponse;
    private final Observer<ApkDownload> mApkDownloadObserver = this::changeUIWithApkDownload;


    @Inject
    public AIRootLayout(@ApplicationContext Context context) {
        super(context);
        inflateLayout(context);
        initWidgets();
        // 监听fl_content高度变化，同步更新v_content高度
        setupContentHeightSync();
    }

    public AIRootLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AIRootLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateLayout(context);
        initWidgets();
        // 监听fl_content高度变化，同步更新v_content高度
        setupContentHeightSync();
        setKeepScreenOn(true);
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_root, this, true);
    }

    private void initWidgets() {
        ivMascot = findViewById(R.id.iv_mascot);
        pagAnimation = findViewById(R.id.pag_animation);
        vContent = findViewById(R.id.v_content);
        flContent = findViewById(R.id.fl_content);
        llInteraction = findViewById(R.id.ll_interaction);
        flLeave = findViewById(R.id.fl_leave);
    }

    public void observeData(LiveData<EmoteState> emoteStateLiveData, LiveData<String> voiceInputLiveData, LiveData<String> nplResponseLiveData, LiveData<ApkDownload> apkDownloadLiveData) {
        emoteStateLiveData.observeForever(mEmoteStateObserver);
        voiceInputLiveData.observeForever(mVoiceInputObserver);
        nplResponseLiveData.observeForever(mNplResponseObserver);
        apkDownloadLiveData.observeForever(mApkDownloadObserver);
    }

    public void unObserveData(LiveData<EmoteState> emoteStateLiveData, LiveData<String> voiceInputLiveData, LiveData<String> nplResponseLiveData, LiveData<ApkDownload> apkDownloadLiveData) {
        emoteStateLiveData.removeObserver(mEmoteStateObserver);
        voiceInputLiveData.removeObserver(mVoiceInputObserver);
        nplResponseLiveData.removeObserver(mNplResponseObserver);
        apkDownloadLiveData.removeObserver(mApkDownloadObserver);
    }

    /**
     * 设置fl_content高度变化时同步更新v_content高度
     */
    private void setupContentHeightSync() {
        flContent.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // 获取fl_content的实际高度
            int contentHeight = flContent.getHeight();
            if (contentHeight > 0) {
                // 更新v_content的高度
                LayoutParams params = (LayoutParams) vContent.getLayoutParams();
                params.height = contentHeight;
                vContent.setLayoutParams(params);
            }
        });
    }

    private void changeUIWithState(EmoteState emoteState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            populateMascotAnimation(emoteState);
        } else {
            populateMascotImage(emoteState);
        }
    }

    private void changeUIWithVoiceInput(String voiceInput) {
        llInteraction.voiceInput(voiceInput);
    }

    private void changeUIWithNplResponse(String nplText) {
        llInteraction.nplReply(nplText);
    }

    private void changeUIWithApkDownload(ApkDownload apkDownload) {
        llInteraction.showDownloadProcess(apkDownload);
    }

    private void populateMascotAnimation(EmoteState emoteState) {
        if (pagAnimation.isPlaying()) {
            pagAnimation.pause();
        }
        switch (emoteState) {
            case WEATHER_SUNNY:
                pagAnimation.setPath("assets://pag_sunny.pag");
                break;
            case WEATHER_CLOUDY:
                pagAnimation.setPath("assets://pag_cloudy.pag");
                break;
            case WEATHER_FOG:
                pagAnimation.setPath("assets://pag_fog.pag");
                break;
            case WEATHER_OVERCAST:
                pagAnimation.setPath("assets://pag_overcast.pag");
                break;
            case WEATHER_RAINSTORM:
                pagAnimation.setPath("assets://pag_rain_storm.pag");
                break;
            case WEATHER_SANDSTORM:
                pagAnimation.setPath("assets://pag_sand_storm.pag");
                break;
            case WEATHER_SMALL_RAIN:
                pagAnimation.setPath("assets://pag_small_rain.pag");
                break;
            case WEATHER_SNOW:
                pagAnimation.setPath("assets://pag_snow.pag");
                break;
            case NORMAL:
                pagAnimation.setPath("assets://pag_wink.pag");
                break;
            case CRYING:
                pagAnimation.setPath("assets://pag_crying.pag");
                break;
            case LAUGHING:
                pagAnimation.setPath("assets://pag_laughing.pag");
                break;
            case THINKING:
                pagAnimation.setPath("assets://pag_thinking.pag");
                break;
            case IDLE:
                pagAnimation.setPath("assets://pag_hello.pag");
                break;
            default:
                break;
        }
        pagAnimation.setRepeatCount(-1);
        pagAnimation.play();
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
}
