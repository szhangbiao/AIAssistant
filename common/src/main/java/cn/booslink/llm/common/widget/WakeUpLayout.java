package cn.booslink.llm.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.booslink.llm.common.R;
import cn.booslink.llm.common.utils.ContextUtils;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class WakeUpLayout extends LinearLayout {

    public WakeUpLayout(@ApplicationContext Context context) {
        this(context, null);
    }

    public WakeUpLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WakeUpLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateLayout(context);
        setOrientation(VERTICAL);
        int padding = ContextUtils.dp2px(context, 20);
        setPadding(padding, padding, padding, padding);
    }

    private void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_speech_wakeup, this, true);
    }
}
