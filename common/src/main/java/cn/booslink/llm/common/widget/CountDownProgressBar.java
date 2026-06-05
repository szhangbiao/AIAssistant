package cn.booslink.llm.common.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import cn.booslink.llm.common.R;

public class CountDownProgressBar extends View {

    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private RectF mRectF;
    private LinearGradient mGradient;
    private Drawable mDoneDrawable;

    // 可自定义的颜色配置
    private int mBackgroundColor = BACKGROUND_COLOR;
    private int mGradientStartColor = GRADIENT_START_COLOR;
    private int mGradientEndColor = GRADIENT_END_COLOR;
    private float mStrokeWidth = STROKE_WIDTH;

    // 颜色和宽度配置，与 RoundProgressBar 保持一致
    private static final int BACKGROUND_COLOR = 0x4DFFFFFF; // #4DFFFFFF
    private static final int GRADIENT_START_COLOR = 0xFF5C57FF; // #5C57FF
    private static final int GRADIENT_END_COLOR = 0xFF3EEDEF; // #3EEDEF
    private static final float STROKE_WIDTH = 5f; // 5dp

    // 倒计时长 5 秒
    private static final long DEFAULT_DURATION_MS = 5000L;
    private ValueAnimator mAnimator;
    private float mProgressFraction = 1.0f; // 1.0 -> 0.0

    private OnCountDownListener mListener;

    public interface OnCountDownListener {
        void onTick(int second);

        void onFinish();
    }

    public CountDownProgressBar(Context context) {
        this(context, null);
    }

    public CountDownProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
        initParams();
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CountDownProgressBar)) {
            mBackgroundColor = a.getColor(R.styleable.CountDownProgressBar_backgroundColor, BACKGROUND_COLOR);
            mGradientStartColor = a.getColor(R.styleable.CountDownProgressBar_gradientStartColor, GRADIENT_START_COLOR);
            mGradientEndColor = a.getColor(R.styleable.CountDownProgressBar_gradientEndColor, GRADIENT_END_COLOR);
            mStrokeWidth = a.getDimension(R.styleable.CountDownProgressBar_strokeWidth, STROKE_WIDTH);
        }
    }

    private void initParams() {
        // 初始化背景画笔
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mStrokeWidth);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        // 初始化进度画笔
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectF = new RectF();
        mGradient = null; // 将在onSizeChanged中创建

        // 加载完成状态的勾选图片
        mDoneDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_download_done);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mGradient != null) {
            mGradient = null;
        }
        mGradient = new LinearGradient(0, 0, w, h, mGradientStartColor, mGradientEndColor, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int usableWidth = width - paddingLeft - paddingRight;
        int usableHeight = height - paddingTop - paddingBottom;

        float centerX = paddingLeft + usableWidth / 2.0f;
        float centerY = paddingTop + usableHeight / 2.0f;

        float radius = (Math.min(usableWidth, usableHeight) - mStrokeWidth) / 2.0f - 1.0f;
        if (radius < 0) {
            radius = 0;
        }

        // 设置绘制区域
        mRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // 绘制背景圆环
        canvas.drawArc(mRectF, 0, 360, false, mBackgroundPaint);

        // 绘制进度圆环（渐变色）
        if (mGradient != null) {
            mProgressPaint.setShader(mGradient);
        }
        float startAngle = -90f + (1.0f - mProgressFraction) * 360f;
        float sweepAngle = mProgressFraction * 360f;
        canvas.drawArc(mRectF, startAngle, sweepAngle, false, mProgressPaint);

        // 绘制中心勾选图标
        if (mDoneDrawable != null) {
            int maxInnerSize = (int) (radius * 2.0f - mStrokeWidth);
            int drawableWidth = mDoneDrawable.getIntrinsicWidth();
            int drawableHeight = mDoneDrawable.getIntrinsicHeight();
            if (drawableWidth > maxInnerSize || drawableHeight > maxInnerSize) {
                float scale = Math.min((float) maxInnerSize / drawableWidth, (float) maxInnerSize / drawableHeight);
                drawableWidth = (int) (drawableWidth * scale);
                drawableHeight = (int) (drawableHeight * scale);
            }

            int left = (int) (centerX - drawableWidth / 2.0f);
            int top = (int) (centerY - drawableHeight / 2.0f);
            int right = left + drawableWidth;
            int bottom = top + drawableHeight;

            mDoneDrawable.setBounds(left, top, right, bottom);
            mDoneDrawable.draw(canvas);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelCountDown();
    }

    public void setOnCountDownListener(OnCountDownListener listener) {
        this.mListener = listener;
    }

    public void startCountDown() {
        startCountDown(DEFAULT_DURATION_MS);
    }

    public void startCountDown(final long durationMs) {
        cancelCountDown();
        mProgressFraction = 1.0f;
        mAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        mAnimator.setDuration(durationMs);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private int lastSeconds = (int) Math.ceil(durationMs / 1000.0f);

            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                mProgressFraction = (float) animation.getAnimatedValue();
                invalidate();
                if (mListener != null) {
                    long elapsed = (long) (animation.getAnimatedFraction() * durationMs);
                    long remainingMs = durationMs - elapsed;
                    int seconds = (int) Math.ceil(remainingMs / 1000.0f);
                    if (seconds < lastSeconds) {
                        lastSeconds = seconds;
                        if (seconds > 0) {
                            mListener.onTick(seconds);
                        }
                    }
                }
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressFraction = 0.0f;
                invalidate();
                if (mListener != null) {
                    mListener.onFinish();
                }
            }
        });
        mAnimator.start();
    }

    public void cancelCountDown() {
        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.cancel();
            mAnimator = null;
        }
    }
}
