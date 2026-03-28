package cn.booslink.llm.speech;

import android.content.Context;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUISetting;

import javax.inject.Inject;

import cn.booslink.llm.common.model.Device;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class SpeechAgentImpl implements ISpeechAgent, AIUIListener {

    private final static String TAG = "SpeechAgent";

    private final Context mContext;
    private AIUIAgent mAIUIAgent = null;

    @Inject
    public SpeechAgentImpl(@ApplicationContext Context context, Device device) {
        this.mContext = context;
        AIUISetting.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, device.sn);
    }

    @Override
    public void createAgent() {
        // TODO WRITE_EXTERNAL_STORAGE 权限
        if (mAIUIAgent == null) {
            mAIUIAgent = AIUIAgent.createAgent(mContext, "", this);
        }
    }

    @Override
    public void onEvent(AIUIEvent aiuiEvent) {

    }

    @Override
    public void destroyAgent() {
        if (null != mAIUIAgent) {
            mAIUIAgent.destroy();
            mAIUIAgent = null;
        }
    }
}
