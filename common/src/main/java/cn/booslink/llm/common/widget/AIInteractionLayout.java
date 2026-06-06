package cn.booslink.llm.common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.WeatherUI;
import cn.booslink.llm.common.utils.ContextUtils;

public class AIInteractionLayout extends LinearLayout {

    private TextView tvQuestion;
    private TextView tvResultTitle;
    private LoadingView loadingView;
    private ApkDownloadLayout apkDownloadLayout;
    private ApkDownloadLayout apkDismissLayout;
    private WeatherListLayout weatherListLayout;
    private WakeUpLayout llWakeup;
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
        showLoading(true);
        setPadding(0, 0, 0, ContextUtils.dp2px(context, 24));
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
        showLoading(false);
        weatherListLayout.setVisibility(View.GONE);
        llWakeup.setVisibility(View.GONE);
        tvNplReply.setVisibility(View.VISIBLE);
        tvNplReply.setText(nplText);
    }

    public void showDownloadProcess(ApkDownload apkDownload) {
        boolean shouldHideDownloadLayout = apkDownload.isEmpty() || apkDownload.isDownloadError() || apkDownload.isDownloadFail() || apkDownload.isInstallFail() || apkDownload.isInstallFinish();
        //showLoading(false);
        apkDownloadLayout.setVisibility(shouldHideDownloadLayout ? GONE : VISIBLE);
        apkDownloadLayout.updateDownloadView(apkDownload);
        if (apkDownload.isInstallFinish() || (apkDownload.isDownloadComplete() && apkDownload.isDownloadOnly())) {
            showDismissProcess(apkDownload);
        }
    }

    public void showDismissProcess(ApkDownload apkDownload) {
        apkDismissLayout.setVisibility(View.VISIBLE);
        apkDismissLayout.resetViews();
        apkDismissLayout.updateDownloadView(apkDownload);
        apkDismissLayout.startCountDown(apkDownload.isInstallFinish());
    }

    public void showWeatherList(WeatherUI weatherData) {
        showLoading(false);
        tvNplReply.setVisibility(GONE);
        llWakeup.setVisibility(GONE);
        weatherListLayout.setVisibility(VISIBLE);
        weatherListLayout.updateWeatherUI(weatherData);
    }

    public void showLoading(boolean isShow) {
        if (isShow) {
            tvNplReply.setText("");
            tvNplReply.scrollTo(0, 0);
            tvNplReply.setVisibility(View.VISIBLE);
            weatherListLayout.setVisibility(GONE);
            llWakeup.setVisibility(GONE);
        }
        loadingView.setVisibility(isShow ? VISIBLE : GONE);
    }

    public void showWakeup() {
        llWakeup.setVisibility(View.VISIBLE);
        tvNplReply.setVisibility(View.GONE);
        weatherListLayout.setVisibility(GONE);
        loadingView.setVisibility(View.GONE);
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_interaction, this, true);
    }

    private void initWidgets() {
        tvQuestion = findViewById(R.id.tv_question);
        tvResultTitle = findViewById(R.id.tv_result_title);
        tvNplReply = findViewById(R.id.tv_npl);
        apkDownloadLayout = findViewById(R.id.fl_download_layout);
        apkDismissLayout = findViewById(R.id.fl_download_dismiss);
        weatherListLayout = findViewById(R.id.cl_weather_list);
        loadingView = findViewById(R.id.loadingView);
        llWakeup = findViewById(R.id.ll_wakeup);
        tvNplReply.setMovementMethod(new ScrollingMovementMethod());
    }
}
