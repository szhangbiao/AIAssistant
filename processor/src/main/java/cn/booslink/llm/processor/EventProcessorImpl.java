package cn.booslink.llm.processor;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Answer;
import cn.booslink.llm.common.model.CBMEvent;
import cn.booslink.llm.common.model.CBMSemantic;
import cn.booslink.llm.common.model.EventData;
import cn.booslink.llm.common.model.EventInfo;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceQuery;
import cn.booslink.llm.common.model.enums.AIUITag;
import cn.booslink.llm.common.model.enums.CBMSub;
import cn.booslink.llm.common.model.enums.QueryState;
import cn.booslink.llm.common.storage.ISpeechStorage;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.common.utils.RxUtil;
import cn.booslink.llm.downloader.IAppManager;
import cn.booslink.llm.processor.process.IIntentProcess;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import timber.log.Timber;

public class EventProcessorImpl implements IEventProcessor {
    private final static String TAG = "EventProcessor";

    private final static String TYPE_VAD = "Vad";
    private final static String VAD_BOS = "Bos";
    private final static String VAD_EOS = "Eos";

    private final Gson mGson;
    private final Handler mHandler;
    private final IAppManager mAppManager;
    private final StringBuilder mNplBuilder;
    private final ISpeechStorage mSpeechStorage;
    private final IIntentProcess mIntentProcess;
    private final ISpeechInteraction mSpeechInteraction;

    private Disposable mEventDisposable;
    private FlowableEmitter<AIUIEvent> mEventEmitter;
    private volatile boolean isSubscriptionActive = false;
    private volatile boolean isDestroyed = false;

    private EventData mEventData = EventData.Companion.empty();

    @Inject
    public EventProcessorImpl(Gson gson, IIntentProcess intentProcess, IAppManager appManager, ISpeechStorage speechStorage, ISpeechInteraction speechInteraction) {
        this.mGson = gson;
        this.mAppManager = appManager;
        this.mIntentProcess = intentProcess;
        this.mSpeechStorage = speechStorage;
        this.mNplBuilder = new StringBuilder();
        this.mSpeechInteraction = speechInteraction;
        this.mHandler = new Handler(Looper.getMainLooper());
        createEventEmitter();
    }

    @Override
    public void processEvent(AIUIEvent event) {
        switch (event.eventType) {
            case AIUIConstant.EVENT_STATE: // 服务状态事件
                break;
            case AIUIConstant.EVENT_RESULT: // 结果事件
                //Timber.tag(TAG).d("result = %s", event.info);
                safeEmitEvent(event);
                break;
            case AIUIConstant.EVENT_WAKEUP: // 唤醒事件
                Timber.tag(TAG).d("wakeup");
                int type = event.arg1; // 0 （语音唤醒）, 1 （发送CMD_WAKEUP手动唤醒）
                mHandler.post(mSpeechInteraction::UIWakeup);
                if (type == 0) {
                    boolean shouldBlockSleepLogic = mAppManager.isPkgDownloading() || mAppManager.isPkgInstalling();
                    if (shouldBlockSleepLogic) return;
                    mSpeechInteraction.updateQuery(new VoiceQuery("bobo在听，有什么可以帮您~", QueryState.WAKE_UP));
                }
                break;
            case AIUIConstant.EVENT_PRE_SLEEP: // 准备休眠事件
                Timber.tag(TAG).d("prepare sleep");
                break;
            case AIUIConstant.EVENT_SLEEP: // 休眠事件
                int sleepType = event.arg1; // 0 （交互超时,自动休眠）, 1 （发送CMD_RESET_WAKEUP手动唤醒）
                Timber.tag(TAG).d("sleep");
                boolean shouldBlockSleepLogic = mAppManager.isPkgDownloading() || mAppManager.isPkgInstalling();
                if (shouldBlockSleepLogic) return;
                boolean showLeaveConfirm = mSpeechStorage.shouldShowLeaveConfirm(sleepType);
                if (showLeaveConfirm) {
                    mSpeechInteraction.semanticAnswer(UIResponse.Companion.withSleep(sleepType));
                } else {
                    mHandler.post(mSpeechInteraction::UISleep);
                }
                break;
            case AIUIConstant.EVENT_VAD: // VAD事件
                int vadState = event.arg1;
                if (vadState == AIUIConstant.VAD_BOS) {
                    Timber.tag(TAG).d("speak start");
                } else if (vadState == AIUIConstant.VAD_EOS) {
                    Timber.tag(TAG).d("speak end");
                } else if (vadState == AIUIConstant.VAD_BOS_TIMEOUT) {
                    Timber.tag(TAG).d("speak timeout");
                } else if (vadState == AIUIConstant.VAD_VOL) {
                    //Timber.tag(TAG).d("speak volume = %d", event.arg2);
                }
                break;
            case AIUIConstant.EVENT_CMD_RETURN: // 某条CMD命令对应的返回事件
                break;
            case AIUIConstant.EVENT_START_RECORD: // 通知外部录音开始，用户可以开始说话
            case AIUIConstant.EVENT_STOP_RECORD: // 通知外部录音停止
                break;
            case AIUIConstant.EVENT_TTS: // 语音合成事件
                break;
            case AIUIConstant.EVENT_CONNECTED_TO_SERVER: // 与服务端建立连接
                Timber.tag(TAG).d("connected to server");
                break;
            case AIUIConstant.EVENT_SERVER_DISCONNECTED: // 与服务端断开连接
                Timber.tag(TAG).d("disconnect to server");
                break;
            case AIUIConstant.EVENT_ERROR: // 出错事件
                //mSpeechInteraction.updateQuery(new VoiceQuery(null, QueryState.ERROR));
                Timber.tag(TAG).d("error = %s", event.info);
                break;
        }
    }

    @Override
    public void release() {
        isDestroyed = true;
        mNplBuilder.delete(0, mNplBuilder.length());
        mHandler.removeCallbacksAndMessages(null);
        if (mEventDisposable != null) {
            mEventDisposable.dispose();
        }
        mEventDisposable = null;
        mEventEmitter = null;
        isSubscriptionActive = false;
    }

    private void createEventEmitter() {
        mEventDisposable = Flowable.create((FlowableOnSubscribe<AIUIEvent>) emitter -> {
                    mEventEmitter = emitter;
                    isSubscriptionActive = true;
                }, BackpressureStrategy.LATEST)
                .map(this::parseEventData)
                .map(this::processSemanticData)
                .compose(RxUtil.flowableOnMain())
                .subscribe(this::populateEventResult, this::parseOrPopulateEventFailed);
    }

    private EventData parseEventData(AIUIEvent event) {
        if (TextUtils.isEmpty(event.info)) return EventData.Companion.empty();
        EventInfo eventInfo = mGson.fromJson(event.info, EventInfo.class);
        CBMSub sub = eventInfo.getSub();
        String cntId = eventInfo.getCntId();
        if (sub == null || TextUtils.isEmpty(cntId)) return EventData.Companion.empty();
        String tag = event.data.getString("tag");
        //Timber.tag(TAG).d("parseEventData, sub = %s", sub);
        try {
            byte[] bytes = event.data.getByteArray(cntId);
            String cntJsonRaw = new String(bytes, StandardCharsets.UTF_8);
            //Timber.tag(TAG).d("parseEventData, cnt json = %s", cntJsonRaw);
            EventData data = mGson.fromJson(cntJsonRaw, EventData.class);
            data.setTag(AIUITag.fromTag(tag));
            data.setSub(sub);
            return data;
        } catch (JsonSyntaxException e) {
            Timber.tag(TAG).e(e, "Parse iat result failed");
        }
        return EventData.Companion.empty();
    }

    private EventData processSemanticData(EventData eventData) {
        if (CBMSub.CBM_SEMANTIC == eventData.getSub() && eventData.getCbmSemantic() != null) {
            CBMSemantic cbmSemantic = eventData.getCbmSemantic().getText();
            if (cbmSemantic == null) return eventData;
            Timber.tag(TAG).d("processSemanticData, category = %s", cbmSemantic.getCategory());
            try {
                UIResponse response = cbmSemantic.getResponse(mGson);
                eventData.setResponse(response);
            } catch (JsonSyntaxException e) {
                Timber.tag(TAG).e(e, "Parse semantic result failed");
            }
        }
        return eventData;
    }

    private void populateEventResult(EventData data) {
        CBMSub sub = data.getSub();
        if (sub == null) return;
        if (sub == CBMSub.IAT) {
            if (data.getText() == null || data.getTag() == AIUITag.LAUNCH) return;
            Timber.tag(TAG).d("iat result = %s", data.getText().getIATVoice());
            mEventData = mEventData.copyIat(data.getText());
            mSpeechInteraction.updateQuery(new VoiceQuery(data.getText().getIATVoice(), QueryState.QUERYING));
        } else if (sub == CBMSub.NLP) {
            if (data.getNlp() == null) return;
            int status = data.getNlp().getStatus() != null ? data.getNlp().getStatus() : -1;
            switch (status) {
                case 0:
                    mNplBuilder.delete(0, mNplBuilder.length());
                    break;
                case 1:
                    mNplBuilder.append(data.getNlp().getText());
                    mSpeechInteraction.nlpAnswer(mNplBuilder.toString());
                    if (data.getTag() == AIUITag.LAUNCH) return;
                    mSpeechInteraction.updateQuery(new VoiceQuery(null, QueryState.DONE));
                    break;
                case 2:
                    String nplContent = mNplBuilder.toString();
                    Timber.tag(TAG).d("nlp, content = %s", nplContent);
                    mSpeechInteraction.nlpAnswer(nplContent);
                    mNplBuilder.delete(0, mNplBuilder.length());
                    mEventData = mEventData.copyNlp(data.getNlp());
                    if (data.getTag() == AIUITag.LAUNCH || TextUtils.isEmpty(nplContent)) return;
                    mSpeechInteraction.updateQuery(new VoiceQuery(null, QueryState.DONE));
                    break;
            }
        } else if (sub == CBMSub.CBM_TIDY) {
            if (data.getCbmTidy() == null || data.getCbmTidy().getText() == null || data.getTag() == AIUITag.LAUNCH) return;
            Timber.tag(TAG).d("cbm tidy, query = %s", data.getCbmTidy().getText().getQuery());
            mEventData = mEventData.copyTidy(data.getCbmTidy());
            mSpeechInteraction.updateQuery(new VoiceQuery(data.getCbmTidy().getText().getQuery(), QueryState.QUERYING));
        } else if (sub == CBMSub.CBM_SEMANTIC) {
            if (data.getResponse() == null || data.getTag() == AIUITag.LAUNCH) return;
            mSpeechInteraction.updateQuery(new VoiceQuery(null, QueryState.DONE));
            mSpeechInteraction.semanticAnswer(data.getResponse());
            if (data.getCbmSemantic() == null) return;
            mEventData = mEventData.copySemantic(data.getCbmSemantic(), data.getResponse());
            CBMSemantic cbmSemantic = data.getCbmSemantic().getText();
            if (cbmSemantic != null && cbmSemantic.getSemantic() != null) {
                mIntentProcess.processIntent(data.getResponse().getCategory(), cbmSemantic.getSemantic());
                if (cbmSemantic.getSemantic().isEmpty()) return;
                Timber.tag(TAG).d("cbm semantic category = %s, intent = %s", data.getResponse().getCategory(), cbmSemantic.getSemantic().get(0).getIntent());
            }
        } else if (sub == CBMSub.CBM_TOOL_PK) {
            if (data.getCbmToolPK() == null || data.getCbmToolPK().getText() == null) return;
            Timber.tag(TAG).d("cbm tool pk = %s", data.getCbmToolPK().getText().getPkType());
        } else if ((sub == CBMSub.EVENT)) {
            if (data.getEvent() == null) return;
            CBMEvent event = data.getEvent().getText();
            if (event == null) return;
            if (TYPE_VAD.equals(event.getType()) && VAD_EOS.equals(event.getKey())) {
                if (mEventData.getCbmSemantic() == null || mEventData.getCbmSemantic().getText() == null) return;
                Answer answer = mEventData.getCbmSemantic().getText().getAnswer();
                String semanticAnswer = answer != null ? answer.getText() : "";
                if (data.getTag() == AIUITag.LAUNCH && TextUtils.isEmpty(mNplBuilder.toString()) && !TextUtils.isEmpty(semanticAnswer)) {
                    mSpeechInteraction.nlpAnswer(semanticAnswer);
                }
            }
            mEventData = EventData.Companion.empty();
        }
    }

    private void safeEmitEvent(AIUIEvent event) {
        if (isDestroyed) {
            return;
        }
        if (!isSubscriptionActive || mEventEmitter == null) {
            Timber.tag(TAG).w("Subscription not active, attempting to recreate");
            recreateSubscription();
        }
        if (mEventEmitter != null && !mEventEmitter.isCancelled()) {
            mEventEmitter.onNext(event);
        }
    }

    private void recreateSubscription() {
        if (isDestroyed) {
            return;
        }
        if (mEventDisposable != null && !mEventDisposable.isDisposed()) {
            mEventDisposable.dispose();
        }
        mEventDisposable = null;
        mEventEmitter = null;
        isSubscriptionActive = false;
        createEventEmitter();
    }

    private void parseOrPopulateEventFailed(Throwable throwable) {
        Timber.tag(TAG).e(throwable, "parseOrPopulateEventFailed");
        isSubscriptionActive = false;
        if (!isDestroyed) {
            recreateSubscription();
        }
    }
}
