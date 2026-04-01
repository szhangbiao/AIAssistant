package cn.booslink.llm.speech;

import android.content.Context;

import com.google.gson.Gson;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Device;
import cn.booslink.llm.common.model.enums.AIUIState;
import cn.booslink.llm.common.utils.RxUtil;
import cn.booslink.llm.speech.config.AIUIConfig;
import cn.booslink.llm.speech.repository.IConfigRepository;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import timber.log.Timber;

public class SpeechAgentImpl implements ISpeechAgent, AIUIListener {

    private final static String TAG = "SpeechAgent";

    private final Gson mGson;
    private final Context mContext;
    private final IConfigRepository mConfigRepository;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private AIUIConfig mAIUIConfig = null;
    private AIUIAgent mAIUIAgent = null;

    @Inject
    public SpeechAgentImpl(@ApplicationContext Context context, Device device, Gson gson, IConfigRepository configRepository) {
        this.mGson = gson;
        this.mContext = context;
        this.mConfigRepository = configRepository;
        AIUISetting.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, device.sn);
    }

    @Override
    public void createAgent() {
        // TODO WRITE_EXTERNAL_STORAGE 权限
        if (mAIUIAgent == null) {
            Disposable disposable = mConfigRepository.readConfig()
                    .map(aiuiConfig -> {
                        mAIUIConfig = aiuiConfig;
                        return mGson.toJson(aiuiConfig);
                    })
                    .compose(RxUtil.singleOnMain())
                    .subscribe(aiuiParams -> {
                        // init aiui agent
                        mAIUIAgent = AIUIAgent.createAgent(mContext, aiuiParams, this);
                    });
            addDisposable(disposable);
        }
    }

    @Override
    public void sendMessage(AIUIMessage message) {
        if (message == null) return;
        mAIUIAgent.sendMessage(message);
    }

    @Override
    public void onEvent(AIUIEvent event) {
        Timber.tag(TAG).d("onEvent, type = %d", event.eventType);
        switch (event.eventType) {
            case AIUIConstant.EVENT_STATE: // 服务状态事件
                int state = event.arg1;
                AIUIState aiuiState = state < AIUIState.values().length - 1 ? AIUIState.values()[state] : AIUIState.UNKNOWN;
                Timber.tag(TAG).d("onEvent, state = %s", aiuiState);
                break;
            case AIUIConstant.EVENT_RESULT: // 结果事件
                break;
            case AIUIConstant.EVENT_WAKEUP: // 唤醒事件
            case AIUIConstant.EVENT_PRE_SLEEP: // 准备休眠事件
            case AIUIConstant.EVENT_SLEEP: // 休眠事件
                break;
            case AIUIConstant.EVENT_VAD: // VAD事件
                break;
            case AIUIConstant.EVENT_CMD_RETURN: // 某条CMD命令对应的返回事件
                break;
            case AIUIConstant.EVENT_START_RECORD: // 通知外部录音开始，用户可以开始说话
            case AIUIConstant.EVENT_STOP_RECORD: // 通知外部录音停止
                break;
            case AIUIConstant.EVENT_TTS: // 语音合成事件
                break;
            case AIUIConstant.EVENT_CONNECTED_TO_SERVER: // 与服务端建立连接
            case AIUIConstant.EVENT_SERVER_DISCONNECTED: // 与服务端断开连接
            case AIUIConstant.EVENT_ERROR: // 出错事件
                break;
        }
    }

    @Override
    public void destroyAgent() {
        clear();
        if (null != mAIUIAgent) {
            mAIUIAgent.destroy();
            mAIUIAgent = null;
        }
    }

    protected void addDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    public void clear() {
        mCompositeDisposable.clear();
    }
}
