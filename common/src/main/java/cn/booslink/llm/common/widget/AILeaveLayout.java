package cn.booslink.llm.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.di.CommonEntryPoint;
import cn.booslink.llm.common.model.VoiceQuery;
import cn.booslink.llm.common.model.enums.AIUITag;
import cn.booslink.llm.common.model.enums.QueryState;
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.storage.ISpeechStorage;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import dagger.Lazy;
import dagger.hilt.android.EntryPointAccessors;

public class AILeaveLayout extends FrameLayout {

    private TextView tvTitle;
    private Button btnPositive;
    private Button btnNegative;

    private Lazy<ISpeechAgent> mSpeechAgentLazy;
    private Lazy<ISpeechStorage> mSpeechStorageLazy;
    private Lazy<ISpeechInteraction> mSpeechInteractionLazy;

    private int mSleepType = -1;

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

    public void updateByType(int sleepType) {
        if (sleepType < 0 || sleepType > 1) return;
        this.mSleepType = sleepType;
        tvTitle.setText(sleepType == 1 ? R.string.speech_sleep_active : R.string.speech_sleep_passive);
        btnPositive.setText(sleepType == 1 ? R.string.active_button_positive : R.string.passive_button_positive);
        btnNegative.setText(sleepType == 1 ? R.string.active_button_negative : R.string.passive_button_negative);
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_leave, this, true);
    }

    private void initWidgets() {
        tvTitle = findViewById(R.id.tv_leave_title);
        btnPositive = findViewById(R.id.btn_positive);
        btnNegative = findViewById(R.id.btn_negative);
    }

    private void initWidgetListeners() {
        btnNegative.setOnClickListener(v -> {
            if (mSleepType < 0 || mSleepType > 1) return;
            ISpeechInteraction speechInteraction = mSpeechInteractionLazy.get();
            if (speechInteraction == null) return;
            speechInteraction.UISleep();
            ISpeechStorage speechStorage = mSpeechStorageLazy.get();
            if (speechStorage == null) return;
            speechStorage.setShowLeaveConfirm(mSleepType, false);
        });
        btnPositive.setOnClickListener(v -> {
            if (mSleepType < 0 || mSleepType > 1) return;
            ISpeechInteraction speechInteraction = mSpeechInteractionLazy.get();
            if (speechInteraction == null) return;
            if (mSleepType == 1) {
                speechInteraction.UISleep();
            } else {
                ISpeechAgent speechAgent = mSpeechAgentLazy.get();
                if (speechAgent == null) return;
                speechAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, null, null));
                speechInteraction.updateQuery(new VoiceQuery("bobo在听，有什么可以帮您~", QueryState.WAKE_UP));
            }
        });
    }
}
