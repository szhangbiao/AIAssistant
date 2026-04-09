package cn.booslink.llm.common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.model.ApkDownload;

public class AIInteractionLayout extends LinearLayout {

    private TextView tvQuestion;
    private TextView tvResultTitle;
    private FrameLayout flResult;
    private ApkDownloadLayout apkDownloadLayout;

    private TextView tvNplReply;

    public AIInteractionLayout(@NonNull Context context) {
        this(context, null);
    }

    public AIInteractionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AIInteractionLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);
        inflateLayout(context);
        initWidgets();
    }

    public void voiceInput(String voiceTxt) {
        if (TextUtils.isEmpty(voiceTxt)) return;
        tvQuestion.setText(voiceTxt);
    }

    public void nplReply(String nplText) {
        if (TextUtils.isEmpty(nplText)) return;
        tvNplReply.setVisibility(View.VISIBLE);
        tvNplReply.setText(nplText);
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_interaction, this, true);
    }

    private void initWidgets() {
        tvQuestion = findViewById(R.id.tv_question);
        tvResultTitle = findViewById(R.id.tv_result_title);
        flResult = findViewById(R.id.fl_result);
        tvNplReply = findViewById(R.id.tv_npl);
    }

    public void showDownloadProcess(ApkDownload apkDownload) {
        boolean shouldHideDownloadLayout = apkDownload.isEmpty() || apkDownload.isDownloadFail() || apkDownload.isInstallFail() || apkDownload.isInstallFinish();
        tvNplReply.setVisibility(shouldHideDownloadLayout ? VISIBLE : GONE);
        apkDownloadLayout.setVisibility(shouldHideDownloadLayout ? GONE : VISIBLE);
        apkDownloadLayout.updateDownloadView(apkDownload);
    }
}
