package cn.booslink.llm.common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Locale;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.di.CommonEntryPoint;
import cn.booslink.llm.common.image.ImageLoader;
import cn.booslink.llm.common.model.ApkDownload;
import dagger.hilt.android.EntryPointAccessors;

public class ApkDownloadLayout extends RelativeLayout {

    private ImageView ivIcon;
    private TextView tvName;
    private TextView tvStatus;
    private ImageView ivDone;
    private RoundProgressBar pbProgress;
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
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_apk_download, this, true);
    }

    private void initWidgets() {
        ivIcon = findViewById(R.id.iv_icon);
        tvName = findViewById(R.id.tv_name);
        tvStatus = findViewById(R.id.tv_status);
        ivDone = findViewById(R.id.iv_download_done);
        pbProgress = findViewById(R.id.pb_progress);
        tvProgress = findViewById(R.id.tv_progress);
    }

    public void updateDownloadView(ApkDownload download) {
        if (download.isEmpty()) {
            resetViews();
            return;
        }
        if (download.getProgress() == 0 && !TextUtils.isEmpty(download.getIcon())) {
            CommonEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(getContext().getApplicationContext(), CommonEntryPoint.class);
            ImageLoader imageLoader = hiltEntryPoint.imageLoader();
            imageLoader.loadImage(ivIcon, download.getIcon());
        }
        tvName.setText(download.getName());
        ivDone.setVisibility(download.isDownloadComplete() ? VISIBLE : GONE);
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

    private void resetViews() {
        tvName.setText("");
        ivDone.setVisibility(GONE);
        pbProgress.setVisibility(VISIBLE);
        tvProgress.setVisibility(VISIBLE);
        tvProgress.setText("0%");
        tvStatus.setText(R.string.downloading);
        ivIcon.setImageBitmap(null);
    }
}
