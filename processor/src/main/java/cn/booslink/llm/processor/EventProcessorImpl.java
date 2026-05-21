package cn.booslink.llm.processor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIMessage;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Answer;
import cn.booslink.llm.common.model.CBMEvent;
import cn.booslink.llm.common.model.CBMSemantic;
import cn.booslink.llm.common.model.EventData;
import cn.booslink.llm.common.model.EventInfo;
import cn.booslink.llm.common.model.NetworkStatus;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceQuery;
import cn.booslink.llm.common.model.VoiceResult;
import cn.booslink.llm.common.model.enums.AIUIState;
import cn.booslink.llm.common.model.enums.AIUITag;
import cn.booslink.llm.common.model.enums.CBMSub;
import cn.booslink.llm.common.model.enums.QueryState;
import cn.booslink.llm.common.network.NetworkMonitor;
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.storage.ISpeechStorage;
import cn.booslink.llm.common.ui.ISpeechInteraction;
import cn.booslink.llm.common.utils.NetworkUtils;
import cn.booslink.llm.common.utils.RxUtil;
import cn.booslink.llm.downloader.IAppManager;
import cn.booslink.llm.processor.process.IIntentProcess;
import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import timber.log.Timber;

public class EventProcessorImpl implements IEventProcessor {

    private final static String TAG = "EventProcessor";

    private final static String KEY_TAG = "tag";
    private final static String KEY_STREAM_ID = "stream_id";
    private final static String KEY_UID = "uid";

    private final static String TYPE_VAD = "Vad";
    private final static String VAD_BOS = "Bos";
    private final static String VAD_EOS = "Eos";
    private final static String VAD_SILENCE = "Silence";

    private final static int RESULT_NETWORK_ERROR = 10120;
    private final static int RESULT_NETWORK_TIMEOUT = 10114;

    private final Gson mGson;
    private final Handler mHandler;
    private final Context mContext;
    private final IAppManager mAppManager;
    private final StringBuilder mNplBuilder;
    private final ISpeechStorage mSpeechStorage;
    private final IIntentProcess mIntentProcess;
    private final Lazy<ISpeechAgent> mSpeechAgentLazy;
    private final ISpeechInteraction mSpeechInteraction;

    private Disposable mEventDisposable;
    private Disposable mNetworkDisposable;
    private FlowableEmitter<AIUIEvent> mEventEmitter;
    private volatile boolean isSubscriptionActive = false;
    private volatile boolean isDestroyed = false;
    private volatile int mSpeechStatus;

    private EventData mEventData = EventData.Companion.empty();

    @Inject
    public EventProcessorImpl(Gson gson, @ApplicationContext Context context, IIntentProcess intentProcess, IAppManager appManager, ISpeechStorage speechStorage, ISpeechInteraction speechInteraction, NetworkMonitor networkMonitor, Lazy<ISpeechAgent> speechAgentLazy) {
        this.mGson = gson;
        this.mContext = context;
        this.mAppManager = appManager;
        this.mIntentProcess = intentProcess;
        this.mSpeechStorage = speechStorage;
        this.mNplBuilder = new StringBuilder();
        this.mSpeechAgentLazy = speechAgentLazy;
        this.mSpeechInteraction = speechInteraction;
        setupNetworkChangeObservable(networkMonitor);
        this.mHandler = new Handler(Looper.getMainLooper());
        createEventEmitter();
    }

    @Override
    public void processEvent(AIUIEvent event) {
        switch (event.eventType) {
            case AIUIConstant.EVENT_STATE: // 服务状态事件
                mSpeechStatus = event.arg1;
                break;
            case AIUIConstant.EVENT_RESULT: // 结果事件
                //Timber.tag(TAG).d("result = %s", event.info);
                safeEmitEvent(event);
                break;
            case AIUIConstant.EVENT_WAKEUP: // 唤醒事件
                int type = event.arg1; // 0 （语音唤醒）, 1 （发送CMD_WAKEUP手动唤醒）
                Timber.tag(TAG).d("wakeup, type = %d", type);
                mHandler.post(mSpeechInteraction::UIWakeup);
                if (type == 0) {
                    boolean shouldBlockSleepLogic = mAppManager.isPkgDownloading() || mAppManager.isPkgInstalling();
                    if (shouldBlockSleepLogic) {
                        // TODO
                        return;
                    }
                    mSpeechInteraction.updateQuery(new VoiceQuery("Bobo在听，有什么可以帮您~", QueryState.WAKE_UP));
                }
                break;
            case AIUIConstant.EVENT_PRE_SLEEP: // 准备休眠事件
                Timber.tag(TAG).d("prepare sleep");
                break;
            case AIUIConstant.EVENT_SLEEP: // 休眠事件
                int sleepType = event.arg1; // 0 （交互超时,自动休眠）, 1 （发送CMD_RESET_WAKEUP手动休眠）
                Timber.tag(TAG).d("sleep, type = %d", sleepType);
                boolean shouldBlockSleepLogic = mAppManager.isPkgDownloading() || mAppManager.isPkgInstalling();
                boolean isNetworkConnected = NetworkUtils.isConnected(mContext);
                if (sleepType == 0 && shouldBlockSleepLogic && isNetworkConnected) {
                    wakeupNetworkResumeOrDownloadContinue();
                    return;
                }
                if (!isNetworkConnected) return;
                boolean showLeaveConfirm = mSpeechStorage.shouldShowLeaveConfirm();
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
                String uid = event.data.getString(KEY_UID);
                Timber.tag(TAG).d("connected to server, uid = %s", uid);
                break;
            case AIUIConstant.EVENT_SERVER_DISCONNECTED: // 与服务端断开连接
                Timber.tag(TAG).d("disconnect to server");
                break;
            case AIUIConstant.EVENT_ERROR: // 出错事件
                int code = event.arg1;
                Timber.tag(TAG).d("error code = %d, info = %s", code, event.info);
                mSpeechInteraction.semanticAnswer(UIResponse.Companion.empty());
                if (RESULT_NETWORK_ERROR == code || RESULT_NETWORK_TIMEOUT == code) {
                    mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.ERROR));
                    mSpeechInteraction.nlpAnswer("网络出现错误");
                }
                break;
        }
    }

    @Override
    public boolean isProcessActive() {
        return mSpeechStatus == AIUIState.WORKING.getState();
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
        if (mNetworkDisposable != null) {
            mNetworkDisposable.dispose();
        }
        mNetworkDisposable = null;
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
                .compose(RxUtil.flowableOnIO())
                .subscribe(this::populateEventResult, this::parseOrPopulateEventFailed);
    }

    private EventData parseEventData(AIUIEvent event) {
        if (TextUtils.isEmpty(event.info)) return EventData.Companion.empty();
        EventInfo eventInfo = mGson.fromJson(event.info, EventInfo.class);
        CBMSub sub = eventInfo.getSub();
        String cntId = eventInfo.getCntId();
        if (sub == null || TextUtils.isEmpty(cntId)) return EventData.Companion.empty();
        //Timber.tag(TAG).d("parseEventData, sub = %s", sub);
        try {
            byte[] bytes = event.data.getByteArray(cntId);
            String cntJsonRaw = new String(bytes, StandardCharsets.UTF_8);
            String tag = event.data.getString(KEY_TAG);
            String streamId = event.data.getString(KEY_STREAM_ID);
            Timber.tag(TAG).d("parseEventData, cnt json = %s", cntJsonRaw);
            EventData data = mGson.fromJson(cntJsonRaw, EventData.class);
            data.setId(streamId);
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
                mEventData.setSemanticHandled(!response.isWeatherEmpty());
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
            if (data.getNlp() == null || (!TextUtils.isEmpty(mEventData.getId()) && !mEventData.getId().equals(data.getId()))) return;
            int status = data.getNlp().getStatus() != null ? data.getNlp().getStatus() : -1;
            switch (status) {
                case 0:
                    Timber.tag(TAG).d("nlp, start");
                    mNplBuilder.delete(0, mNplBuilder.length());
                    break;
                case 1:
                    mNplBuilder.append(data.getNlp().getText());
                    Timber.tag(TAG).d("nlp, content = %s, semanticHandled = %b", data.getNlp().getText(), mEventData.getSemanticHandled());
                    if (data.getTag() != AIUITag.LAUNCH && mEventData.getSemanticHandled()) return;
                    mSpeechInteraction.nlpAnswer(mNplBuilder.toString());
                    if (data.getTag() == AIUITag.LAUNCH) return;
                    mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.DONE));
                    break;
                case 2:
                    String nplContent = mNplBuilder.toString();
                    Timber.tag(TAG).d("nlp, content = %s, semanticHandled = %b", nplContent, mEventData.getSemanticHandled());
                    mNplBuilder.delete(0, mNplBuilder.length());
                    mEventData = mEventData.copyNlp(data.getNlp());
                    if (data.getTag() != AIUITag.LAUNCH && mEventData.getSemanticHandled()) return;
                    mSpeechInteraction.nlpAnswer(nplContent);
                    if (data.getTag() == AIUITag.LAUNCH || TextUtils.isEmpty(nplContent)) return;
                    mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.DONE));
                    break;
            }
        } else if (sub == CBMSub.CBM_TIDY) {
            if (data.getCbmTidy() == null || data.getCbmTidy().getText() == null || data.getTag() == AIUITag.LAUNCH) return;
            Timber.tag(TAG).d("cbm tidy, query = %s", data.getCbmTidy().getText().getQuery());
            mEventData = mEventData.copyTidy(data.getCbmTidy());
            mSpeechInteraction.updateQuery(new VoiceQuery(data.getCbmTidy().getText().getQuery(), QueryState.QUERYING));
        } else if (sub == CBMSub.CBM_SEMANTIC) {
            if (data.getResponse() == null || data.getTag() == AIUITag.LAUNCH) return;
            mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.DONE));
            mSpeechInteraction.semanticAnswer(data.getResponse());
            if (data.getCbmSemantic() == null) return;
            mEventData = mEventData.copySemantic(data.getCbmSemantic(), data.getResponse());
            CBMSemantic cbmSemantic = data.getCbmSemantic().getText();
            if (cbmSemantic != null && cbmSemantic.getSemantic() != null) {
                VoiceResult voiceResult = mIntentProcess.processIntent(data.getResponse().getCategory(), cbmSemantic.getSemantic());
                if (data.getResponse().isWeatherEmpty()) {
                    mEventData.setSemanticHandled(voiceResult.getHandled());
                }
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
            String type = event.getType();
            String key = event.getKey();
            if (TYPE_VAD.equals(type) && (VAD_SILENCE.equals(key) || VAD_EOS.equals(key))) {
                mEventData = EventData.Companion.withId(data.getId());
                if (mEventData.getCbmSemantic() == null || mEventData.getCbmSemantic().getText() == null) return;
                Answer answer = mEventData.getCbmSemantic().getText().getAnswer();
                String semanticAnswer = answer != null ? answer.getText() : "";
                if (data.getTag() == AIUITag.LAUNCH && TextUtils.isEmpty(mNplBuilder.toString()) && !TextUtils.isEmpty(semanticAnswer)) {
                    mSpeechInteraction.nlpAnswer(semanticAnswer);
                }
            } else if (TYPE_VAD.equals(type) && VAD_BOS.equals(key)) {
                mEventData = EventData.Companion.withId(data.getId());
                if (mEventData.getCbmSemantic() == null || mEventData.getCbmSemantic().getText() == null) return;
                Answer answer = mEventData.getCbmSemantic().getText().getAnswer();
                String semanticAnswer = answer != null ? answer.getText() : "";
                if (data.getTag() == AIUITag.LAUNCH && TextUtils.isEmpty(mNplBuilder.toString()) && !TextUtils.isEmpty(semanticAnswer)) {
                    mSpeechInteraction.nlpAnswer(semanticAnswer);
                }
            }
            Timber.tag(TAG).d("event type = %s, key = %s, event id = %s", type, key, data.getId());
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

    private void setupNetworkChangeObservable(NetworkMonitor networkMonitor) {
        mNetworkDisposable = networkMonitor.getNetworkObservable()
                .distinctUntilChanged()
                .compose(RxUtil.observableOnMain())
                .subscribe(networkStatus -> {
                    Timber.tag(TAG).d("Network changed, status = %s", networkStatus);
                    ISpeechAgent speechAgent = mSpeechAgentLazy.get();
                    if (speechAgent == null || !speechAgent.isAIUIReady()) return;
                    boolean isConnect = networkStatus == NetworkStatus.CONNECTED;
                    if (isConnect) {
                        mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.DONE));
                        mSpeechInteraction.nlpAnswer("网络恢复了");
                        wakeupNetworkResumeOrDownloadContinue();
                    } else {
                        mSpeechInteraction.updateQuery(VoiceQuery.Companion.stateOnly(QueryState.ERROR));
                        mSpeechInteraction.nlpAnswer("网络出现错误");
                    }
                });
    }

    private void wakeupNetworkResumeOrDownloadContinue() {
        ISpeechAgent speechAgent = mSpeechAgentLazy.get();
        if (speechAgent == null) return;
        speechAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, null, null));
    }
}
