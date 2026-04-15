package cn.booslink.llm.common.ui;

import android.app.Activity;

import org.jetbrains.annotations.Nullable;

import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.UIResponse;
import cn.booslink.llm.common.model.VoiceQuery;

public interface ISpeechInteraction {

    void attachToWindow();

    void detachFromWindow();

    void attachToActivity(Activity activity);

    void detachFromActivity(Activity activity);

    void destroyView();

    void updateQuery(VoiceQuery query);

    void nlpAnswer(String nlpReply);

    void semanticAnswer(UIResponse response);

    void downloadUpdate(ApkDownload download);
}
