package cn.booslink.llm.common.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.booslink.llm.common.R;

public class LoadingView extends View {

    private static final int DOT_COUNT = 3;
    private static final float ENLARGED_THRESHOLD = 1.1f;
    private static final int DEFAULT_DOT_RADIUS_DP = 5;
    private static final int DEFAULT_DOT_SPACING_DP = 8;
    private static final int DEFAULT_ANIMATION_DURATION = 800;
    private static final float DEFAULT_ENLARGED_SCALE = 2.0f;

    private Paint mPaint;
    private float mDotRadius;
    private float mDotSpacing;
    private int mNormalColor;
    private int mEnlargedColor;
    private float mEnlargedRadiusScale;
    private int mAnimationDuration;

    private List<Float> mCurrentRadii;
    private List<AnimatorSet> mAnimatorSets;
    private boolean isAnimating = false;

    // Constructors
    public LoadingView(@NonNull Context context) {
        this(context, null);
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        initPaint();
        initDefaultValues();
        loadAttributes(context, attrs);
        initRadii();
        setupAnimation();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void initDefaultValues() {
        mCurrentRadii = new ArrayList<>();
        mAnimatorSets = new ArrayList<>();
        mDotRadius = dpToPx(DEFAULT_DOT_RADIUS_DP);
        mDotSpacing = dpToPx(DEFAULT_DOT_SPACING_DP);
        mNormalColor = Color.WHITE;
        mEnlargedColor = Color.WHITE;
        mEnlargedRadiusScale = DEFAULT_ENLARGED_SCALE;
        mAnimationDuration = DEFAULT_ANIMATION_DURATION;
    }

    private void loadAttributes(Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LoadingView, 0, 0);
        try {
            mDotRadius = a.getDimension(R.styleable.LoadingView_dotRadius, mDotRadius);
            mDotSpacing = a.getDimension(R.styleable.LoadingView_dotSpacing, mDotSpacing);
            mNormalColor = a.getColor(R.styleable.LoadingView_normalColor, mNormalColor);
            mEnlargedColor = a.getColor(R.styleable.LoadingView_enlargedColor, mEnlargedColor);
            mEnlargedRadiusScale = a.getFloat(R.styleable.LoadingView_enlargedRadiusScale, mEnlargedRadiusScale);
            mAnimationDuration = a.getInteger(R.styleable.LoadingView_animationDuration, mAnimationDuration);
        } finally {
            a.recycle();
        }
    }

    private void initRadii() {
        for (int i = 0; i < DOT_COUNT; i++) {
            mCurrentRadii.add(mDotRadius);
        }
    }

    private void setupAnimation() {
        clearAnimations();
        createDotAnimations();
    }

    private void clearAnimations() {
        mAnimatorSets.clear();
    }

    private void createDotAnimations() {
        AnimatorSet mainAnimator = new AnimatorSet();
        List<AnimatorSet> dotAnimators = new ArrayList<>();

        // Create animation for each dot
        for (int i = 0; i < DOT_COUNT; i++) {
            AnimatorSet dotAnimator = createDotAnimation(i);
            dotAnimators.add(dotAnimator);
        }

        // Play animations with overlap for wave effect
        // Each dot starts when previous dot is halfway through its animation
        long delayBetweenDots = mAnimationDuration / 2; // Start delay between dots

        dotAnimators.get(0).setStartDelay(0);
        dotAnimators.get(1).setStartDelay(delayBetweenDots);
        dotAnimators.get(2).setStartDelay(delayBetweenDots * 2);

        mainAnimator.playTogether(dotAnimators.get(0), dotAnimators.get(1), dotAnimators.get(2));

        // Create infinite loop
        mainAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isAnimating) {
                    mainAnimator.start();
                }
            }
        });

        mAnimatorSets.add(mainAnimator);
    }

    private AnimatorSet createDotAnimation(int dotIndex) {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator scaleUp = createScaleUpAnimation(dotIndex);
        ValueAnimator scaleDown = createScaleDownAnimation(dotIndex);
        animatorSet.playSequentially(scaleUp, scaleDown);
        return animatorSet;
    }

    private ValueAnimator createScaleUpAnimation(int dotIndex) {
        ValueAnimator scaleUp = ValueAnimator.ofFloat(mDotRadius, mDotRadius * mEnlargedRadiusScale);
        scaleUp.setDuration(mAnimationDuration / 2);
        scaleUp.setInterpolator(new LinearInterpolator());
        scaleUp.addUpdateListener(animation -> updateDotRadius(dotIndex, animation));
        return scaleUp;
    }

    private ValueAnimator createScaleDownAnimation(int dotIndex) {
        ValueAnimator scaleDown = ValueAnimator.ofFloat(mDotRadius * mEnlargedRadiusScale, mDotRadius);
        scaleDown.setDuration(mAnimationDuration / 2);
        scaleDown.setInterpolator(new LinearInterpolator());
        scaleDown.addUpdateListener(animation -> updateDotRadius(dotIndex, animation));
        return scaleDown;
    }

    private void updateDotRadius(int dotIndex, ValueAnimator animation) {
        if (isAnimating) {
            mCurrentRadii.set(dotIndex, (Float) animation.getAnimatedValue());
            invalidate();
        }
    }

    // View measurement and drawing
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = calculateWidth();
        int height = calculateHeight();

        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    private int calculateWidth() {
        return (int) (mDotRadius * 2 * mEnlargedRadiusScale * DOT_COUNT + mDotSpacing * (DOT_COUNT - 1));
    }

    private int calculateHeight() {
        return (int) (mDotRadius * 2 * mEnlargedRadiusScale);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawDots(canvas);
    }

    private void drawDots(Canvas canvas) {
        float centerY = getHeight() / 2f;
        float startX = calculateStartX();

        for (int i = 0; i < DOT_COUNT; i++) {
            drawDot(canvas, i, startX, centerY);
        }
    }

    private float calculateStartX() {
        float totalWidth = mDotRadius * 2 * mEnlargedRadiusScale * DOT_COUNT + mDotSpacing * (DOT_COUNT - 1);
        return (getWidth() - totalWidth) / 2f;
    }

    private void drawDot(Canvas canvas, int dotIndex, float startX, float centerY) {
        float currentRadius = mCurrentRadii.get(dotIndex);
        float x = startX + dotIndex * (mDotRadius * 2 * mEnlargedRadiusScale + mDotSpacing) + mDotRadius * mEnlargedRadiusScale;

        mPaint.setColor(getDotColor(currentRadius));
        canvas.drawCircle(x, centerY, currentRadius, mPaint);
    }

    private int getDotColor(float currentRadius) {
        boolean isEnlarged = currentRadius > mDotRadius * ENLARGED_THRESHOLD;
        return isEnlarged ? mEnlargedColor : mNormalColor;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        play();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    // Animation control methods
    public void play() {
        if (!isAnimating) {
            isAnimating = true;
            startAllAnimations();
        }
    }

    public void stop() {
        isAnimating = false;
        cancelAllAnimations();
        resetAllDots();
    }

    private void startAllAnimations() {
        for (AnimatorSet animatorSet : mAnimatorSets) {
            animatorSet.start();
        }
    }

    private void cancelAllAnimations() {
        for (AnimatorSet animatorSet : mAnimatorSets) {
            animatorSet.cancel();
        }
    }

    private void resetAllDots() {
        for (int i = 0; i < DOT_COUNT; i++) {
            mCurrentRadii.set(i, mDotRadius);
        }
        invalidate();
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void setDotRadius(float radius) {
        mDotRadius = radius;
        setupAnimation();
        requestLayout();
    }

    public void setDotSpacing(float spacing) {
        mDotSpacing = spacing;
        requestLayout();
    }

    public void setNormalColor(int color) {
        mNormalColor = color;
        invalidate();
    }

    public void setEnlargedColor(int color) {
        mEnlargedColor = color;
        invalidate();
    }

    public void setEnlargedRadiusScale(float scale) {
        mEnlargedRadiusScale = scale;
        setupAnimation();
        requestLayout();
    }

    public void setAnimationDuration(int duration) {
        mAnimationDuration = duration;
        setupAnimation();
    }

    public boolean isAnimating() {
        return isAnimating;
    }
}
