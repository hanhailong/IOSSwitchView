package com.hhl.library;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Property;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * 高仿IOS风格的UISwitchView
 * Created by HanHailong on 15/10/15.
 */
public class IOSSwitchView extends View {

    private static final int foregroundColor = 0xFFEFEFEF;
    private static final int backgroundColor = 0xFFCCCCCC;

    private int colorStep = backgroundColor;
    private int mTintColor;
    private int mThumbTintColor;
    private int mStrokeWidth;

    private boolean isOn = false;
    private boolean preIsOn;
    private boolean thumbState;

    private int width;
    private int height;

    private Paint mPaint;

    private ObjectAnimator mInnerContentAnimator;
    private ObjectAnimator mThumbExpandAnimator;
    private ObjectAnimator mThumbMoveAnimator;

    private OnSwitchStateChangeListener mOnSwitchStateChangeListener;

    private float innerContentRate = 1.0f;
    private float thumbExpandRate;
    private float thumbMoveRate;

    private RectF innerContentRectF;
    private RectF thumbRectF;
    private RectF tempRoundRectF;

    private float cornerRadius;
    private float centerX;
    private float centerY;
    private float intrinsicInnerWidth;
    private float intrinsicInnerHeight;
    private float intrinsicThumbWidth;
    private float thumbMaxExpandWidth;

    //手势检测器
    private GestureDetector mGestureDetector;

    public IOSSwitchView(Context context) {
        this(context, null);
    }

    public IOSSwitchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IOSSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IOSSwitchView);
        mTintColor = a.getColor(R.styleable.IOSSwitchView_tintColor, Color.GREEN);
        mThumbTintColor = a.getColor(R.styleable.IOSSwitchView_thumbTintColor, Color.WHITE);

        int defaultStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, context.getResources()
                .getDisplayMetrics());
        mStrokeWidth = a.getDimensionPixelOffset(R.styleable.IOSSwitchView_strokeWidth, defaultStrokeWidth);
        isOn = a.getBoolean(R.styleable.IOSSwitchView_isOn, false);
        preIsOn = isOn;
        thumbState = isOn;

        if (isOn) {
            thumbMoveRate = 1.0f;
            innerContentRate = 0.0f;
        } else {
            thumbMoveRate = 0.0f;
            innerContentRate = 1.0f;
        }

        a.recycle();

        //初始化画笔、动画等属性
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tempRoundRectF = new RectF();
        innerContentRectF = new RectF();
        thumbRectF = new RectF();

        //启动软加速
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //灰白色矩形形变动画
        mInnerContentAnimator = ObjectAnimator.ofFloat(this, new Property<IOSSwitchView, Float>(Float.class, "innerbound") {
            @Override
            public void set(IOSSwitchView object, Float value) {
                object.setInnerContentRate(value);
            }

            @Override
            public Float get(IOSSwitchView object) {
                return object.getInnerContentRate();
            }
        }, innerContentRate, 1.0f);
        mInnerContentAnimator.setDuration(300);
        mInnerContentAnimator.setInterpolator(new DecelerateInterpolator());

        //thumb拉伸动画
        mThumbExpandAnimator = ObjectAnimator.ofFloat(this, new Property<IOSSwitchView, Float>(Float.class, "thumbExpand") {
            @Override
            public void set(IOSSwitchView object, Float value) {
                object.setThumbExpandRate(value);
            }

            @Override
            public Float get(IOSSwitchView object) {
                return object.getThumbExpandRate();
            }
        }, thumbExpandRate, 1.0f);
        mThumbExpandAnimator.setDuration(300);
        mThumbExpandAnimator.setInterpolator(new DecelerateInterpolator());

        //thumb位移动画
        mThumbMoveAnimator = ObjectAnimator.ofFloat(this, new Property<IOSSwitchView, Float>(Float.class, "thumbMove") {
            @Override
            public void set(IOSSwitchView object, Float value) {
                object.setThumbMoveRate(value);
            }

            @Override
            public Float get(IOSSwitchView object) {
                return object.getThumbMoveRate();
            }
        }, thumbMoveRate, 1.0f);
        mThumbMoveAnimator.setDuration(300);
        mThumbMoveAnimator.setInterpolator(new DecelerateInterpolator());

        //手势
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {

                if (!isEnabled()) return false;

                preIsOn = isOn;

                //灰白色矩形缩小到0
                mInnerContentAnimator.setFloatValues(innerContentRate, 0.0f);
                mInnerContentAnimator.start();

                //thumb有个拉伸的动作
                mThumbExpandAnimator.setFloatValues(thumbExpandRate, 1.0f);
                mThumbExpandAnimator.start();

                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                //手指抬起执行一系列的动画
                isOn = thumbState;

                if (preIsOn == isOn) {//反转
                    isOn = !isOn;
                    thumbState = !thumbState;
                }

                //打开状态
                if (thumbState) {
                    //thumb移动到右侧打开区域
                    mThumbMoveAnimator.setFloatValues(thumbMoveRate, 1.0F);
                    mThumbMoveAnimator.start();

                    //灰白色圆角矩形缩小到0
                    mInnerContentAnimator.setFloatValues(innerContentRate, 0.0F);
                    mInnerContentAnimator.start();
                } else {//关闭状态
                    //thumb移动到左侧关闭区域
                    mThumbMoveAnimator.setFloatValues(thumbMoveRate, 0.0F);
                    mThumbMoveAnimator.start();

                    //灰白色圆角矩形放大到覆盖背景大小
                    mInnerContentAnimator.setFloatValues(innerContentRate, 1.0F);
                    mInnerContentAnimator.start();
                }
                //thumb恢复原大小
                mThumbExpandAnimator.setFloatValues(thumbExpandRate, 0.0F);
                mThumbExpandAnimator.start();

                if (mOnSwitchStateChangeListener != null && preIsOn != isOn) {
                    mOnSwitchStateChangeListener.onStateSwitched(isOn);
                }

                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                //在打开开关的区域
                if (e2.getX() > centerX) {
                    //并且开关状态是关闭的，就执行打开开关操作
                    if (!thumbState) {
                        thumbState = !thumbState;

                        mThumbMoveAnimator.setFloatValues(thumbMoveRate, 1.0F);
                        mThumbMoveAnimator.start();

                        mInnerContentAnimator.setFloatValues(innerContentRate, 0.0F);
                        mInnerContentAnimator.start();
                    }
                } else {//在关闭区域
                    //开关处于打开状态
                    if (thumbState) {
                        thumbState = !thumbState;
                        //执行关闭开关动画
                        mThumbMoveAnimator.setFloatValues(thumbMoveRate, 0.0F);
                        mThumbMoveAnimator.start();
                    }
                }

                return true;
            }

        });
        //禁止长按
        mGestureDetector.setIsLongpressEnabled(false);


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //测量宽度和高度
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        //保持一定的宽高比例
        if ((float) height / (float) width < 0.5f) {
            height = (int) (width * 0.5);

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(heightMeasureSpec));
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.getMode(widthMeasureSpec));
            super.setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }

        centerX = width * 0.5f;
        centerY = height * 0.5f;
        cornerRadius = centerY;

        innerContentRectF.left = mStrokeWidth;
        innerContentRectF.top = mStrokeWidth;
        innerContentRectF.right = width - mStrokeWidth;
        innerContentRectF.bottom = height - mStrokeWidth;

        intrinsicInnerWidth = innerContentRectF.width();
        intrinsicInnerHeight = innerContentRectF.height();

        thumbRectF.left = mStrokeWidth;
        thumbRectF.top = mStrokeWidth;
        thumbRectF.right = width - mStrokeWidth;
        thumbRectF.bottom = height - mStrokeWidth;

        intrinsicThumbWidth = thumbRectF.height();

        //thumb最大拉伸宽度
        thumbMaxExpandWidth = width * 0.7f;

        if (thumbMaxExpandWidth > intrinsicThumbWidth * 1.25f) {
            thumbMaxExpandWidth = intrinsicThumbWidth * 1.25f;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (!thumbState) {
                    mInnerContentAnimator.setFloatValues(innerContentRate, 1.0f);
                    mInnerContentAnimator.start();
                }

                mThumbExpandAnimator.setFloatValues(thumbExpandRate, 0.0f);
                mThumbExpandAnimator.start();

                isOn = thumbState;

                if (mOnSwitchStateChangeListener != null && isOn != preIsOn) {
                    mOnSwitchStateChangeListener.onStateSwitched(isOn);
                }

                break;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = intrinsicInnerWidth * 0.5f * innerContentRate;
        float h = intrinsicInnerHeight * 0.5f * innerContentRate;

        this.innerContentRectF.left = centerX - w;
        this.innerContentRectF.top = centerY - h;
        this.innerContentRectF.right = centerX + w;
        this.innerContentRectF.bottom = centerY + h;

        //thumb拉伸宽度变化，其变化值从1->1.7之间
        w = intrinsicThumbWidth + (thumbMaxExpandWidth - intrinsicThumbWidth) * thumbExpandRate;

        boolean left = thumbRectF.left + thumbRectF.width() * 0.5f < centerX;
        if (left) {
            thumbRectF.left = thumbRectF.right - w;
        } else {
            thumbRectF.right = thumbRectF.left + w;
        }

        float kw = thumbRectF.width();
        w = (float) (width - kw - (mStrokeWidth * 2)) * thumbMoveRate;

        thumbRectF.left = mStrokeWidth + w;
        thumbRectF.right = thumbRectF.left + kw;

        //颜色值过渡变化，从深灰白色变化到tintColor色
        this.colorStep = transformRGBColor(thumbMoveRate, backgroundColor, mTintColor);

        //画TintColor颜色的圆角矩形
        mPaint.setColor(colorStep);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        drawRoundRect(0, 0, width, height, cornerRadius, canvas, mPaint);

        mPaint.setColor(foregroundColor);
        //画灰白色圆角矩形
        canvas.drawRoundRect(innerContentRectF, innerContentRectF.height() * 0.5f, innerContentRectF.height() * 0.5f, mPaint);

        //画thumb
        mPaint.setColor(mThumbTintColor);
        canvas.drawRoundRect(thumbRectF, cornerRadius, cornerRadius, mPaint);

        mPaint.setColor(0xFFCCCCCC);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1);
        canvas.drawRoundRect(thumbRectF, cornerRadius, cornerRadius, mPaint);
    }

    /**
     * RGB颜色过渡变化
     *
     * @param progress
     * @param fromColor
     * @param toColor
     * @return
     */
    private int transformRGBColor(float progress, int fromColor, int toColor) {
        int fr = (fromColor >> 16) & 0xFF;
        int fg = (fromColor >> 8) & 0xFF;
        int fb = fromColor & 0xFF;

        int tr = (toColor >> 16) & 0xFF;
        int tg = (toColor >> 8) & 0xFF;
        int tb = toColor & 0xFF;

        int rGap = (int) ((float) (tr - fr) * progress);
        int gGap = (int) ((float) (tg - fg) * progress);
        int bGap = (int) ((float) (tb - fb) * progress);

        return 0xFF000000 | ((fr + rGap) << 16) | ((fg + gGap) << 8) | (fb + bGap);
    }

    /**
     * 画圆角矩形
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param radius
     * @param canvas
     * @param paint
     */
    private void drawRoundRect(float left, float top, float right, float bottom, float radius, Canvas canvas, Paint paint) {
        tempRoundRectF.set(left, top, right, bottom);
        canvas.drawRoundRect(tempRoundRectF, radius, radius, paint);
    }

    /**
     * 设置切换状态变化监听器
     *
     * @param listener
     */
    public void setOnSwitchStateChangeListener(OnSwitchStateChangeListener listener) {
        this.mOnSwitchStateChangeListener = listener;
    }

    public int getTintColor() {
        return mTintColor;
    }

    public void setTintColor(int mTintColor) {
        this.mTintColor = mTintColor;
    }

    public int getThumbTintColor() {
        return mThumbTintColor;
    }

    public void setThumbTintColor(int mThumbTintColor) {
        this.mThumbTintColor = mThumbTintColor;
    }

    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(int mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
    }

    public boolean isOn() {
        return isOn;
    }

    /**
     * 设置是否选中
     *
     * @param on
     */
    public void setOn(boolean on) {
        setOn(isOn, false);
    }

    public void setOn(boolean on, boolean animate) {
        // TODO: 15/10/16 设置是否选中
        if (isOn = on) return;

        isOn = on;
        thumbState = on;

        //有动画效果
        if (animate) {
            if (on) {
                mInnerContentAnimator.setFloatValues(innerContentRate, 0);
                mInnerContentAnimator.start();

                mThumbMoveAnimator.setFloatValues(thumbMoveRate, 1);
                mThumbMoveAnimator.start();
            } else {
                mInnerContentAnimator.setFloatValues(innerContentRate, 1);
                mInnerContentAnimator.start();

                mThumbMoveAnimator.setFloatValues(thumbMoveRate, 0);
                mThumbMoveAnimator.start();
            }

            mThumbExpandAnimator.setFloatValues(thumbExpandRate, 0);
            mThumbExpandAnimator.start();
        } else {
            //设置选中
            if (on) {
                setThumbMoveRate(1);
                setInnerContentRate(0);
            } else {
                setThumbMoveRate(0);
                setInnerContentRate(1);
            }
            setThumbExpandRate(0);
        }

        if (mOnSwitchStateChangeListener != null) {
            mOnSwitchStateChangeListener.onStateSwitched(isOn);
        }
    }

    private float getInnerContentRate() {
        return innerContentRate;
    }

    private void setInnerContentRate(float innerContentRate) {
        this.innerContentRate = innerContentRate;
        invalidate();
    }

    private float getThumbExpandRate() {
        return thumbExpandRate;
    }

    private void setThumbExpandRate(float thumbExpandRate) {
        this.thumbExpandRate = thumbExpandRate;
        invalidate();
    }

    private float getThumbMoveRate() {
        return thumbMoveRate;
    }

    private void setThumbMoveRate(float thumbMoveRate) {
        this.thumbMoveRate = thumbMoveRate;
        invalidate();
    }

    /**
     * SwitchView状态切换
     */
    public static interface OnSwitchStateChangeListener {
        /**
         * 是否选中
         *
         * @param isOn
         */
        public void onStateSwitched(boolean isOn);
    }

}
