package cn.booslink.llm.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.di.CommonEntryPoint;
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.storage.ISpeechStorage;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import dagger.Lazy;
import dagger.hilt.android.EntryPointAccessors;

public class AILeaveLayout extends FrameLayout {
    private Button btnPositive;
    private Button btnNegative;

    private Lazy<ISpeechAgent> mSpeechAgentLazy;
    private Lazy<ISpeechStorage> mSpeechStorageLazy;
    private Lazy<ISpeechInteraction> mSpeechInteractionLazy;

    public AILeaveLayout(@NonNull Context context) {
        this(context, null);
    }

    public AILeaveLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AILeaveLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateLayout(context);
        initWidgets();
        initWidgetListeners();
        initializeDependencies();
    }

    public void initializeDependencies() {
        if (mSpeechInteractionLazy == null || mSpeechStorageLazy == null || mSpeechAgentLazy == null) {
            CommonEntryPoint entryPoint = EntryPointAccessors.fromApplication(getContext().getApplicationContext(), CommonEntryPoint.class);
            mSpeechAgentLazy = entryPoint.lazySpeechAgent();
            mSpeechStorageLazy = entryPoint.lazySpeechStorage();
            mSpeechInteractionLazy = entryPoint.lazySpeechInteraction();
        }
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_leave, this, true);
    }

    private void initWidgets() {
        btnPositive = findViewById(R.id.btn_positive);
        btnNegative = findViewById(R.id.btn_negative);
    }

    private void initWidgetListeners() {
        btnNegative.setOnClickListener(v -> {
            ISpeechInteraction speechInteraction = mSpeechInteractionLazy.get();
            if (speechInteraction == null) return;
            speechInteraction.UISleep();
            ISpeechStorage speechStorage = mSpeechStorageLazy.get();
            if (speechStorage == null) return;
            speechStorage.setShowLeaveConfirm(false);
        });
        btnPositive.setOnClickListener(v -> {
            ISpeechInteraction speechInteraction = mSpeechInteractionLazy.get();
            if (speechInteraction == null) return;
            speechInteraction.UISleep();
        });
    }
}
