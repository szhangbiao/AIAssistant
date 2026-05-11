package cn.booslink.llm.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import cn.booslink.llm.common.utils.ContextUtils;

public class CornerView extends FrameLayout {
    private final Path mPath;
    private final float radius;

    public CornerView(Context context) {
        this(context, null);
    }

    public CornerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPath = new Path();
        radius = ContextUtils.dp2px(context, 16);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        RectF rect = new RectF();
        mPath.reset();
        rect.set(0, 0, w, h);
        mPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        mPath.close();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(mPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}
