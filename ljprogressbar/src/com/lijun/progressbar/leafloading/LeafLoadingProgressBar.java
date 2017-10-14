
package com.lijun.progressbar.leafloading;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.lijun.androidstudy.R;

public class LeafLoadingProgressBar extends View {
    private static final String TAG = "LeafLoadingView";

    private static final int MIN_LEAF_MOVE_SPEED = 100;
    private static final int MAX_LEAF_MOVE_SPEED = 500;
    private static final int TOTAL_PROGRESS = 100;
    private static final int MIN_FAN_ROTATE_TIME = 500;
    private static final int MAX_FAN_ROTATE_TIME = 5000;

    private int mLeafColor;//叶子颜色 取值:1-100
    private int mLeafMoveSpeed;//叶子移动速度 取值:1-100
    private int mLeafRotateSpeed;//叶子旋转速度 取值:1-100
    private int mLeafAmplitude;//叶子振幅 取值:1-100
    private int mFanRotateSpeed;//扇子旋转速度 取值:1-100
    private int mFanColor;//扇子颜色

    private int mProgress;//0~100

    private int mWidth, mHeight;
    private int mOutLinePadding;
    private int fanCicleRadius;
    private int fanCicleInnerRadius;
    private int fanCicleX;
    private int fanCicleY;
    private Rect fanRect;
    private int tempPadding = 5;//风扇与内圆间距
    private AnimatedVectorDrawable fanDrawable;
    private Bitmap mBitmap;


    private Paint mOutLinePaint;
    private Paint mFanPaint;

    private int mOutLineColor;
    private int mOutLineInnerColor;


    private float mCurrentFanRotate;//当前扇子旋转角度
    private ObjectAnimator mFanRotateAnimator;//扇子旋转动画
    private final Property<LeafLoadingProgressBar, Float> CURRENT_FAN_ROTATE
            = new Property<LeafLoadingProgressBar, Float>(float.class, "current_position") {
        @Override
        public Float get(LeafLoadingProgressBar obj) {
            return obj.mCurrentFanRotate;
        }

        @Override
        public void set(LeafLoadingProgressBar obj, Float pos) {
            if (pos >= 360) {
                obj.mCurrentFanRotate = 0.0f;
                CURRENT_FAN_ROTATE.set(LeafLoadingProgressBar.this, 0.0f);
                obj.startFanRotateAnimation();
            } else {
                obj.mCurrentFanRotate = pos;
                obj.invalidate();
                obj.invalidateOutline();
            }

        }
    };

    private void startFanRotateAnimation() {
        if (mFanRotateAnimator != null) {
            mFanRotateAnimator.cancel();
            mFanRotateAnimator = null;
        }
        mFanRotateAnimator = ObjectAnimator.ofFloat(this, CURRENT_FAN_ROTATE, 360);
        mCurrentFanRotate = 0;
        mFanRotateAnimator.setDuration(MIN_FAN_ROTATE_TIME + (MAX_FAN_ROTATE_TIME - MIN_FAN_ROTATE_TIME) * (100 - mFanRotateSpeed) / 100);//lijun add isAnimationgToDefault?currentDuration:
        mFanRotateAnimator.setInterpolator(new LinearInterpolator());
        mFanRotateAnimator.start();
        fanDrawable.start();
    }

    public LeafLoadingProgressBar(Context context) {
        this(context, null);
    }

    public LeafLoadingProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeafLoadingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeafLoadingProgressBar);
        mLeafColor = a.getInt(R.styleable.LeafLoadingProgressBar_leaf_color, R.color.leafloading_default_leaf_color);
        mFanRotateSpeed = a.getInt(R.styleable.LeafLoadingProgressBar_fan_rotate_speed, 50);
        if (mFanRotateSpeed < 0) mFanRotateSpeed = 0;
        if (mFanRotateSpeed > 100) mFanRotateSpeed = 100;
        mFanColor = a.getInt(R.styleable.LeafLoadingProgressBar_fan_color, R.color.leafloading_default_fan_color);
        a.recycle();

        init(context);
    }

    private void init(Context context) {

        Resources resources = context.getResources();

        mOutLinePaint = new Paint();
        mOutLinePaint.setStyle(Paint.Style.FILL);
        mOutLinePaint.setColor(getResources().getColor(R.color.leafloading_outline_color));

        mFanPaint = new Paint();
        mFanPaint.setColor(mFanColor);

        mOutLineColor = resources.getColor(R.color.leafloading_outline_color);
        mOutLineInnerColor = resources.getColor(R.color.leafloading_outline_inner_color);

        //可以控制旋转速度
        VectorDrawable vectorDrawable = (VectorDrawable) getResources().getDrawable(R.drawable.fan);
        vectorDrawable.setTint(mFanColor);
        mBitmap = UiUtils.getBitmapFromVectorDrawable(context,vectorDrawable);

        //不可动态调整旋转速度
        fanDrawable = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.fan_rotater);
        fanDrawable.setTint(mFanColor);
        fanDrawable.start();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);


        if (widthMode != View.MeasureSpec.EXACTLY || heightMode != View.MeasureSpec.EXACTLY) {
            heightSize = widthSize / 6;
            final int childWidthMeasureSpec =
                    View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY);
            final int childHeightMeasureSpec =
                    View.MeasureSpec.makeMeasureSpec(heightSize, View.MeasureSpec.EXACTLY);
            super.onMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
        mWidth = widthSize;
        mHeight = heightSize;
        mOutLinePadding = (mHeight < mWidth ? mHeight : mWidth) / 8;
        fanCicleRadius = (mHeight < mWidth ? mHeight : mWidth) / 2;
        fanCicleInnerRadius = (int) (fanCicleRadius * 0.85);
        fanCicleX = mWidth - fanCicleRadius;
        fanCicleY = fanCicleRadius;
        int fanPadding = (int) (fanCicleRadius * 0.15);
        fanRect = new Rect(mWidth - fanCicleRadius - fanCicleInnerRadius + tempPadding, fanPadding + tempPadding, mWidth - fanPadding - tempPadding, mHeight - fanPadding - tempPadding);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        startFanRotateAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawStaticOutLines(canvas);
        drawFan(canvas);
        drawProgressAndLeafs(canvas);
    }

    private void drawFan(Canvas canvas) {
        canvas.clipRect(fanRect);
//        canvas.rotate(mCurrentFanRotate,fanCicleX,fanCicleY);
//        canvas.drawBitmap(mBitmap,null,fanRect,mFanPaint);
        fanDrawable.setBounds(fanRect);
        fanDrawable.draw(canvas);
    }

    private void drawProgressAndLeafs(Canvas canvas) {

        if (mProgress > TOTAL_PROGRESS) {
            mProgress = TOTAL_PROGRESS;
        }
//        // mProgressWidth为进度条的宽度，根据当前进度算出进度条的位置
//        mCurrentProgressPosition = mProgressWidth * mProgress / TOTAL_PROGRESS;
//        // 即当前位置在图中所示1范围内
//        if (mCurrentProgressPosition < mArcRadius) {
//            Log.i(TAG, "mProgress = " + mProgress + "---mCurrentProgressPosition = "
//                    + mCurrentProgressPosition
//                    + "--mArcProgressWidth" + mArcRadius);
//            // 1.绘制白色ARC，绘制orange ARC
//            // 2.绘制白色矩形
//
//            // 1.绘制白色ARC
//            canvas.drawArc(mArcRectF, 90, 180, false, mWhitePaint);
//
//            // 2.绘制白色矩形
//            mWhiteRectF.left = mArcRightLocation;
//            canvas.drawRect(mWhiteRectF, mWhitePaint);
//
//            // 绘制叶子
//            drawLeafs(canvas);
//
//            // 3.绘制棕色 ARC
//            // 单边角度
//            int angle = (int) Math.toDegrees(Math.acos((mArcRadius - mCurrentProgressPosition)
//                    / (float) mArcRadius));
//            // 起始的位置
//            int startAngle = 180 - angle;
//            // 扫过的角度
//            int sweepAngle = 2 * angle;
//            Log.i(TAG, "startAngle = " + startAngle);
//            canvas.drawArc(mArcRectF, startAngle, sweepAngle, false, mOrangePaint);
//        } else {
//            Log.i(TAG, "mProgress = " + mProgress + "---transfer-----mCurrentProgressPosition = "
//                    + mCurrentProgressPosition
//                    + "--mArcProgressWidth" + mArcRadius);
//            // 1.绘制white RECT
//            // 2.绘制Orange ARC
//            // 3.绘制orange RECT
//            // 这个层级进行绘制能让叶子感觉是融入棕色进度条中
//
//            // 1.绘制white RECT
//            mWhiteRectF.left = mCurrentProgressPosition;
//            canvas.drawRect(mWhiteRectF, mWhitePaint);
//            // 绘制叶子
//            drawLeafs(canvas);
//            // 2.绘制Orange ARC
//            canvas.drawArc(mArcRectF, 90, 180, false, mOrangePaint);
//            // 3.绘制orange RECT
//            mOrangeRectF.left = mArcRightLocation;
//            mOrangeRectF.right = mCurrentProgressPosition;
//            canvas.drawRect(mOrangeRectF, mOrangePaint);
//
//        }

    }

    /**
     * 绘制叶子
     *
     * @param canvas
     */
    private void drawLeafs(Canvas canvas) {
//        mLeafRotateTime = mLeafRotateTime <= 0 ? LEAF_ROTATE_TIME : mLeafRotateTime;
//        long currentTime = System.currentTimeMillis();
//        for (int i = 0; i < mLeafInfos.size(); i++) {
//            Leaf leaf = mLeafInfos.get(i);
//            if (currentTime > leaf.startTime && leaf.startTime != 0) {
//                // 绘制叶子－－根据叶子的类型和当前时间得出叶子的（x，y）
//                getLeafLocation(leaf, currentTime);
//                // 根据时间计算旋转角度
//                canvas.save();
//                // 通过Matrix控制叶子旋转
//                Matrix matrix = new Matrix();
//                float transX = mLeftMargin + leaf.x;
//                float transY = mLeftMargin + leaf.y;
//                Log.i(TAG, "left.x = " + leaf.x + "--leaf.y=" + leaf.y);
//                matrix.postTranslate(transX, transY);
//                // 通过时间关联旋转角度，则可以直接通过修改LEAF_ROTATE_TIME调节叶子旋转快慢
//                float rotateFraction = ((currentTime - leaf.startTime) % mLeafRotateTime)
//                        / (float) mLeafRotateTime;
//                int angle = (int) (rotateFraction * 360);
//                // 根据叶子旋转方向确定叶子旋转角度
//                int rotate = leaf.rotateDirection == 0 ? angle + leaf.rotateAngle : -angle
//                        + leaf.rotateAngle;
//                matrix.postRotate(rotate, transX
//                        + mLeafWidth / 2, transY + mLeafHeight / 2);
//                canvas.drawBitmap(mLeafBitmap, matrix, mBitmapPaint);
//                canvas.restore();
//            } else {
//                continue;
//            }
//        }
    }

    /**
     * 绘制静态框架部分
     *
     * @param canvas
     */
    private void drawStaticOutLines(Canvas canvas) {

        mOutLinePaint.setColor(mOutLineColor);
        canvas.drawRoundRect(0, 0, mWidth, mHeight, mHeight / 2.0f, mHeight / 2.0f, mOutLinePaint);
        mOutLinePaint.setColor(mOutLineInnerColor);
        canvas.drawRoundRect(mOutLinePadding, mOutLinePadding, mWidth - mOutLinePadding, mHeight - mOutLinePadding, mHeight / 2.0f, mHeight / 2.0f, mOutLinePaint);
        mOutLinePaint.setColor(Color.WHITE);
        canvas.drawCircle(fanCicleX, fanCicleY, fanCicleRadius, mOutLinePaint);
        mOutLinePaint.setColor(mOutLineColor);
        canvas.drawCircle(fanCicleX, fanCicleY, fanCicleInnerRadius, mOutLinePaint);
    }
}
