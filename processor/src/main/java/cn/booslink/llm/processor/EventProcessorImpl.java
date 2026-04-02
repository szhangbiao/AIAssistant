package cn.booslink.llm.processor;

import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import cn.booslink.llm.common.model.EventInfo;
import cn.booslink.llm.common.model.enums.CBMSub;
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

    private Disposable mEventDisposable;
    private FlowableEmitter<AIUIEvent> mEventEmitter;
    private volatile boolean isSubscriptionActive = false;
    private volatile boolean isDestroyed = false;

    @Inject
    public EventProcessorImpl(Gson gson) {
        this.mGson = gson;
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
                safeEmitEvent(event);
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
                .compose(RxUtil.flowableOnMain())
                .subscribe(this::populateEventResult, this::parseOrPopulateEventFailed);
    }

    private AIUIEvent parseEventData(AIUIEvent event) {
        Timber.tag(TAG).d("parseEventData");
        EventInfo eventInfo = mGson.fromJson(event.info, EventInfo.class);
        CBMSub sub = eventInfo.getSub();
        if (sub == CBMSub.IAT) {
            populateIATStream(eventInfo.getCntId(), event.data);
        }
        return event;
    }

    private void populateEventResult(AIUIEvent aiuiEvent) {
        // TODO 事件分发
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
        // 创建新订阅
        createEventEmitter();
    }

    private void parseOrPopulateEventFailed(Throwable throwable) {
        Timber.tag(TAG).e(throwable, "parseOrPopulateEventFailed");
        isSubscriptionActive = false;
        if (!isDestroyed) {
            recreateSubscription();
        }
    }

    private void populateIATStream(@Nullable String cntId, Bundle data) {
        byte[] bytes = data.getByteArray(cntId);
        try {
            String cntJsonRaw = new String(bytes, StandardCharsets.UTF_8);
            Timber.tag(TAG).d("populateIATStream, json = %s", cntJsonRaw);
            JSONObject cntJson = new JSONObject(cntJsonRaw);
            JSONObject result = cntJson.optJSONObject("text");
            if (result.length() >= 2) {
                updateIATPGS(cntJson);
            }
        } catch (JSONException e) {
            Timber.tag(TAG).e(e, "populateIATStream");
        }
    }

    // 处理听写PGS的队列
    private String[] mIATPGSStack = new String[50];

    private void updateIATPGS(JSONObject cntJson) {
        JSONObject text = cntJson.optJSONObject("text");
        // 解析拼接此次听写结果
        StringBuilder iatText = new StringBuilder();
        JSONArray words = text.optJSONArray("ws");
        boolean lastResult = text.optBoolean("ls");
        for (int index = 0; index < words.length(); index++) {
            JSONArray charWord = words.optJSONObject(index).optJSONArray("cw");
            for (int cIndex = 0; cIndex < charWord.length(); cIndex++) {
                iatText.append(charWord.optJSONObject(cIndex).opt("w"));
            }
        }
        String voiceIAT;
        String pgsMode = text.optString("pgs");
        if (TextUtils.isEmpty(pgsMode)) {
        } else {
            int serialNumber = text.optInt("sn");
            mIATPGSStack[serialNumber] = iatText.toString();
            //pgs结果两种模式rpl和apd模式（替换和追加模式）
            if ("rpl".equals(pgsMode)) {
                //根据replace指定的range，清空stack中对应位置值
                JSONArray replaceRange = text.optJSONArray("rg");
                try {
                    int start = replaceRange.getInt(0);
                    int end = replaceRange.getInt(1);
                    for (int index = start; index <= end; index++) {
                        mIATPGSStack[index] = null;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            StringBuilder PGSResult = new StringBuilder();
            //汇总stack经过操作后的剩余的有效结果信息
            for (int index = 0; index < mIATPGSStack.length; index++) {
                if (TextUtils.isEmpty(mIATPGSStack[index])) continue;
                if (!TextUtils.isEmpty(PGSResult.toString())) PGSResult.append("\n");
                PGSResult.append(mIATPGSStack[index]);
                //如果是最后一条听写结果，则清空stack便于下次使用
                if (lastResult) {
                    mIATPGSStack[index] = null;
                }
            }
            voiceIAT = PGSResult.toString();
            Timber.tag(TAG).d("IAT voice = %s", voiceIAT);
        }
    }
}
