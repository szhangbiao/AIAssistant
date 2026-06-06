package cn.booslink.llm.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

public class SpeechWindowLayout extends FrameLayout {
    private static final String TAG = "WindowLayout";

    private final ViewTreeObserver.OnWindowFocusChangeListener mWindowFocusListener =
            hasFocus -> Timber.tag(TAG).d("Window focus changed: hasFocus = %b", hasFocus);

    private final ViewTreeObserver.OnGlobalFocusChangeListener mGlobalFocusListener =
            (oldFocus, newFocus) -> Timber.tag(TAG).d("Global focus changed: old = %s, new = %s", oldFocus, newFocus);

    public SpeechWindowLayout(@NonNull Context context) {
        this(context, null);
    }

    public SpeechWindowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeechWindowLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnFocusChangeListener((v, hasFocus) ->
                Timber.tag(TAG).d("mParentView view focus changed: hasFocus = %b", hasFocus)
        );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnWindowFocusChangeListener(mWindowFocusListener);
        getViewTreeObserver().addOnGlobalFocusChangeListener(mGlobalFocusListener);
        Timber.tag(TAG).d("SpeechWindowContainer onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnWindowFocusChangeListener(mWindowFocusListener);
        getViewTreeObserver().removeOnGlobalFocusChangeListener(mGlobalFocusListener);
        Timber.tag(TAG).d("SpeechWindowContainer onDetachedFromWindow");
    }
}
