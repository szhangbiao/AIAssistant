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

import org.libpag.PAGFile;
import org.libpag.PAGView;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.di.CommonEntryPoint;
import cn.booslink.llm.common.loader.IPAGLoader;
import cn.booslink.llm.common.model.ApkDownload;
import cn.booslink.llm.common.model.WeatherUI;
import cn.booslink.llm.common.speech.ISpeechAgent;
import cn.booslink.llm.common.utils.ContextUtils;
import dagger.Lazy;
import dagger.hilt.android.EntryPointAccessors;
import timber.log.Timber;

public class AIInteractionLayout extends LinearLayout {

    private final static String TAG = "InteractionLayout";

    private final static String LOADING_NAME = "pag_loading.pag";
    private TextView tvQuestion;
    private TextView tvResultTitle;
    private LoadingView loadingView;
    private ApkDownloadLayout apkDownloadLayout;
    private ApkDownloadLayout apkDismissLayout;
    private WeatherListLayout weatherListLayout;

    private Lazy<ISpeechAgent> mSpeechAgentLazy;

    private PAGView pagLoading;
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
        inflateLayout(context);
        initWidgets();
        showTextLoading(true);
        initializeDependencies(context);
        setPadding(0, 0, 0, ContextUtils.dp2px(context, 24));
        setOrientation(VERTICAL);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pagLoading != null) {
            pagLoading.pause();
            pagLoading.setComposition(null); // 切断底层强引用
            pagLoading.freeCache();
        }
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
        showTextLoading(false);
        weatherListLayout.setVisibility(View.GONE);
        llWakeup.setVisibility(View.GONE);
        tvNplReply.setVisibility(View.VISIBLE);
        tvNplReply.setText(nplText);
    }

    public void showDownloadProcess(ApkDownload apkDownload) {
        boolean shouldHideDownloadLayout = apkDownload.isEmpty() || apkDownload.shouldNotDebounce();
        //showLoading(false);
        Timber.tag(TAG).d("showDownloadProcess, name = %s, layout visible = %b", apkDownload.getName(), !shouldHideDownloadLayout);
        apkDownloadLayout.setVisibility(shouldHideDownloadLayout ? GONE : VISIBLE);
        apkDownloadLayout.updateDownloadView(apkDownload);
        if (apkDownload.isInstallFinish() || (apkDownload.isDownloadComplete() && apkDownload.isDownloadOnly())) {
            showDismissProcess(apkDownload);
        }
    }

    public void showDismissProcess(ApkDownload apkDownload) {
        ISpeechAgent speechAgent = mSpeechAgentLazy.get();
        if (speechAgent == null || !speechAgent.isAIUIWorking()) return;
        apkDismissLayout.setVisibility(View.VISIBLE);
        apkDismissLayout.resetViews();
        apkDismissLayout.updateDownloadView(apkDownload);
        apkDismissLayout.startCountDown(apkDownload.isInstallFinish());
    }

    public void showWeatherList(WeatherUI weatherData) {
        showTextLoading(false);
        tvNplReply.setVisibility(GONE);
        llWakeup.setVisibility(GONE);
        weatherListLayout.setVisibility(VISIBLE);
        weatherListLayout.updateWeatherUI(weatherData);
    }

    public void showTextLoading(boolean isShow) {
        if (isShow) {
            tvNplReply.setText("");
            tvNplReply.scrollTo(0, 0);
            tvNplReply.setVisibility(View.VISIBLE);
            weatherListLayout.setVisibility(GONE);
            llWakeup.setVisibility(GONE);
        }
        loadingView.setVisibility(isShow ? VISIBLE : GONE);
    }

    public void initializeDependencies(Context context) {
        if (mSpeechAgentLazy == null) {
            CommonEntryPoint entryPoint = EntryPointAccessors.fromApplication(context.getApplicationContext(), CommonEntryPoint.class);
            mSpeechAgentLazy = entryPoint.lazySpeechAgent();
        }
    }

    private boolean isLoadingAnimSet = false;

    public void showAnimLoading(boolean isQuerying) {
        if (pagLoading != null) {
            pagLoading.setVisibility(isQuerying ? View.VISIBLE : View.GONE);
            if (isQuerying) {
                if (isLoadingAnimSet) {
                    pagLoading.play();
                } else {
                    IPAGLoader pagLoader = getPagLoader();
                    PAGFile loadingFile = pagLoader != null ? pagLoader.getPagFile(LOADING_NAME) : null;
                    if (loadingFile != null) {
                        pagLoading.setComposition(loadingFile);
                    } else {
                        pagLoading.setPathAsync("assets://" + LOADING_NAME, pagFile -> {
                            if (pagLoader != null) {
                                pagLoader.putPagFile(LOADING_NAME, pagFile);
                            }
                        });
                    }
                    isLoadingAnimSet = true;
                    pagLoading.play();
                }
            } else {
                pagLoading.pause();
            }
        }
    }

    private IPAGLoader mPagLoader;

    private IPAGLoader getPagLoader() {
        if (mPagLoader == null) {
            CommonEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(getContext().getApplicationContext(), CommonEntryPoint.class);
            mPagLoader = hiltEntryPoint.lazyPAGLoader().get();
        }
        return mPagLoader;
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
        pagLoading = findViewById(R.id.pag_loading);
        apkDownloadLayout = findViewById(R.id.fl_download_layout);
        apkDismissLayout = findViewById(R.id.fl_download_dismiss);
        weatherListLayout = findViewById(R.id.cl_weather_list);
        loadingView = findViewById(R.id.loadingView);
        llWakeup = findViewById(R.id.ll_wakeup);
        tvNplReply.setMovementMethod(new ScrollingMovementMethod());
        tvNplReply.setFocusable(false);
        tvNplReply.setFocusableInTouchMode(false);
        if (pagLoading != null) {
            pagLoading.setRepeatCount(-1);
            pagLoading.setMaxFrameRate(30f);
        }
    }

    public void stopAnimIfNeed() {
        if (pagLoading != null && pagLoading.getVisibility() == View.VISIBLE) {
            pagLoading.setVisibility(View.GONE);
            pagLoading.pause();
            pagLoading.freeCache();
        }
    }

    public void resetViews() {
        tvNplReply.scrollTo(0, 0);
        tvNplReply.setText("");
        tvNplReply.setVisibility(View.VISIBLE);
        weatherListLayout.setVisibility(GONE);
        llWakeup.setVisibility(GONE);
        loadingView.setVisibility(View.GONE);
        apkDownloadLayout.resetViews();
        apkDownloadLayout.setVisibility(View.GONE);
        apkDismissLayout.resetViews();
        apkDismissLayout.setVisibility(View.GONE);
    }
}
