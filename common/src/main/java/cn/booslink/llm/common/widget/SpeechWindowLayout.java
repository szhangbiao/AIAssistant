package cn.booslink.llm.common.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SpeechWindowLayout extends FrameLayout {
    private static final String TAG = "WindowLayout";

    private final ViewTreeObserver.OnWindowFocusChangeListener mWindowFocusListener =
            hasFocus -> Timber.tag(TAG).d("Window focus changed: hasFocus = %b", hasFocus);

    private final ViewTreeObserver.OnGlobalFocusChangeListener mGlobalFocusListener =
            (oldFocus, newFocus) -> Timber.tag(TAG).d("Global focus changed: old = %s, new = %s", oldFocus, newFocus);

    private final List<View> mTouchableViews = new ArrayList<>();
    private Object mInsetsListener;

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

    /**
     * 添加需要拦截触摸事件的View
     */
    public void addTouchableView(View view) {
        if (view != null && !mTouchableViews.contains(view)) {
            mTouchableViews.add(view);
        }
    }

    public void clearTouchableViews() {
        mTouchableViews.clear();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnWindowFocusChangeListener(mWindowFocusListener);
        getViewTreeObserver().addOnGlobalFocusChangeListener(mGlobalFocusListener);
        attachInsetsListener();
        Timber.tag(TAG).d("SpeechWindowContainer onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnWindowFocusChangeListener(mWindowFocusListener);
        getViewTreeObserver().removeOnGlobalFocusChangeListener(mGlobalFocusListener);
        detachInsetsListener();
        Timber.tag(TAG).d("SpeechWindowContainer onDetachedFromWindow");
    }

    private void attachInsetsListener() {
        if (mInsetsListener != null) return;
        try {
            Class<?> listenerClass = Class.forName("android.view.ViewTreeObserver$OnComputeInternalInsetsListener");
            mInsetsListener = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[]{listenerClass}, new java.lang.reflect.InvocationHandler() {
                        private Field touchableInsetsField = null;
                        private Field touchableRegionField = null;

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            String methodName = method.getName();
                            if ("onComputeInternalInsets".equals(methodName)) {
                                Object info = args[0];
                                try {
                                    if (touchableInsetsField == null || touchableRegionField == null) {
                                        for (Field f : info.getClass().getDeclaredFields()) {
                                            f.setAccessible(true);
                                            String name = f.getName();
                                            if (f.getType() == Region.class) {
                                                touchableRegionField = f;
                                            } else if (name.equals("setTouchableInsets") || name.equals("touchableInsets") || name.equals("mTouchableInsets")) {
                                                touchableInsetsField = f;
                                            }
                                        }
                                        // 兼容其他魔改系统的命名
                                        if (touchableInsetsField == null) {
                                            for (Field f : info.getClass().getDeclaredFields()) {
                                                if (f.getType() == int.class && f.getName().toLowerCase().contains("touch")) {
                                                    touchableInsetsField = f;
                                                    touchableInsetsField.setAccessible(true);
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    touchableInsetsField.setInt(info, 3); // 3 corresponds to TOUCHABLE_INSETS_REGION
                                    Region region = (Region) touchableRegionField.get(info);
                                    region.setEmpty();
                                    Rect rect = new Rect();
                                    for (View view : mTouchableViews) {
                                        if (view != null && view.getVisibility() == View.VISIBLE) {
                                            view.getDrawingRect(rect);
                                            offsetDescendantRectToMyCoords(view, rect);
                                            region.union(rect);
                                        }
                                    }
                                } catch (Exception e) {
                                    Timber.tag(TAG).e(e, "Error updating InternalInsetsInfo");
                                }
                                return null;
                            } else if ("equals".equals(methodName)) {
                                return proxy == args[0];
                            } else if ("hashCode".equals(methodName)) {
                                return System.identityHashCode(proxy);
                            } else if ("toString".equals(methodName)) {
                                return "OnComputeInternalInsetsListenerProxy";
                            }
                            return null;
                        }
                    });

            Method addMethod = ViewTreeObserver.class.getMethod("addOnComputeInternalInsetsListener", listenerClass);
            addMethod.invoke(getViewTreeObserver(), mInsetsListener);
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Failed to attach OnComputeInternalInsetsListener via reflection");
        }
    }

    private void detachInsetsListener() {
        if (mInsetsListener != null) {
            try {
                Class<?> listenerClass = Class.forName("android.view.ViewTreeObserver$OnComputeInternalInsetsListener");
                Method removeMethod = ViewTreeObserver.class.getMethod("removeOnComputeInternalInsetsListener", listenerClass);
                removeMethod.invoke(getViewTreeObserver(), mInsetsListener);
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "Failed to remove OnComputeInternalInsetsListener");
            }
            mInsetsListener = null;
        }
    }
}
