package cn.booslink.llm.processor.tts;

import static com.iflytek.aiui.AIUIConstant.CANCEL;
import static com.iflytek.aiui.AIUIConstant.CMD_TTS;
import static com.iflytek.aiui.AIUIConstant.START;

import android.text.TextUtils;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIMessage;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.speech.ITTSSpeech;
import dagger.Lazy;
import timber.log.Timber;

public class TTSSpeechImpl implements ITTSSpeech {

    private final static String TAG = "TTSSpeech";

    private final static String KEY_PERCENT = "percent";

    private final Lazy<ISpeechAgent> mSpeechAgentLazy;

    private volatile int mTTSState;
    private volatile String mLastText;

    @Inject
    public TTSSpeechImpl(Lazy<ISpeechAgent> speechAgentLazy) {
        mSpeechAgentLazy = speechAgentLazy;
    }

    @Override
    public void speak(String text) {
        if (TextUtils.isEmpty(text)) return;
        byte[] ttsBytes = text.getBytes(StandardCharsets.UTF_8);
        String params = "vcn=x4_lingxiaoying_em_v2,speed=50,pitch=50,volume=50";
        ISpeechAgent speechAgent = mSpeechAgentLazy.get();
        if (speechAgent == null) return;
        this.mLastText = text;
        speechAgent.sendMessage(new AIUIMessage(CMD_TTS, START, 0, params, ttsBytes));
    }

    @Override
    public synchronized void ttsState(AIUIEvent event) {
        this.mTTSState = event.arg1;
        switch (mTTSState) {
            case AIUIConstant.TTS_SPEAK_BEGIN:
                Timber.tag(TAG).d("Speak begin");
                break;
            case AIUIConstant.TTS_SPEAK_PROGRESS:
                Timber.tag(TAG).d("Speak progress = %d", event.data.getInt(KEY_PERCENT));
                break;
            case AIUIConstant.TTS_SPEAK_PAUSED:
                Timber.tag(TAG).d("Speak pause");
                break;
            case AIUIConstant.TTS_SPEAK_RESUMED:
                Timber.tag(TAG).d("Speak resumed");
                break;
            case AIUIConstant.TTS_SPEAK_COMPLETED:
                Timber.tag(TAG).d("Speak completed");
                break;
        }
    }

    @Override
    public void cancel() {
        if (TextUtils.isEmpty(mLastText)) return;
        byte[] ttsBytes = mLastText.getBytes(StandardCharsets.UTF_8);
        String params = "vcn=x4_lingxiaoying_em_v2,speed=50,pitch=50,volume=50";
        ISpeechAgent speechAgent = mSpeechAgentLazy.get();
        if (speechAgent == null) return;
        mLastText = null;
        speechAgent.sendMessage(new AIUIMessage(CMD_TTS, CANCEL, 0, params, ttsBytes));
    }
}
