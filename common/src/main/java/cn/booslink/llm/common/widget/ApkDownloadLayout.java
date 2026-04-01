package cn.booslink.llm.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.booslink.llm.common.R;

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
}
