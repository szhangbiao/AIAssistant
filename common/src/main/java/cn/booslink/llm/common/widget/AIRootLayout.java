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

    private final Observer<EmoteState> mEmoteStateObserver = this::changeUIWithState;

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
    }

    public void observeData(LiveData<EmoteState> mEmoteState) {
        mEmoteState.observeForever(mEmoteStateObserver);
    }

    public void unObserveData(LiveData<EmoteState> mEmoteState) {
        mEmoteState.removeObserver(mEmoteStateObserver);
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
}
