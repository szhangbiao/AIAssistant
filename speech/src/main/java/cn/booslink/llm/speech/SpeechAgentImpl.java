package cn.booslink.llm.speech;

import android.content.Context;

import com.google.gson.Gson;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUISetting;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Device;
import cn.booslink.llm.common.utils.RxUtil;
import cn.booslink.llm.speech.config.AIUIConfig;
import cn.booslink.llm.speech.repository.IConfigRepository;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

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
    public void onEvent(AIUIEvent aiuiEvent) {

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
