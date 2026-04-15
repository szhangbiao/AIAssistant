package cn.booslink.llm.common.widget;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.WeatherUI;
import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class AIInteractionLayout extends LinearLayout {

    private TextView tvQuestion;
    private TextView tvResultTitle;
    private ViewGroup flResult;
    private ApkDownloadLayout apkDownloadLayout;
    private WeatherListLayout weatherListLayout;

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
        setupBlurView();
    }

    public void voiceInput(String voiceTxt) {
        if (TextUtils.isEmpty(voiceTxt)) return;
        tvQuestion.setText(voiceTxt);
    }

    public void updateTipTitle(String tipTitle) {
        if (TextUtils.isEmpty(tipTitle)) return;
        tvResultTitle.setText(tipTitle);
    }

    public void nplReply(String nplText) {
        if (TextUtils.isEmpty(nplText)) return;
        tvNplReply.setVisibility(View.VISIBLE);
        weatherListLayout.setVisibility(View.GONE);
        apkDownloadLayout.setVisibility(View.GONE);
        tvNplReply.setText(nplText);
    }

    public void showDownloadProcess(ApkDownload apkDownload) {
        boolean shouldHideDownloadLayout = apkDownload.isEmpty() || apkDownload.isDownloadFail() || apkDownload.isInstallFail() || apkDownload.isInstallFinish();
        weatherListLayout.setVisibility(GONE);
        tvNplReply.setVisibility(shouldHideDownloadLayout ? VISIBLE : GONE);
        apkDownloadLayout.setVisibility(shouldHideDownloadLayout ? GONE : VISIBLE);
        apkDownloadLayout.updateDownloadView(apkDownload);
    }

    public void showWeatherList(WeatherUI weatherData) {
        tvNplReply.setVisibility(GONE);
        apkDownloadLayout.setVisibility(GONE);
        weatherListLayout.setVisibility(VISIBLE);
        weatherListLayout.updateWeatherUI(weatherData);
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_interaction, this, true);
    }

    private void initWidgets() {
        tvQuestion = findViewById(R.id.tv_question);
        tvResultTitle = findViewById(R.id.tv_result_title);
        flResult = findViewById(R.id.fl_result);
        tvNplReply = findViewById(R.id.tv_npl);
        apkDownloadLayout = findViewById(R.id.fl_download_layout);
        weatherListLayout = findViewById(R.id.cl_weather_list);
    }

    private void setupBlurView() {
        BlurView blurView = findViewById(R.id.blurView);
        BlurTarget blurTarget = findViewById(R.id.blurTarget);
        float radius = 20f; // 15f gives good medium blur effect
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            blurView.setClipToOutline(true);
        }
        blurView.setupWith(blurTarget)
                //.setFrameClearDrawable(windowBackground) // Optional. Useful when your root has a lot of transparent background, which results in semi-transparent blurred content. This will make the background opaque
                .setBlurRadius(radius);
    }
}
