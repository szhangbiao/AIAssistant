package cn.booslink.llm.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import javax.inject.Inject;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.model.enums.EmoteState;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class AIRootLayout extends FrameLayout {

    private ImageView ivMascot;
    private FrameLayout flContent;
    private AIInteractionLayout llInteraction;
    private AILeaveLayout flLeave;

    private final Observer<EmoteState> mEmoteStateObserver = this::changeUIWithState;
    private final Observer<String> mVoiceInputObserver = this::changeUIWithVoiceInput;

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
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_root, this, true);
    }

    private void initWidgets() {
        ivMascot = findViewById(R.id.iv_mascot);
        flContent = findViewById(R.id.fl_content);
        llInteraction = findViewById(R.id.ll_interaction);
        flLeave = findViewById(R.id.fl_leave);
    }

    public void observeData(LiveData<EmoteState> emoteStateLiveData, LiveData<String> voiceInputLiveData) {
        emoteStateLiveData.observeForever(mEmoteStateObserver);
        voiceInputLiveData.observeForever(mVoiceInputObserver);
    }

    public void unObserveData(LiveData<EmoteState> emoteStateLiveData, LiveData<String> voiceInputLiveData) {
        emoteStateLiveData.removeObserver(mEmoteStateObserver);
        voiceInputLiveData.removeObserver(mVoiceInputObserver);
    }

    private void changeUIWithState(EmoteState emoteState) {
        switch (emoteState) {
            case NORMAL:
                ivMascot.setImageResource(R.drawable.ic_mascot_normal);
                break;
            case CRYING:
                ivMascot.setImageResource(R.drawable.ic_mascot_crying);
                break;
            case LAUGHING:
                ivMascot.setImageResource(R.drawable.ic_mascot_laughing);
                break;
            case WEATHER_FINE:
                // TODO
                break;
            case IDLE:
                ivMascot.setImageResource(R.drawable.ic_mascot_hello);
            default:
                break;
        }
    }

    private void changeUIWithVoiceInput(String voiceInput) {
        llInteraction.voiceInput(voiceInput);
    }
}
