
package com.lijun.progressbar.leafloading;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.util.PhotoUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class LeafLoadingProgressBar extends View {
    private static final String TAG = "LeafLoadingProgressBar";

    private static final int TOTAL_PROGRESS = 100;
    private static final long DEAFULT_LEAF_FLOAT_TIME = 3000;// 叶子飘动一个周期所花的时间
    private static final long DEAFULT_LEAF_ROTATE_TIME = 2000;// 叶子旋转一周需要的时间

    private static final int MSG_REFRESH_PROGRESS = 1;

    private enum State {NORMAL, LOADING}

    private State mState = State.NORMAL;

    //attrs
//    private int mLeafAmplitude;//叶子振幅
    private long mLeafRotateTime;//叶子旋转一周时间
    private long mLeafFloatTime;//叶子移动一个周期时间
    private int mLeafColor;//叶子颜色 取值:1-100
    private long mFanRotateTime;//扇子旋转一周时间
    private int mFanColor;//扇子颜色
    private int mBackgroudColor;//背景色
    private int mEmptyProgressColor;//空白色

    private int mProgress;//当前进度 0~100

    private int mWidth, mHeight;
    private int mOutLinePadding;

    //fan
    private int fanCicleRadius;
    private int fanCicleInnerRadius;
    private int fanCicleX;
    private int fanCicleY;
    private Rect fanRect;
    private int tempPadding = 5;//风扇与内圆间距

    // progress
    private int mProgressWidth;
    private int mCurrentProgressPosition;
    private int mArcCicle;//进度条左端的半圆半径
    private int mProgressCicleAngle;
    private RectF mArcRectF, mEmptyRectF, mArcEmptyRectF;
    private float emptRectProgressLeft;
    private float emptRectProgressRight;
    private int tempX;

    private AnimatedVectorDrawable fanDrawable;
    private Bitmap mBitmap;

    //leafs
    // arc的右上角的x坐标，也是矩形x坐标的起始点
    private int mArcRightLocation;
    private LeafFactory mLeafFactory;
    private List<Leaf> mLeafInfos;
    // 用于控制随机增加的时间不抱团
    private int mAddTime;
    private int mMaxLeafAmplitude = 60;//叶子最大振幅
    private int mMinLeafAmplitude = 30;//叶子最小振幅
    private Bitmap mLeafBitmap;
    private int mLeafWidth, mLeafHeight;

    private Paint mOutLinePaint;
    private Paint mFanPaint;
    private Paint mFillerPaint;
    private Paint mFillerPaintDSTOver;
    private Paint mEmptyPaint;
    private Paint mLeafPaint;

    PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

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

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_PROGRESS:
                    if (mProgress < 40) {
                        mProgress += 1;
                        // 随机800ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_PROGRESS,
                                new Random().nextInt(300));
                        LeafLoadingProgressBar.this.setProgress(mProgress);
                    } else {
                        mProgress += 1;
                        // 随机1200ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_PROGRESS,
                                new Random().nextInt(500));
                        LeafLoadingProgressBar.this.setProgress(mProgress);

                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void startFanRotateAnimation() {
        mState = State.LOADING;
        if (mFanRotateAnimator != null) {
            mFanRotateAnimator.cancel();
            mFanRotateAnimator = null;
        }
        mFanRotateAnimator = ObjectAnimator.ofFloat(this, CURRENT_FAN_ROTATE, 360);
        mCurrentFanRotate = 0;
        mFanRotateAnimator.setDuration(mFanRotateTime);
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

        Resources resources = getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeafLoadingProgressBar);
//        mLeafAmplitude = a.getInt(R.styleable.LeafLoadingProgressBar_leaf_amplitude, 50);
        mLeafRotateTime = a.getInt(R.styleable.LeafLoadingProgressBar_leaf_rotate_time, resources.getInteger(R.integer.default_leaf_float_duration));
        mLeafFloatTime = a.getInt(R.styleable.LeafLoadingProgressBar_leaf_move_float_time, resources.getInteger(R.integer.default_leaf_float_duration));
        mLeafColor = a.getInt(R.styleable.LeafLoadingProgressBar_leaf_color, R.color.leafloading_default_leaf_color);
        mFanRotateTime = a.getInt(R.styleable.LeafLoadingProgressBar_fan_rotate_time, resources.getInteger(R.integer.default_fan_rotate_duration));
        mFanColor = a.getInt(R.styleable.LeafLoadingProgressBar_fan_color, R.color.leafloading_default_fan_color);
        mBackgroudColor = a.getInt(R.styleable.LeafLoadingProgressBar_background_color, R.color.leafloading_backgroud_color);
        mEmptyProgressColor = a.getInt(R.styleable.LeafLoadingProgressBar_empty_progress_color, R.color.leafloading_empty_progress_color);
        a.recycle();

        init(context);
    }

    private void init(Context context) {

        Resources resources = context.getResources();

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//xfermode 需要关闭硬件加速

        mOutLinePaint = new Paint();
        mOutLinePaint.setStyle(Paint.Style.FILL);
        mOutLinePaint.setColor(mBackgroudColor);

        mFanPaint = new Paint(mFanColor);
        mFillerPaint = new Paint();
        mFillerPaint.setColor(mLeafColor);
        mFillerPaint.setAntiAlias(true);
        mFillerPaint.setStyle(Paint.Style.FILL);
        mFillerPaintDSTOver = new Paint(mFillerPaint);
        mFillerPaintDSTOver.setXfermode(xfermode);

        mEmptyPaint = new Paint();
        mEmptyPaint.setColor(Color.RED);
        mEmptyPaint.setAntiAlias(true);
        mEmptyPaint.setStyle(Paint.Style.FILL);
        mEmptyPaint.setXfermode(xfermode);

        mLeafPaint = new Paint();
        mLeafPaint.setAntiAlias(true);
        mLeafPaint.setDither(true);

        //可以控制旋转速度
        VectorDrawable vectorDrawable = (VectorDrawable) resources.getDrawable(R.drawable.fan);
        vectorDrawable.setTint(mFanColor);
        mBitmap = UiUtils.getBitmapFromVectorDrawable(context, vectorDrawable);

        //不可动态调整旋转速度
        fanDrawable = (AnimatedVectorDrawable) resources.getDrawable(R.drawable.fan_rotater);
        fanDrawable.setTint(mFanColor);
        fanDrawable.start();

        VectorDrawable drawable = (VectorDrawable) resources.getDrawable(R.drawable.leaf);
        drawable.setTint(mLeafColor);
        mLeafBitmap = UiUtils.getBitmapFromVectorDrawable(context, drawable);

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

        tempX = (int) Math.sqrt(fanCicleRadius * fanCicleRadius - (fanCicleRadius - mOutLinePadding) * (fanCicleRadius - mOutLinePadding));
        mProgressWidth = mWidth - mOutLinePadding - fanCicleRadius - tempX;
        mArcCicle = fanCicleRadius - mOutLinePadding;
        mArcRectF = new RectF(mOutLinePadding, mOutLinePadding, mOutLinePadding + mArcCicle * 2, mHeight - mOutLinePadding);
        emptRectProgressLeft = fanCicleRadius;
        emptRectProgressRight = mProgressWidth + mOutLinePadding;

        mMaxLeafAmplitude = (int) ((mHeight / 2 - mOutLinePadding) * 0.8);//初始化振幅范围
        mMinLeafAmplitude = (int) ((mHeight / 2 - mOutLinePadding) * 0.2);

        mLeafWidth = mLeafHeight = fanCicleRadius * 2 / 2;//叶子大小是扇子的1/3
        if (mLeafBitmap != null && mLeafBitmap.getHeight() > 0 && mLeafBitmap.getWidth() > 0 && mLeafBitmap.getWidth() != mLeafWidth || mLeafBitmap.getHeight() != mLeafHeight) {
            mLeafBitmap = PhotoUtils.zoom(mLeafBitmap, mLeafWidth, mLeafHeight);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeafFactory = new LeafFactory();
        mLeafInfos = mLeafFactory.generateLeafs();
        startFanRotateAnimation();//转动扇子
        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_PROGRESS, 1000);//进度条加载测试
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawStaticOutLines(canvas);
        drawProgress(canvas);
        drawLeafs(canvas);
        drawFan(canvas);
    }

    private void drawFan(Canvas canvas) {
        canvas.save();
        mOutLinePaint.setColor(Color.WHITE);
        canvas.drawCircle(fanCicleX, fanCicleY, fanCicleRadius, mOutLinePaint);
        mOutLinePaint.setColor(mBackgroudColor);
        canvas.drawCircle(fanCicleX, fanCicleY, fanCicleInnerRadius, mOutLinePaint);

        canvas.clipRect(fanRect);
        canvas.rotate(mCurrentFanRotate, fanCicleX, fanCicleY);
        canvas.drawBitmap(mBitmap, null, fanRect, mFanPaint);
//        fanDrawable.setBounds(fanRect);
//        fanDrawable.draw(canvas);
        canvas.restore();
    }

    private void drawProgress(Canvas canvas) {

        if (mProgress > TOTAL_PROGRESS) {
            mProgress = TOTAL_PROGRESS;
        }

        if (mCurrentProgressPosition < mArcCicle) {
            canvas.drawArc(mArcRectF, 90, 180, false, mEmptyPaint);
            int startAngle = 180 - mProgressCicleAngle;
            int sweepAngle = 2 * mProgressCicleAngle;
            canvas.drawArc(mArcRectF, startAngle, sweepAngle, false, mFillerPaint);
            canvas.drawRect(emptRectProgressLeft, mArcRectF.top, emptRectProgressRight, mArcRectF.bottom, mEmptyPaint);
        } else if (mCurrentProgressPosition < mProgressWidth - (fanCicleRadius - tempX)) {
            canvas.drawArc(mArcRectF, 90, 180, false, mFillerPaint);
            canvas.drawRect(mArcCicle + mOutLinePadding, mArcRectF.top, emptRectProgressLeft, mArcRectF.bottom, mFillerPaint);
            canvas.drawRect(emptRectProgressLeft, mArcRectF.top, emptRectProgressRight, mArcRectF.bottom, mEmptyPaint);
        } else {
            canvas.drawArc(mArcRectF, 90, 180, false, mFillerPaint);
            canvas.drawRect(mArcCicle + mOutLinePadding, mArcRectF.top, mWidth - fanCicleRadius * 2, mArcRectF.bottom, mFillerPaint);
            canvas.drawRect(mWidth - fanCicleRadius * 2, mArcRectF.top, emptRectProgressLeft, mArcRectF.bottom, mFillerPaint);
            canvas.drawRect(emptRectProgressLeft, mArcRectF.top, emptRectProgressRight, mArcRectF.bottom, mEmptyPaint);
        }

    }

    /**
     * 绘制叶子
     *
     * @param canvas
     */
    private void drawLeafs(Canvas canvas) {
        final long currentTime = System.currentTimeMillis();
        for (int i = 0; i < mLeafInfos.size(); i++) {
            Leaf leaf = mLeafInfos.get(i);
            if (currentTime > leaf.startTime && leaf.startTime != 0) {
                // 绘制叶子－－根据叶子的类型和当前时间得出叶子的（x，y）
                getLeafLocation(leaf, currentTime);
                // 根据时间计算旋转角度
                canvas.save();
                // 通过Matrix控制叶子旋转
                Matrix matrix = new Matrix();
                float transX = mOutLinePadding + leaf.x;
                float transY = mOutLinePadding + leaf.y;
                Log.i(TAG, "left.x = " + leaf.x + "--leaf.y=" + leaf.y);
                matrix.postTranslate(transX, transY);
                // 通过时间关联旋转角度，则可以直接通过修改LEAF_ROTATE_TIME调节叶子旋转快慢
                float rotateFraction = ((currentTime - leaf.startTime) % mLeafRotateTime)
                        / (float) mLeafRotateTime;
                int angle = (int) (rotateFraction * 360);
                // 根据叶子旋转方向确定叶子旋转角度
                int rotate = leaf.rotateDirection == 0 ? angle + leaf.rotateAngle : -angle
                        + leaf.rotateAngle;
                matrix.postRotate(rotate, transX
                        + mLeafWidth / 2, transY + mLeafHeight / 2);
                canvas.drawBitmap(mLeafBitmap, matrix, mLeafPaint);
                canvas.restore();
            } else {
                continue;
            }
        }
    }

    /**
     * 绘制静态框架部分
     *
     * @param canvas
     */
    private void drawStaticOutLines(Canvas canvas) {
        mOutLinePaint.setColor(mBackgroudColor);
        canvas.drawRoundRect(0, 0, mWidth, mHeight, mHeight / 2.0f, mHeight / 2.0f, mOutLinePaint);
        mOutLinePaint.setColor(mEmptyProgressColor);
        canvas.drawRoundRect(mOutLinePadding, mOutLinePadding, mWidth - mOutLinePadding, mHeight - mOutLinePadding, mHeight / 2.0f, mHeight / 2.0f, mOutLinePaint);
    }

    public void setProgress(int progress) {
        Log.d(TAG, "setProgress progress : " + progress);
        mProgress = progress;
        if (mProgress > TOTAL_PROGRESS) {
            mProgress = TOTAL_PROGRESS;
            stopProgessLoading();
        } else if (mProgress < 0) {
            mProgress = 0;
        }
        mCurrentProgressPosition = mProgressWidth * mProgress / TOTAL_PROGRESS;
        if (mCurrentProgressPosition < mArcCicle) {
            mProgressCicleAngle = (int) Math.toDegrees(Math.acos((mArcCicle - mCurrentProgressPosition)
                    / (float) mArcCicle));
            emptRectProgressLeft = mArcCicle + mOutLinePadding;
        } else {
            emptRectProgressLeft = mCurrentProgressPosition + mOutLinePadding;
        }
        invalidate();
    }

    private void stopProgessLoading() {
        mState = State.NORMAL;
        if (mFanRotateAnimator != null) {
            mFanRotateAnimator.cancel();
            mFanRotateAnimator = null;
        }
        mHandler.removeMessages(MSG_REFRESH_PROGRESS);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopProgessLoading();
    }

    /**
     * 叶子对象，用来记录叶子主要数据
     *
     * @author Ajian_Studio
     */
    private class Leaf {
        // 在绘制部分的位置
        float x, y;
        // 控制叶子飘动的幅度
        int amplitude;
        // 旋转角度
        int rotateAngle;
        // 旋转方向--0代表顺时针，1代表逆时针
        int rotateDirection;
        // 起始时间(ms)
        long startTime;
    }

    private class LeafFactory {
        private static final int MAX_LEAFS = 8;
        Random random = new Random();

        // 生成一个叶子信息
        public Leaf generateLeaf() {
            Leaf leaf = new Leaf();
            int randomAmplitude = random.nextInt(mMaxLeafAmplitude - mMinLeafAmplitude);
            // 随时类型－ 随机振幅
            leaf.amplitude = mMinLeafAmplitude + randomAmplitude;
            // 随机起始的旋转角度
            leaf.rotateAngle = random.nextInt(360);
            // 随机旋转方向（顺时针或逆时针）
            leaf.rotateDirection = random.nextInt(2);
            // 为了产生交错的感觉，让开始的时间有一定的随机性
            mLeafFloatTime = mLeafFloatTime <= 0 ? DEAFULT_LEAF_FLOAT_TIME : mLeafFloatTime;
            mAddTime += random.nextInt((int) (mLeafFloatTime * 2));
            leaf.startTime = System.currentTimeMillis() + mAddTime;
            return leaf;
        }

        // 根据最大叶子数产生叶子信息
        public List<Leaf> generateLeafs() {
            return generateLeafs(MAX_LEAFS);
        }

        // 根据传入的叶子数量产生叶子信息
        public List<Leaf> generateLeafs(int leafSize) {
            List<Leaf> leafs = new LinkedList<Leaf>();
            for (int i = 0; i < leafSize; i++) {
                leafs.add(generateLeaf());
            }
            return leafs;
        }
    }

    private void getLeafLocation(Leaf leaf, long currentTime) {
        long intervalTime = currentTime - leaf.startTime;
        if (intervalTime < 0) {
            return;
        } else if (intervalTime > mLeafFloatTime) {
            leaf.startTime = System.currentTimeMillis()
                    + new Random().nextInt((int) mLeafFloatTime);
        }

        float fraction = (float) intervalTime / mLeafFloatTime;
        leaf.x = (int) (mProgressWidth - mProgressWidth * fraction);
        leaf.y = getLocationY(leaf);
    }

    /**
     * 通过叶子信息获取当前叶子的Y值
     * <p>
     * 正弦型函数解析式：y=Asin（ωx+φ）+h
     * φ（初相位）：决定波形与X轴位置关系或横向移动距离（左加右减）
     * ω：决定周期（最小正周期T=2π/|ω|）
     * A：决定峰值（即纵向拉伸压缩的倍数）
     * h：表示波形在Y轴的位置关系或纵向移动距离（上加下减）
     */
    private int getLocationY(Leaf leaf) {
        float w = (float) ((float) 2 * Math.PI / mProgressWidth);
        return (int) (leaf.amplitude / 2 * Math.sin(w * leaf.x)) + mArcCicle * 2 / 3 - mOutLinePadding;
    }

}
