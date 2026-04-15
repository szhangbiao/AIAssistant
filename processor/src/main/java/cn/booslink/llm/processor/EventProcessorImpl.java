package cn.booslink.llm.processor;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import cn.booslink.llm.common.model.CBMSemantic;
import cn.booslink.llm.common.model.EventData;
import cn.booslink.llm.common.model.EventInfo;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceQuery;
import cn.booslink.llm.common.model.enums.AIUITag;
import cn.booslink.llm.common.model.enums.CBMSub;
import cn.booslink.llm.common.model.enums.QueryState;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.common.utils.RxUtil;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import timber.log.Timber;

public class EventProcessorImpl implements IEventProcessor {
    private final static String TAG = "EventProcessor";

    private final Gson mGson;
    private final ISpeechInteraction mSpeechInteraction;
    private final StringBuilder mNplBuilder = new StringBuilder();

    private Disposable mEventDisposable;
    private FlowableEmitter<AIUIEvent> mEventEmitter;
    private volatile boolean isSubscriptionActive = false;
    private volatile boolean isDestroyed = false;

    @Inject
    public EventProcessorImpl(Gson gson, ISpeechInteraction speechInteraction) {
        this.mGson = gson;
        this.mSpeechInteraction = speechInteraction;
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
                int type = event.arg1; // 0 （语音唤醒）, 1 （发送CMD_WAKEUP手动唤醒）
                if (type == 0) {
                    mSpeechInteraction.updateQuery(new VoiceQuery("bobo在听，有什么可以帮您~", QueryState.WAKE_UP));
                }
                Timber.tag(TAG).d("wakeup");
                break;
            case AIUIConstant.EVENT_PRE_SLEEP: // 准备休眠事件
                Timber.tag(TAG).d("prepare sleep");
                break;
            case AIUIConstant.EVENT_SLEEP: // 休眠事件
                Timber.tag(TAG).d("sleep");
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
        Timber.tag(TAG).d("parseEventData, sub = %s", sub);
        try {
            byte[] bytes = event.data.getByteArray(cntId);
            String cntJsonRaw = new String(bytes, StandardCharsets.UTF_8);
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
                    Timber.tag(TAG).d("nlp, content = %s", mNplBuilder.toString());
                    mSpeechInteraction.nlpAnswer(mNplBuilder.toString());
                    if (data.getTag() == AIUITag.LAUNCH) return;
                    mSpeechInteraction.updateQuery(new VoiceQuery(null, QueryState.DONE));
                    break;
            }
        } else if (sub == CBMSub.CBM_TIDY) {
            if (data.getCbmTidy() == null || data.getCbmTidy().getText() == null || data.getTag() == AIUITag.LAUNCH) return;
            Timber.tag(TAG).d("cbm tidy, query = %s", data.getCbmTidy().getText().getQuery());
            mSpeechInteraction.updateQuery(new VoiceQuery(data.getCbmTidy().getText().getQuery(), QueryState.QUERYING));
        } else if (sub == CBMSub.CBM_SEMANTIC) {
            if (data.getResponse() == null || data.getTag() == AIUITag.LAUNCH) return;
            Timber.tag(TAG).d("cbm semantic content= %s", mGson.toJson(data.getResponse()));
            mSpeechInteraction.updateQuery(new VoiceQuery(null, QueryState.DONE));
            mSpeechInteraction.semanticAnswer(data.getResponse());
        } else if (sub == CBMSub.CBM_TOOL_PK) {
            if (data.getCbmToolPK() == null || data.getCbmToolPK().getText() == null) return;
            Timber.tag(TAG).d("cbm tool pk = %s", data.getCbmToolPK().getText().getPkType());
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
