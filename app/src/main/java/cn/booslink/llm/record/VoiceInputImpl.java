package cn.booslink.llm.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import cn.booslink.llm.common.speech.ISpeechAgent;
import timber.log.Timber;

public class VoiceInputImpl implements IVoiceInput {

    private static final String TAG = "VoiceInput";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord mAudioRecord;
    private final ISpeechAgent mSpeechAgent;

    private volatile boolean mAudioRecording = false;
    private Disposable mRecordDisposable;
    private Disposable mStopDisposable;

    @Inject
    public VoiceInputImpl(ISpeechAgent speechAgent) {
        this.mSpeechAgent = speechAgent;
    }

    @Override
    public synchronized void startVoice() {
        // 如果在延迟停止的期间内再次被调用，取消停止任务并复用当前录音
        if (mStopDisposable != null && !mStopDisposable.isDisposed()) {
            mStopDisposable.dispose();
            mStopDisposable = null;
            Timber.tag(TAG).d("startVoice called during stop delay, cancelling stop request");
            if (mAudioRecording) {
                return; // 继续使用当前的录音会话
            }
        }
        if (!initAudioRecord()) {
            return;
        }
        startRecordTask();
    }

    @Override
    public synchronized void stopVoice() {
        // 防止重复触发
        if (mStopDisposable != null && !mStopDisposable.isDisposed()) {
            return;
        }
        mStopDisposable = Completable.timer(1, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(() -> {
                    synchronized (VoiceInputImpl.this) {
                        stopRecordTask();
                        releaseAudioRecord();
                        // 发送停止写入的指令
                        AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_STOP_WRITE, 0, 0, "data_type=audio,sample_rate=16000", null);
                        mSpeechAgent.sendMessage(msg);
                        Timber.tag(TAG).d("CMD_STOP_WRITE after 1s delay");
                    }
                }, throwable -> {
                    Timber.tag(TAG).e(throwable, "Error during delayed stopVoice");
                });
    }

    @Override
    public synchronized void release() {
        if (mStopDisposable != null && !mStopDisposable.isDisposed()) {
            mStopDisposable.dispose();
            mStopDisposable = null;
        }
        stopRecordTask();
        releaseAudioRecord();
    }

    private boolean initAudioRecord() {
        releaseAudioRecord(); // 确保之前的已被释放
        try {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, MIN_BUFFER_SIZE);
            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Timber.tag(TAG).e("error, after new instance, mAudioRecord.getState() is %s", mAudioRecord.getState());
                return false;
            }
            Timber.tag(TAG).d("AudioRecord new instance finished");
            mAudioRecord.startRecording();
            if (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                Timber.tag(TAG).e("error, after startRecording, not start, mAudioRecord.getState() is %s", mAudioRecord.getRecordingState());
                return false;
            }
            Timber.tag(TAG).d("AudioRecord startRecording success!");
            return true;
        } catch (SecurityException se) {
            Timber.tag(TAG).e(se, "Permission denied for RECORD_AUDIO");
            return false;
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Error initializing AudioRecord");
            return false;
        }
    }

    private void releaseAudioRecord() {
        if (mAudioRecord != null) {
            try {
                if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    mAudioRecord.stop();
                }
                Timber.tag(TAG).d("AudioRecord release");
                mAudioRecord.release();
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "Error stopping/releasing AudioRecord");
            } finally {
                mAudioRecord = null;
            }
        }
    }

    private void startRecordTask() {
        mAudioRecording = true;
        Timber.tag(TAG).d("startRecordTask, isReady=%b", mSpeechAgent.isAIUIReady());
        mRecordDisposable = Schedulers.io().scheduleDirect(() -> {
            final byte[] buf = new byte[MIN_BUFFER_SIZE];
            while (mAudioRecording && mAudioRecord != null) {
                int len = mAudioRecord.read(buf, 0, buf.length);
                if (len < 0) {
                    Timber.tag(TAG).e("mAudioRecord.read return %d, stopping recording!", len);
                    return;
                }
                if (len != buf.length) {
                    Timber.tag(TAG).w("AudioRecord need read " + buf.length + " but real read " + len);
                }
                if (len > 0 && mSpeechAgent.isAIUIReady()) {
                    byte[] sendBuf = Arrays.copyOf(buf, len);
                    //Timber.tag(TAG).d("CMD_WRITE, size = %d", sendBuf.length);
                    AIUIMessage writeAudio = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, "data_type=audio,sample_rate=16000", sendBuf);
                    mSpeechAgent.sendMessage(writeAudio);
                }
            }
        });
    }

    private byte[] adjPcmVol(byte[] src) {
        int cur = 0;
        byte[] buf = new byte[src.length];
        short maxVol = 0;
        while (cur < src.length) {
            short vol = getShort(src, cur);
            short absVol = (short) Math.abs(vol);
            if (maxVol < absVol) {
                maxVol = absVol;
            }
            long volLong = (long) (vol * 0.008f);
            if (volLong < -0x8000) {
                vol = -0x8000;
            } else if (volLong > 0x7FFF) {
                vol = 0x7FFF;
            } else {
                vol = (short) volLong;
            }
            buf[cur] = (byte) (vol & 0xFF);
            buf[cur + 1] = (byte) ((vol >> 8) & 0xFF);
            cur += 2;
        }
        //Timber.tag(TAG).d("maxVol is " + maxVol + ", len is " + src.length);
        return buf;
    }

    private short getShort(byte[] data, int start) {
        return (short) ((data[start] & 0xFF) | (data[start + 1] << 8));
    }

    /**
     * 停止录音任务
     */
    private void stopRecordTask() {
        mAudioRecording = false;
        if (mRecordDisposable != null && !mRecordDisposable.isDisposed()) {
            mRecordDisposable.dispose();
            mRecordDisposable = null;
        }
    }
}
