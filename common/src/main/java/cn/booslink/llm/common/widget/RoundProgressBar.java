package cn.booslink.llm.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.booslink.llm.common.R;

public class RoundProgressBar extends View {
    
    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private RectF mRectF;
    private LinearGradient mGradient;
    private int mProgress = 0;
    private int mMaxProgress = 100;
    
    // 可自定义的颜色配置
    private int mBackgroundColor = BACKGROUND_COLOR;
    private int mGradientStartColor = GRADIENT_START_COLOR;
    private int mGradientEndColor = GRADIENT_END_COLOR;
    private float mStrokeWidth = STROKE_WIDTH;
    
    // 颜色配置
    private static final int BACKGROUND_COLOR = 0x4DFFFFFF; // #4DFFFFFF
    private static final int GRADIENT_START_COLOR = 0xFF5C57FF; // #5C57FF
    private static final int GRADIENT_END_COLOR = 0xFF3EEDEF; // #3EEDEF
    private static final float STROKE_WIDTH = 5f; // 5dp
    
    public RoundProgressBar(Context context) {
        this(context, null);
    }
    
    public RoundProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public RoundProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
        initParams();
    }
    
    private void initAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar)) {
            mProgress = a.getInt(R.styleable.RoundProgressBar_progress, 0);
            mMaxProgress = a.getInt(R.styleable.RoundProgressBar_max, 100);
            
            // 读取自定义颜色和宽度
            mBackgroundColor = a.getColor(R.styleable.RoundProgressBar_backgroundColor, BACKGROUND_COLOR);
            mGradientStartColor = a.getColor(R.styleable.RoundProgressBar_gradientStartColor, GRADIENT_START_COLOR);
            mGradientEndColor = a.getColor(R.styleable.RoundProgressBar_gradientEndColor, GRADIENT_END_COLOR);
            mStrokeWidth = a.getDimension(R.styleable.RoundProgressBar_strokeWidth, STROKE_WIDTH);
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
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 在尺寸变化时创建渐变对象
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
        int radius = Math.min(width, height) / 2 - (int) (mStrokeWidth / 2);
        // 设置绘制区域
        mRectF.set(width / 2.0f - radius, height / 2.0f - radius, width / 2.0f + radius, height / 2.0f + radius);
        // 绘制背景圆环
        canvas.drawArc(mRectF, 0, 360, false, mBackgroundPaint);
        // 使用已创建的渐变对象
        if (mGradient != null) {
            mProgressPaint.setShader(mGradient);
        }
        // 绘制进度圆环
        float sweepAngle = (float) mProgress / mMaxProgress * 360;
        canvas.drawArc(mRectF, -90, sweepAngle, false, mProgressPaint);
    }
    
    /**
     * 设置进度
     * @param progress 进度值 (0-100)
     */
    public void setProgress(int progress) {
        mProgress = Math.max(0, Math.min(progress, mMaxProgress));
        invalidate();
    }
    
    /**
     * 获取当前进度
     */
    public int getProgress() {
        return mProgress;
    }
    
    /**
     * 设置最大进度
     */
    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
        invalidate();
    }
    
    /**
     * 获取最大进度
     */
    public int getMaxProgress() {
        return mMaxProgress;
    }
}
