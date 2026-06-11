package cn.booslink.llm.common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.Locale;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.di.CommonEntryPoint;
import cn.booslink.llm.common.image.ImageLoader;
import cn.booslink.llm.common.model.ApkDownload;
import dagger.hilt.android.EntryPointAccessors;
import timber.log.Timber;

public class ApkDownloadLayout extends RelativeLayout {

    private ImageView ivIcon;
    private TextView tvName;
    private TextView tvStatus;
    private TextView tvLabel;
    private TextView tvCountdown;
    private RoundProgressBar pbProgress;
    private CountDownProgressBar pbDone;
    private TextView tvProgress;

    public ApkDownloadLayout(Context context) {
        this(context, null);
    }

    public ApkDownloadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ApkDownloadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateLayout(context);
        initWidgets();
        initListeners();
    }


    public void updateDownloadView(ApkDownload download) {
        if (download.isEmpty()) {
            resetViews();
            return;
        }
        if (!TextUtils.isEmpty(download.getIcon())) {
            CommonEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(getContext().getApplicationContext(), CommonEntryPoint.class);
            ImageLoader imageLoader = hiltEntryPoint.lazyImageLoader().get();
            if (imageLoader != null) {
                imageLoader.loadImage(ivIcon, download.getIcon());
            }
        } else if (download.getApkIcon() != null) {
            ivIcon.setImageDrawable(download.getApkIcon());
        }
        tvName.setText(download.getName());
        pbDone.setVisibility(download.isDownloadComplete() ? VISIBLE : GONE);
        pbProgress.setVisibility(download.isDownloadComplete() ? GONE : VISIBLE);
        tvProgress.setVisibility(download.isDownloadComplete() ? GONE : VISIBLE);
        if (download.isDownloadComplete()) {
            tvStatus.setText(R.string.download_done);
        } else {
            tvStatus.setText(R.string.downloading);
            pbProgress.setProgress(download.getProgress());
            tvProgress.setText(String.format(Locale.getDefault(), "%d%%", download.getProgress()));
        }
    }

    public void startCountDown(boolean isApkInstalled) {
        tvLabel.setVisibility(VISIBLE);
        tvCountdown.setVisibility(VISIBLE);
        tvLabel.setText(isApkInstalled ? R.string.install_finish : R.string.download_finish);
        tvStatus.setVisibility(GONE);
        pbProgress.setVisibility(GONE);
        pbDone.setVisibility(VISIBLE);
        updateCountDownText(5);
        pbDone.startCountDown();
    }

    public void resetViews() {
        clearAnimation();
        tvName.setText("");
        pbDone.setVisibility(GONE);
        pbDone.cancelCountDown();
        pbProgress.setVisibility(VISIBLE);
        tvProgress.setVisibility(VISIBLE);
        tvLabel.setVisibility(GONE);
        tvCountdown.setVisibility(GONE);
        tvProgress.setText("0%");
        tvStatus.setText(R.string.downloading);
        ivIcon.setImageBitmap(null);
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_apk_download, this, true);
    }

    private void initWidgets() {
        ivIcon = findViewById(R.id.iv_icon);
        tvName = findViewById(R.id.tv_name);
        tvStatus = findViewById(R.id.tv_status);
        tvLabel = findViewById(R.id.tv_label);
        tvCountdown = findViewById(R.id.tv_countdown);
        pbDone = findViewById(R.id.pb_done);
        pbProgress = findViewById(R.id.pb_progress);
        tvProgress = findViewById(R.id.tv_progress);
    }

    private void initListeners() {
        pbDone.setOnCountDownListener(new CountDownProgressBar.OnCountDownListener() {
            @Override
            public void onTick(int second) {
                updateCountDownText(second);
            }

            @Override
            public void onFinish() {
                fadeDownloadLayout();
            }
        });
    }

    private void fadeDownloadLayout() {
        animate().alpha(0.0f)
                .setDuration(1000L)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setVisibility(GONE);
                        updateCountDownText(5);
                        setAlpha(1.0f);
                    }
                })
                .start();
    }

    private void updateCountDownText(int second) {
        Timber.tag("DownloadLayout").d("Countdown second = %d", second);
        String secondStr = String.valueOf(second);
        SpannableStringBuilder builder = new SpannableStringBuilder(secondStr);
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#3EEDEF")), 0, secondStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" 秒后自动消失");
        tvCountdown.setText(builder);
    }
}
