package cn.booslink.llm.processor.status;

import android.os.Handler;
import android.os.Looper;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;

import javax.inject.Inject;

import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.speech.ISpeechStatus;
import cn.booslink.llm.processor.IEventProcessor;
import dagger.Lazy;
import timber.log.Timber;

public class SpeechStatusImpl implements ISpeechStatus {

    private static final String TAG = "SpeechStatus";

    private static final long SLEEP_TIME = 20_000L;

    private final Handler mHandler;
    private final Runnable mSleepRunnable;
    private final Lazy<ISpeechAgent> mSpeechAgentLazy;
    private final Lazy<IEventProcessor> mEventProcessorLazy;

    private volatile boolean mIsTimeoutSleeping;

    @Inject
    public SpeechStatusImpl(Lazy<ISpeechAgent> speechStatusLazy, Lazy<IEventProcessor> eventProcessorLazy) {
        mSpeechAgentLazy = speechStatusLazy;
        mEventProcessorLazy = eventProcessorLazy;
        mHandler = new Handler(Looper.getMainLooper());
        mSleepRunnable = () -> {
            ISpeechAgent speechAgent = mSpeechAgentLazy.get();
            IEventProcessor eventProcessor = mEventProcessorLazy.get();
            if (speechAgent == null || eventProcessor == null) return;
            boolean isSpeechActive = eventProcessor.isInteractionActive();
            Timber.tag(TAG).d("Count down to sleep, active = %b", isSpeechActive);
            if (isSpeechActive) {
                reset();
                return;
            }
            mIsTimeoutSleeping = true;
            speechAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_RESET_WAKEUP, 0, 0, null, null));
        };
    }

    @Override
    public void wakeup() {
        Timber.tag(TAG).d("wakeup");
        mIsTimeoutSleeping = false;
        mHandler.removeCallbacks(mSleepRunnable);
        mHandler.postDelayed(mSleepRunnable, SLEEP_TIME);
    }

    @Override
    public void sleep() {
        Timber.tag(TAG).d("sleep");
        mIsTimeoutSleeping = false;
        mHandler.removeCallbacks(mSleepRunnable);
    }

    @Override
    public void reset() {
        Timber.tag(TAG).d("reset");
        //mIsTimeoutSleeping = false;
        mHandler.removeCallbacks(mSleepRunnable);
        mHandler.postDelayed(mSleepRunnable, SLEEP_TIME);
    }

    @Override
    public boolean isTimeout() {
        return mIsTimeoutSleeping;
    }
}
