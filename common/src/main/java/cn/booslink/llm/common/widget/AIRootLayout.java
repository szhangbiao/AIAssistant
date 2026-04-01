package cn.booslink.llm.common.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import javax.inject.Inject;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.model.enums.EmoteState;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class AIRootLayout extends FrameLayout {

    private ImageView ivMascot;
    private FrameLayout flContent;

    private Observer<EmoteState> mEmoteStateObserver;

    @Inject
    public AIRootLayout(@ApplicationContext Context context) {
        super(context, null);
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
}
