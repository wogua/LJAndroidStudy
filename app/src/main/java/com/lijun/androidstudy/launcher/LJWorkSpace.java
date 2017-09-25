package com.lijun.androidstudy.launcher;

import java.util.ArrayList;
import java.util.List;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.util.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class LJWorkSpace extends ViewGroup {
    private static final String TAG = "LJWorkSpace";
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mCurScreen;
    private int mNextScreen = -1;
    private int mDefaultScreen = 0;
    private int mChildWidth = 0;
    private Rect mViewport = new Rect();

    private int mCurScrollX;
    private int mCurScrollY;
    private int currentDegree;
    private int currentobDegree;

    private static final int TOUCH_STATE_REST = 0;// 手指离开屏幕状态
    private static final int TOUCH_STATE_SCROLLING = 1;
    protected static final int PAGE_SNAP_ANIMATION_DURATION = 750;
    private static final int SNAP_VELOCITY = 600;

    public static final String SCROLL_STYLE_KEY = "scroll_style";
    //the styles need match arrays slide_menu_values
    public static final int SCROLL_STYLE_CLASSICS = 1;//经典翻页方式
    public static final int SCROLL_STYLE_CUBE = 2;//立方体翻页
    public static final int SCROLL_STYLE_COLUMNAR = 3;//柱状翻页
    public static final int SCROLL_STYLE_SPHERE = 4;//球面翻页
    public static final int SCROLL_STYLE_3DROTA = 5;//3D翻转 from 三星
    public static final int SCROLL_STYLE_CARDSTACK = 6;//卡片堆
    public int mScrollStyle = 1;

    private int mTouchState = TOUCH_STATE_REST;// 手指与屏幕是否接触状态

    private int mTouchSlop;
    /** 横坐标的最终位置 */
    private float mLastMotionX;
    /** 纵坐标的最终位置 */
    private float mLastMotionY;

    private int mMaxScrollX;
    protected int mChildCountOnLastLayout;
    private int[] mPageScrolls;
    private int mOverScrollX;
    private int[] mTempVisiblePagesRange = new int[2];
    private int[] mTmpIntPoint = new int[2];
    protected boolean mForceDrawAllChildrenNextFrame;
    private int mLastScreenCenter = -1;

    private int mUnboundedScrollX;
    private boolean mFirstLayout = true;

    private float mDensity;//屏幕密度
    private int mCameraDistance = 14000;

    /********sphere*******/
    private boolean sphereInitialized = false;
    private Bitmap[] mCelllayoutsBitmapCache;//每页视图的缓存位图
    private Paint mSpherePaint;
    ColorMatrixColorFilter mColorMatrixFilter;
    LayoutBitmapPieces[] mLayoutBitmapPieces;
    private float originalX,originalY;//MotionEvent.ACTION_DOWN 的位置
    private float deltaDistanceX,deltaDistanceY;//相对于初始点滑动的距离坐标
    private Camera mCamera;
    private Matrix mMatrix;
    float[] coordZ1;
    float[] rotateX;
    float[] coordY;
    private float[] oldCoordX;
    private float[] oldCoordY;
    private float mRadius;
    private double perAngle;//将球面按经度分割，每个经度上的x,y角度是固定的
    private float mLastMotionXRemainder;
    private float perDegree = 0;//每一列的角度差
    private int screenCenter;
    private static int prepare = 20;//生成球面所需要的滑动角度
    private int currentDegreeX;//X轴偏移的角度
    private int currentDegreeY;//Y轴偏移的角度
    private boolean sphereDrawHull = false;//完整的球面形成后，后面都画完整的
    /********sphere*******/
    public LJWorkSpace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public LJWorkSpace(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public LJWorkSpace(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        final Resources res = getResources();
        mCurScreen = mDefaultScreen;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mDensity = res.getDisplayMetrics().density;
        mCameraDistance = res.getInteger(R.integer.config_cameraDistance);

        SharedPreferences sp= context.getSharedPreferences("LJLauncher", Activity.MODE_PRIVATE);
        mScrollStyle = sp.getInt(SCROLL_STYLE_KEY, SCROLL_STYLE_CLASSICS);
        scrollStyleChanged(mScrollStyle);

        mSpherePaint = new Paint();
        mCamera = new Camera();
        mMatrix = new Matrix();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        // TODO Auto-generated method stub
        final int childCount = getChildCount();
        if (mPageScrolls == null || childCount != mChildCountOnLastLayout) {
            mPageScrolls = new int[childCount];
        }
        if (changed) {
            /** 子view离父view左边的距离 */
            int childLeft = 0;
            /** 获取子view数目 */

            for (int i = 0; i < childCount; i++) {
                /** 获取子view */
                final View childView = getChildAt(i);

                if (childView.getVisibility() != View.GONE) {// 如果子view可见的话
                    /** 获取子view的宽度 */
                    mChildWidth = childView.getMeasuredWidth();
                    /** 为子view设置大小和位置 */
                    childView.layout(childLeft, 0, childLeft + mChildWidth,
                            childView.getMeasuredHeight());
                    /** 左边距自加子view宽度，从而得到下一个子view的x坐标 */
                    mPageScrolls[i] = childLeft;
                    childLeft += mChildWidth;
                }
            }
        }
        if (mFirstLayout && mCurScreen >= 0 && mCurScreen < getChildCount()) {
            setHorizontalScrollBarEnabled(false);
            updateCurrentPageScroll();
            setHorizontalScrollBarEnabled(true);
            mFirstLayout = false;
        }
        if (childCount > 0) {
            mMaxScrollX = getScrollForPage(childCount - 1);
        } else {
            mMaxScrollX = 0;
        }
        if (mScroller.isFinished()
                && mChildCountOnLastLayout != getChildCount()) {
            setCurrentPage(getNextPage());
        }
        mChildCountOnLastLayout = childCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * int specMode = MeasureSpec.getMode(measureSpec); int specSize =
         * MeasureSpec.getSize(measureSpec); 依据specMode的值，如果是AT_MOST，specSize
         * 代表的是最大可获得的空间； 如果是EXACTLY，specSize 代表的是精确的尺寸；
         * 如果是UNSPECIFIED，对于控件尺寸来说，没有任何参考意义。
         */
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout only can run at EXACTLY mode!");
        }
        mViewport.set(0, 0, widthSize, heightSize);
        // 给每一个子view给予相同的空间
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        /** 滚动到目标坐标 */
        scrollTo(mCurScreen * widthSize, 0);
    }

    /**
     * 根据当前布局的位置，滚动到目的页面
     */
    public void snapToDestination() {
        final int screenWidth = getMeasuredWidth();
        Log.i(TAG, "screenWidth: " + screenWidth + " screenWidth/2: "
                + screenWidth / 2);
        int destScreen = 0;
        if (getScrollX() < 0) {
            destScreen = (getScrollX() - screenWidth / 2) / screenWidth;
        } else {
            destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
        }
        snapToScreen(destScreen);
    }

    public void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, PAGE_SNAP_ANIMATION_DURATION);
    }

    public void snapToScreenImmediately(int whichScreen) {
        snapToScreen(whichScreen, PAGE_SNAP_ANIMATION_DURATION, true);
    }

    public void snapToScreen(int whichScreen, int duration) {
        snapToScreen(whichScreen, duration, false);
    }

    public void snapToScreen(int whichScreen, int duration, boolean immediate) {
        // whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() -1));
        if (whichScreen == -1) {
            whichScreen = getChildCount() - 1;
        } else if (whichScreen == getChildCount()) {
            whichScreen = 0;
        }
        int newX = getScrollForPage(whichScreen);

        if (((mCurScreen == 0 && whichScreen == getChildCount() - 1) || (mCurScreen == getChildCount() - 1 && whichScreen == 0))) {
            int halfScreenSize = getViewportWidth() / 2;
            if (getScrollX() < halfScreenSize) {
                mUnboundedScrollX = getChildCount() * getMeasuredWidth() + getScrollX();
            } else if (getScrollX() > (mMaxScrollX - halfScreenSize)) {
                mUnboundedScrollX -= mMaxScrollX;
            }
        }

        int delta = newX - mUnboundedScrollX;
        if (((mCurScreen == 0 && whichScreen == getChildCount() - 1) || (mCurScreen == getChildCount() - 1 && whichScreen == 0))) {
            if (newX == 0 && (getScrollX() <= 0 || getScrollX() >= mMaxScrollX)) {
                delta += getMeasuredWidth();
                mUnboundedScrollX = -delta;
            }
        }
        snapToScreen(whichScreen, delta, duration, immediate);
    }

    public void snapToScreen(int whichScreen, int delta, int duration,
                             boolean immediate) {
        mNextScreen = whichScreen;
        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurScreen
                && focusedChild == getPageAt(mCurScreen)) {
            focusedChild.clearFocus();
        }

        awakenScrollBars(duration);
        if (immediate) {
            duration = 0;
        } else if (duration == 0) {
            duration = Math.abs(delta);
        }

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
//		Log.d("--lijun--", "mUnboundedScrollX:"+mUnboundedScrollX + "  delta:"+delta);
        mScroller.startScroll(mUnboundedScrollX, 0, delta, 0, duration);

        if (immediate) {
            computeScroll();
        }
        invalidate();
        mCurScreen = whichScreen;
    }

    public void setToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        mCurScreen = whichScreen;
        scrollTo(whichScreen * getMeasuredWidth(), 0);
    }

    int getCurrentPage() {
        return mCurScreen;
    }

    int getNextPage() {
        return (mNextScreen != -1) ? mNextScreen : mCurScreen;
    }

    /**
     * 由父视图调用，用于通知子视图在必要时更新 mScrollX 和 mScrollY 的值 该操作主要用于子视图使用 Scroller
     * 进行动画滚动时。
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {// 返回true，表示动画仍在进行，还没有停止
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());// 滚动到目标坐标
            postInvalidate(); // 使view重画
        }
    }

    /**
     * 触摸监听事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();// 获取mVelocityTracker实例对象
        }

        /** 将当前的移动事件传递给mVelocityTracker对象 */
        mVelocityTracker.addMovement(event);

        /** 获取当前触摸动作 */
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        // Log.d("--lijun--", "onTouchEvent X:"+ x + "  Y:" +y);
        switch (action) {
            case MotionEvent.ACTION_DOWN:// 当向下按时
                Log.e(TAG, "event down!");
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();// Scrooller停止动画行为
                }
                mLastMotionX = x;
                mLastMotionXRemainder = 0;
                deltaDistanceX = deltaDistanceY = 0;
                sphereDrawHull = false;
                break;

            case MotionEvent.ACTION_MOVE:// 当手指滑动时
                final float deltaX = mLastMotionX + mLastMotionXRemainder - x;
                deltaDistanceX = x - originalX;
                deltaDistanceY = y - originalY;
                final float xDiff = Math.abs(deltaX);
                if (xDiff > mTouchSlop) {
                    mTouchState = TOUCH_STATE_SCROLLING; // 视图还在移动状态
                }
                mLastMotionX = x;
                scrollBy((int) deltaX, 0);
                mLastMotionXRemainder = deltaX - (int) deltaX;//缓慢滑动时deltax太小，不断丢失小数位导致滑动位置不一致
                break;

            case MotionEvent.ACTION_UP:
                Log.e(TAG, "event : up");
                deltaDistanceX = deltaDistanceY = 0;
                final VelocityTracker velocityTracker = mVelocityTracker;
                /** 计算当前速度 */
                velocityTracker.computeCurrentVelocity(1000);
                /** 获取当前x方向的速度 */
                int velocityX = (int) velocityTracker.getXVelocity();
                Log.e(TAG, "velocityX:" + velocityX);
                if (velocityX > SNAP_VELOCITY) { // 向右滑动并且手指滑动速度大于指定的速度(此时速度的方向为正)
                    // Fling enough to move left
                    Log.e(TAG, "snap left");
                    int targetScreen = (mCurScreen == 0) ? (getChildCount() - 1)
                            : (mCurScreen - 1);
                    snapToScreen(targetScreen);// 滑到前一个页面
                } else if (velocityX < -SNAP_VELOCITY) {// 向左滑动时并且手指滑动的速度也大于指定的速度(此时速度方向为负)
                    // Fling enough to move right
                    Log.e(TAG, "snap right");
                    int targetScreen = (mCurScreen == (getChildCount() - 1)) ? (0)
                            : (mCurScreen + 1);
                    snapToScreen(targetScreen);// 滑到后一个页面
                } else {
                    snapToDestination();
                }
                if (mVelocityTracker != null) {// 释放VelocityTracker对象
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                // }
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
        }

        return true;
    }

    /**
     * 该方法是用于拦截手势事件的，每个手势事件都会先调用 此方法返回false，则手势事件会向子控件传递
     * 返回true，则调用onTouchEvent方法
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e(TAG, "onInterceptTouchEvent-slop:" + mTouchSlop);

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                if (xDiff > mTouchSlop) {
                    mTouchState = TOUCH_STATE_SCROLLING; // 视图还在移动状态

                }
                break;

            case MotionEvent.ACTION_DOWN:
                originalX = mLastMotionX = x;
                originalY = mLastMotionY = y;
                mLastMotionXRemainder = 0;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }

        return mTouchState != TOUCH_STATE_REST;
    }

    @Override
    public void scrollBy(int x, int y) {
        // TODO Auto-generated method stub
//		Log.d("--lijun--", "scrollBy X:" + x + "  Y:" + y);
        super.scrollBy(x, y);
    }

    @Override
    public void scrollTo(int x, int y) {
//		Log.i("--lijun--", "scrollTo X:" + x + "  Y:" + y);
        mCurScrollX = x;
        mCurScrollY = y;
		/*非循环翻页时要用到*/
        boolean isXBeforeFirstPage = x < 0;
        boolean isXAfterLastPage = x > mMaxScrollX;
        mUnboundedScrollX = x;
        if (isXBeforeFirstPage) {
            mOverScrollX = 0;
        } else if (isXAfterLastPage) {
            mOverScrollX = mMaxScrollX;
        } else {
            mOverScrollX = x;
        }
		/*非循环翻页时要用到*/
        super.scrollTo(x, y);
    }

    public int getScrollForPage(int index) {
        if (mPageScrolls == null || index >= mPageScrolls.length || index < 0) {
            return 0;
        } else {
            return mPageScrolls[index];
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        int halfScreenSize = getViewportWidth() / 2;
        screenCenter = getScrollX() + halfScreenSize;

        if (screenCenter != mLastScreenCenter) {
            // set mForceScreenScrolled before calling screenScrolled so that
            // screenScrolled can
            // set it for the next frame
            screenScrolled(screenCenter);
            mLastScreenCenter = screenCenter;
        }
        drawChildren(canvas);

    }

    private void drawChildren(Canvas canvas) {
        final int pageCount = getChildCount();
        if(pageCount == 0 )return;

        if(needHideChildren()){
            if(mScrollStyle == SCROLL_STYLE_SPHERE){
                drawScrollSphere(canvas);
            }else if(mScrollStyle == SCROLL_STYLE_COLUMNAR){

            }
        } else {
            getVisiblePages(mTempVisiblePagesRange);
            final int leftScreen = mTempVisiblePagesRange[0];
            final int rightScreen = mTempVisiblePagesRange[1];

            if (leftScreen != -1 && rightScreen != -1) {
                final long drawingTime = getDrawingTime();

                if (rightScreen < leftScreen) {
                    canvas.save();
                    int width = this.getMeasuredWidth();
                    int offset = pageCount * width;
                    if (getScrollX() > mMaxScrollX) {
                        drawChild(canvas, getPageAt(leftScreen), drawingTime);
                        canvas.translate(+offset, 0);
                        drawChild(canvas, getPageAt(rightScreen), drawingTime);
                        // canvas.translate(-offset, 0);

                    } else if (getScrollX() < 0) {
                        drawChild(canvas, getPageAt(rightScreen), drawingTime);
                        canvas.translate(-offset, 0);
                        drawChild(canvas, getPageAt(leftScreen), drawingTime);
                        // canvas.translate(+offset, 0);

                    }
                    canvas.restore();
                } else {
                    canvas.save();
                    canvas.clipRect(getScrollX(), getScrollY(), getScrollX()
                            + getRight() - getLeft(), getScrollY()
                            + getBottom() - getTop());

                    // Draw all the children, leaving the drag view for last
                    for (int i = pageCount - 1; i >= 0; i--) {
                        final View v = getPageAt(i);
                        if (mForceDrawAllChildrenNextFrame
                                || (leftScreen <= i && i <= rightScreen)) {
                            drawChild(canvas, v, drawingTime);
                        }
                    }
                    mForceDrawAllChildrenNextFrame = false;
                    canvas.restore();
                }
            }
        }



    }

    /*
     * 绘制球面
     */
    private void drawScrollSphere(Canvas canvas){
        //防抖
        if(Math.abs(deltaDistanceX) < 4){
            deltaDistanceX = 0;
        }
        if(Math.abs(deltaDistanceY) < 4){
            deltaDistanceY = 0;
        }
        if(!sphereInitialized){
            initSphere();
        }
        if(sphereInitialized){
//			currentDegreeX = (int) (((float)mCurScrollX/getViewportWidth()*180 + 180)%180);
            currentDegreeX = -(int)((float)deltaDistanceX/getViewportWidth()*180.0f);

            getVisiblePages(mTempVisiblePagesRange);
            final int leftScreen = mTempVisiblePagesRange[0];
            final int rightScreen = mTempVisiblePagesRange[1];
//			Log.d("--lijun", "currentDegreeX : " + currentDegreeX);
//			if(mTouchState != TOUCH_STATE_REST && sphereDrawHull){//手指滑动过程，画完整的球面
//
//			}
            if(currentDegreeX > 180 - prepare){//180 ~ 170
                drawHalfSphereNextPage(canvas, mLayoutBitmapPieces[rightScreen].pieces, false);
            }else if(currentDegreeX > 90){//170 ~ 90
                drawHalfSphereCurrentPage(canvas, mLayoutBitmapPieces[leftScreen].pieces, false);
                drawHalfSphereNextPage(canvas, mLayoutBitmapPieces[rightScreen].pieces, true);
            }else if(currentDegreeX > prepare){//90 ~ 10
                sphereDrawHull = true;
                drawHalfSphereNextPage(canvas, mLayoutBitmapPieces[rightScreen].pieces, true);
                drawHalfSphereCurrentPage(canvas, mLayoutBitmapPieces[leftScreen].pieces, false);
            }else if(currentDegreeX > -prepare){//10 ~ -10
                drawHalfSphereCurrentPage(canvas, mLayoutBitmapPieces[mCurScreen].pieces, true);
            }else if(currentDegreeX > -90){//-10 ~ -90
                sphereDrawHull = true;
                drawHalfSphereNextPage(canvas, mLayoutBitmapPieces[leftScreen].pieces, false);
                drawHalfSphereCurrentPage(canvas, mLayoutBitmapPieces[rightScreen].pieces, true);
            }else if(currentDegreeX > (-180+prepare)){//-90 ~ -170
                drawHalfSphereCurrentPage(canvas, mLayoutBitmapPieces[rightScreen].pieces, true);
                drawHalfSphereNextPage(canvas, mLayoutBitmapPieces[leftScreen].pieces, false);
            }else if(currentDegreeX >= -180){//-170 ~ -180
                drawHalfSphereNextPage(canvas, mLayoutBitmapPieces[leftScreen].pieces, false);
            }
        }
    }

    /*
     * 绘制球面
     */
    private void drawScrollColumnar(Canvas canvas) {
        // 防抖
        if (Math.abs(deltaDistanceX) < 4) {
            deltaDistanceX = 0;
        }
        if (Math.abs(deltaDistanceY) < 4) {
            deltaDistanceY = 0;
        }
        if (!sphereInitialized) {
            initColumnar();
        }
    }

    private boolean hasNullBitmapCache(Bitmap[] bitmaps){
        if(bitmaps == null || bitmaps.length == 0){
            return true;
        }else{
            for(Bitmap bm : bitmaps){
                if(bm == null) return true;
            }
        }
        return false;
    }

    private void screenScrolled(int screenCenter) {
        switch (mScrollStyle) {
            case SCROLL_STYLE_CLASSICS:
                break;
            case SCROLL_STYLE_CUBE:
                screenScrolledCUBIC();
                break;
            case SCROLL_STYLE_COLUMNAR:

                break;
            case SCROLL_STYLE_SPHERE:

                break;
            case SCROLL_STYLE_3DROTA:
                screenScrolled3DRota();
                break;

            default:
                break;
        }
    }

    private static float ROTATIONZOOM = 0.9f;
    private static float LEFTPIVOTZOOM = 0.8f;
    private static float RIGHTPIVOTZOOM = 0.2f;
    private void screenScrolled3DRota() {
        currentDegree = (int) (((float)mCurScrollX/getViewportWidth()*90 + 180)%90);
        currentobDegree = 90 - currentDegree;
//		Log.d("--lijun--", "degree:"+currentDegree);
        getVisiblePages(mTempVisiblePagesRange);
        final int leftScreen = mTempVisiblePagesRange[0];
        final int rightScreen = mTempVisiblePagesRange[1];
        LJCellLayout cellLayoutLeft = (LJCellLayout) getChildAt(leftScreen);
        LJCellLayout cellLayoutRight = (LJCellLayout) getChildAt(rightScreen);
        if (currentDegree != 0) {
            cellLayoutLeft.setRotationY((float) ROTATIONZOOM * currentDegree);
            cellLayoutLeft.setOverScrollAmount((float) currentDegree / 90,false);
            cellLayoutLeft.setCameraDistance(mDensity * mCameraDistance);
            cellLayoutLeft.setPivotX(cellLayoutLeft.getMeasuredWidth()* LEFTPIVOTZOOM);
            cellLayoutLeft.setPivotY(cellLayoutLeft.getMeasuredHeight() * 0.5f);
            cellLayoutLeft.setScaleX((float) (0.6 + 0.4f / 90 * (currentobDegree)));
            cellLayoutLeft.setScaleY((float) (0.6 + 0.4f / 90 * (currentobDegree)));


            cellLayoutRight.setRotationY(-(float) ROTATIONZOOM* (currentobDegree));
            cellLayoutRight.setOverScrollAmount((float) (currentobDegree) / 90, true);
            cellLayoutRight.setCameraDistance(mDensity * mCameraDistance);
            cellLayoutRight.setPivotX(cellLayoutRight.getMeasuredWidth()* RIGHTPIVOTZOOM);
            cellLayoutRight.setPivotY(cellLayoutRight.getMeasuredHeight() * 0.5f);
            cellLayoutRight.setScaleX((float) (0.6 + 0.4f / 90 * currentDegree));
            cellLayoutRight.setScaleY((float) (0.6 + 0.4f / 90 * currentDegree));
        }else{
            cellLayoutLeft.resetOverscrollTransforms();
            cellLayoutRight.resetOverscrollTransforms();
        }
    }

    private void screenScrolledCUBIC() {
        currentDegree = (int) (((float)mCurScrollX/getViewportWidth()*90 + 180)%90);
        currentobDegree = 90 - currentDegree;
        getVisiblePages(mTempVisiblePagesRange);
        final int leftScreen = mTempVisiblePagesRange[0];
        final int rightScreen = mTempVisiblePagesRange[1];
        LJCellLayout cellLayoutLeft = (LJCellLayout) getChildAt(leftScreen);
        LJCellLayout cellLayoutRight = (LJCellLayout) getChildAt(rightScreen);
        if (currentDegree != 0) {
            cellLayoutLeft.setRotationY(-(float) currentDegree);
            cellLayoutLeft.setCameraDistance(mDensity * mCameraDistance);
            cellLayoutLeft.setPivotX(cellLayoutLeft.getMeasuredWidth());


            cellLayoutRight.setRotationY((float) (currentobDegree));
            cellLayoutRight.setCameraDistance(mDensity * mCameraDistance);
            cellLayoutRight.setPivotX(0);
        }else{
            cellLayoutLeft.resetCubeScroll();
            cellLayoutRight.resetCubeScroll();
        }
    }

    /*
     * 获取当前显示的页面
     */
    protected void getVisiblePages(int[] range) {
        final int pageCount = getChildCount();
        mTmpIntPoint[0] = mTmpIntPoint[1] = 0;
        range[0] = -1;
        range[1] = -1;
        if (pageCount > 0) {
            // Log.i("--lijun--", "getScrollX():"+getScrollX());
            if (getScrollX() < 0 || getScrollX() > mMaxScrollX) {
                range[0] = pageCount - 1;
                range[1] = 0;
            } else {
                int leftScreen = 0;
                int rightScreen = 0;

                int endIndex = pageCount - 1;
                View currPage = getPageAt(leftScreen);

                while (leftScreen != endIndex
                        && currPage.getX() + currPage.getMeasuredWidth()
                        - currPage.getPaddingRight() <= getScrollX()) {
                    leftScreen++;
                    currPage = getPageAt(leftScreen);
                }
                rightScreen = leftScreen;
                if (rightScreen == endIndex && getScrollX() > mMaxScrollX) {
                    rightScreen = 0;
                } else {
                    currPage = getPageAt(rightScreen + 1);
                    while (rightScreen != endIndex
                            && currPage.getX() - currPage.getPaddingLeft() < getScrollX()
                            + getViewportWidth()) {
                        rightScreen++;
                        currPage = getPageAt(rightScreen + 1);
                    }
                }
                range[0] = leftScreen;
                range[1] = rightScreen;
            }
        } else {
            range[0] = -1;
            range[1] = -1;
        }
    }

    int getViewportWidth() {
        return mViewport.width();
    }

    int getViewportHeight() {
        return mViewport.height();
    }

    View getPageAt(int index) {
        return getChildAt(index);
    }

    void setCurrentPage(int currentPage) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            mNextScreen = -1;
        }
        if (getChildCount() == 0) {
            return;
        }
        mCurScreen = Math.max(0, Math.min(currentPage, getChildCount() - 1));
        updateCurrentPageScroll();
        invalidate();
    }

    private void updateCurrentPageScroll() {
        // If the current page is invalid, just reset the scroll position to
        // zero
        int newX = 0;
        if (0 <= mCurScreen && mCurScreen < getChildCount()) {
            newX = getScrollForPage(mCurScreen);
        }
        scrollTo(newX, 0);
        mScroller.setFinalX(newX);
        mScroller.forceFinished(true);
    }
    public void scrollStyleChanged(int style){
        mScrollStyle = style;
        for(int i = 0 ; i < getChildCount() ; i ++){
            LJCellLayout celllayout = (LJCellLayout) getChildAt(i);
            celllayout.resetOverscrollTransforms();
            if(style == SCROLL_STYLE_3DROTA){
                celllayout.resetOverscrollTransforms();
            }else if(style == SCROLL_STYLE_CUBE){
                celllayout.resetOverscrollTransforms();
                celllayout.setOverScrollAmount(0, true);
            }
        }
        switch (style) {
            case SCROLL_STYLE_CLASSICS:
            case SCROLL_STYLE_CUBE:
            case SCROLL_STYLE_3DROTA:
                break;
            case SCROLL_STYLE_COLUMNAR:
            case SCROLL_STYLE_SPHERE:
                initSphere();
                break;
            default:
                break;
        }
        invalidate();
    }
    /*
     * 得到每页缓存图片
     */
    public Bitmap[] getBitmapCache(){
        Bitmap[] bts = new Bitmap[getChildCount()];
        for(int i = 0 ; i < getChildCount() ; i ++){
            LJCellLayout celllayout = (LJCellLayout) getChildAt(i);
            celllayout.buildDrawingCache();
            bts[i] = celllayout.getDrawingCache();
        }
        return bts;
    }

    private boolean needHideChildren(){
        //如果手指离开屏幕状态下，始终绘制celllayout
        if(mTouchState == TOUCH_STATE_REST && mScroller.isFinished())return false;
        switch (mScrollStyle) {
            case SCROLL_STYLE_CLASSICS:
            case SCROLL_STYLE_CUBE:
            case SCROLL_STYLE_3DROTA:
                return false;
            case SCROLL_STYLE_COLUMNAR:
            case SCROLL_STYLE_SPHERE:
                //柱状，球面翻页时隐藏celllayout
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * 每一页的图像分割封装类
     *
     */
    class LayoutBitmapPieces{
        Bitmap oriBitmap;//原始图片
        public List<Bitmap> pieces;//切割后的图片

        public LayoutBitmapPieces(Bitmap bitmap,int row , int column){
            pieces = Utilities.split(bitmap, row, column);
        }
    }

    private void initColumnar(){
        mCelllayoutsBitmapCache = getBitmapCache();
        if(!hasNullBitmapCache(mCelllayoutsBitmapCache)){
            int count = mCelllayoutsBitmapCache.length;
            mLayoutBitmapPieces = new LayoutBitmapPieces[count];
            for(int i = 0 ; i < count ; i ++){
                mLayoutBitmapPieces[i] = new LayoutBitmapPieces(mCelllayoutsBitmapCache[i], LJLauncher.ROWS, LJLauncher.COLUMNS);
            }

            oldCoordX = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
            oldCoordY = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
            //球的半径
            mRadius = (getMeasuredWidth() - getMeasuredWidth() / LJLauncher.COLUMNS) / 2.0f;
            perAngle =  Math.PI / LJLauncher.ROWS;//π/rows 180度平均划分为ROWS个经度，每个经度度数递增
            /**
             * 获取y轴偏移的z轴坐标
             * 获取绕x轴旋转多少度
             * 获取y轴坐标
             */
            coordZ1 = getCoordZ1();
            rotateX = getRotateX();
            coordY = getCoordY();
            /**
             * 计算bitmap现在所处的坐标
             */
            oldCoordY = getOldCoordY();

            perDegree = 180/LJLauncher.COLUMNS;

            sphereInitialized = true;

            ColorMatrix cm = new ColorMatrix();
            float array[] = {
                    0.4f,   0,      0,      0,      100,
                    0,      0.4f,   0,      0,      100,
                    0,      0,      0.4f,   0,      100,
                    0,      0,      0,      1.0f,   0
            };
            cm.set(array);
            mColorMatrixFilter = new ColorMatrixColorFilter(cm);
        }
    }

    private void initSphere(){
        mCelllayoutsBitmapCache = getBitmapCache();
        if(!hasNullBitmapCache(mCelllayoutsBitmapCache)){
            int count = mCelllayoutsBitmapCache.length;
            mLayoutBitmapPieces = new LayoutBitmapPieces[count];
            for(int i = 0 ; i < count ; i ++){
                mLayoutBitmapPieces[i] = new LayoutBitmapPieces(mCelllayoutsBitmapCache[i], LJLauncher.ROWS, LJLauncher.COLUMNS);
            }

            oldCoordX = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
            oldCoordY = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
            //球的半径
            mRadius = (getMeasuredWidth() - getMeasuredWidth() / LJLauncher.COLUMNS) / 2.0f;
            perAngle =  Math.PI / LJLauncher.ROWS;//π/rows 180度平均划分为ROWS个经度，每个经度度数递增
            /**
             * 获取y轴偏移的z轴坐标
             * 获取绕x轴旋转多少度
             * 获取y轴坐标
             */
            coordZ1 = getCoordZ1();
            rotateX = getRotateX();
            coordY = getCoordY();
            /**
             * 计算bitmap现在所处的坐标
             */
            oldCoordY = getOldCoordY();

            perDegree = 180/LJLauncher.COLUMNS;

            sphereInitialized = true;

            ColorMatrix cm = new ColorMatrix();
            float array[] = {
                    0.4f,   0,      0,      0,      100,
                    0,      0.4f,   0,      0,      100,
                    0,      0,      0.4f,   0,      100,
                    0,      0,      0,      1.0f,   0
            };
            cm.set(array);
            mColorMatrixFilter = new ColorMatrixColorFilter(cm);
        }
    }

    /**
     * 绘制球面
     * 球面直角坐标系方程： （x-a）²+（y-b）²+（z-c）²=R
     * 球面极坐标方程：
     * x = x0 + R * sinθ * cosφ
     * y = y0 + R * sinθ * sinφ
     * z = z0 + R * cosθ
     */

    /**
     *
     * @param canvas            画布
     * @param bitmap            位图
     * @param coordX            x坐标
     * @param coordY            y坐标
     * @param rotateX           绕x轴旋转度数
     * @param rotateY           绕y轴旋转度数
     * @param coordZ1           相对与y轴上的bitmap的z轴上的偏移
     * @param coordZ2           相对与x轴上的bitmap的z轴上的偏移
     * @param oldCoordX         原始x坐标
     * @param oldCoordY         原始y坐标
     * @param transformRatio    滑动比率
     * @param mPaint            画笔
     */
    private void drawCanvas(Canvas canvas, Bitmap bitmap, float coordX, float coordY, float rotateX, float rotateY, float coordZ1, float coordZ2, float oldCoordX, float oldCoordY, float transformRatio, Paint mPaint){
        mMatrix.reset();
        canvas.save();
        mCamera.save();

        mCamera.translate(0.0f, 0.0f, coordZ2 * transformRatio);
        mCamera.rotateY(rotateY * transformRatio);
        mCamera.translate(0.0f, 0.0f, coordZ1 * transformRatio);
        mCamera.rotateX(rotateX * transformRatio);
        mCamera.getMatrix(mMatrix);
//       mMatrix.preScale(1 - (1 - scaleX) * transformRatio, 1 - (1 - scaleY) * transformRatio);
        mMatrix.preTranslate(-(bitmap.getWidth() / 2), -(bitmap.getHeight() / 2));
        mMatrix.postTranslate(+(bitmap.getWidth() / 2), +(bitmap.getHeight() / 2));
        mMatrix.postTranslate(oldCoordX + (coordX - oldCoordX) * transformRatio, oldCoordY + (coordY - oldCoordY) * transformRatio);
        canvas.drawBitmap(bitmap, mMatrix, mPaint);

        mCamera.restore();
        canvas.restore();
    }

    /**
     * bitmap相对于y轴的bitmap的z轴上的距离
     * @return
     */
    private float[] getCoordZ1(){
        float[] coordZ = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
        float centerY = getMeasuredHeight() / 2.0f - getMeasuredHeight() / LJLauncher.ROWS / 2;
        for(int i = 0;i < LJLauncher.ROWS;i++){
            for(int j = 0;j < LJLauncher.COLUMNS;j++){
                /**
                 * x        :   centerX - cos α * R
                 * y        :   centerY - cos α * R
                 * z        :   centerY - sin α * R
                 * rotateX  :   90 - α
                 */
                double realAngle1 = i * perAngle + perAngle/2;
                coordZ[i * LJLauncher.COLUMNS + j] = (float) (centerY - Math.sin(realAngle1) * mRadius);
            }
        }
        return coordZ;
    }

    /**
     * bitmap相对于x轴的bitmap的z轴上的距离
     * @param startAngle
     * @return
     */
    private float[] getCoordZ2(double startAngle){
        float[] coordZ = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
        float centerY = getMeasuredHeight() / 2.0f - getMeasuredHeight() / LJLauncher.ROWS / 2;
        for(int i = 0;i < LJLauncher.ROWS;i++){
            for(int j = 0;j < LJLauncher.COLUMNS;j++){
                /**
                 * x        :   centerX - cos α * R
                 * y        :   centerY - cos α * R
                 * z        :   centerY - sin α * R
                 * rotateX  :   90 - α
                 */
                double realAngle = j * perAngle + perAngle/2 + startAngle * Math.PI / 180;
                coordZ[i * LJLauncher.COLUMNS + j] = (float) (centerY - Math.sin(realAngle) * mRadius);
            }
        }
        return coordZ;
    }

    /**
     * bitmap围绕x轴旋转多少度
     * @return
     */
    private float[] getRotateX(){
        float[] rotateX = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
        for(int i = 0;i < LJLauncher.ROWS;i++){
            for(int j = 0;j < LJLauncher.COLUMNS;j++){
                /**
                 * x        :   centerX - cos α * R
                 * y        :   centerY - cos α * R
                 * z        :   centerY - sin α * R
                 * rotateX  :   90 - α
                 */
                double realAngle = i * perAngle + perAngle/2;
                rotateX[i * LJLauncher.COLUMNS + j] = (float) (90 - (realAngle / (Math.PI / 180)));
            }
        }
        return rotateX;
    }

    /**
     * bitmap围绕y轴旋转多少度
     * @param startAngle
     * @return
     */
    private float[] getRotateY(double startAngle){
        float[] rotateY = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];

        for(int i = 0;i < LJLauncher.ROWS;i++){
            for(int j = 0;j < LJLauncher.COLUMNS;j++){
                /**
                 * x        :   centerX - cos α * R
                 * y        :   centerY - cos α * R
                 * z        :   centerY - sin α * R
                 * rotateX  :   90 - α
                 */
                double realAngle = j * perAngle + perAngle/2 + startAngle * Math.PI / 180;
                rotateY[i * LJLauncher.COLUMNS + j] = (float) ((realAngle / (Math.PI / 180)) - 90);
            }
        }
        return rotateY;
    }

    /**
     * bitmap所在的x坐标
     * @param startAngle
     * @return
     */
    private float[] getCoordX(double startAngle){
        float[] coordX = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
        float centerX = screenCenter;

        for(int i = 0;i < LJLauncher.ROWS;i++){
            for(int j = 0;j < LJLauncher.COLUMNS;j++){
                /**
                 * x        :   centerX - cos α * R
                 * y        :   centerY - cos α * R
                 * z        :   centerY - sin α * R
                 * rotateX  :   90 - α
                 */
                double realAngle = j * perAngle + perAngle/2 + startAngle * Math.PI / 180;
                coordX[i * LJLauncher.COLUMNS + j] = (float) (centerX - Math.cos(realAngle) * mRadius);
            }
        }

        return coordX;
    }

    /**
     * bitmap所在的y坐标
     * @return
     */
    private float[] getCoordY(){
        float[] coordY = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
        float centerY = getMeasuredHeight() / 2.0f - getMeasuredHeight() / LJLauncher.ROWS / 2;
        for(int i = 0;i < LJLauncher.ROWS;i++){
            for(int j = 0;j < LJLauncher.COLUMNS;j++){
                /**
                 * x        :   centerX - cos α * R
                 * y        :   centerY - cos α * R
                 * z        :   centerY - sin α * R
                 * rotateX  :   90 - α
                 */
                double realAngle = i * perAngle + perAngle/2;
                coordY[i * LJLauncher.COLUMNS + j] = (float) (centerY - Math.cos(realAngle) * mRadius);
            }
        }
        return coordY;
    }

    private float[] getOldCoordX(){
        float[] oldX = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
        int width = getViewportWidth();
        int height = getViewportHeight();
        int perW = width/LJLauncher.COLUMNS;
        int perH = height/LJLauncher.ROWS;
        for(int i = 0;i < LJLauncher.ROWS;i++){
            for(int j = 0;j < LJLauncher.COLUMNS;j++){
                oldX[i*LJLauncher.COLUMNS + j] = j*perW + getScrollX();
            }
        }
        return oldX;
    }
    private float[] getOldCoordY(){
        float[] oldY = new float[LJLauncher.ROWS * LJLauncher.COLUMNS];
        int width = getViewportWidth();
        int height = getViewportHeight();
        int perW = width/LJLauncher.COLUMNS;
        int perH = height/LJLauncher.ROWS;
        for(int i = 0;i < LJLauncher.ROWS;i++){
            for(int j = 0;j < LJLauncher.COLUMNS;j++){
                oldY[i*LJLauncher.COLUMNS + j] = i*perH;
            }
        }
        return oldY;
    }

    /**
     * @param canvas
     * @param bitmaps
     * @param isMirror 是否倒序画
     */
    private void drawHalfSphereCurrentPage(Canvas canvas, List<Bitmap> bitmaps,
                                           boolean isMirror) {
        int size = bitmaps.size();
        float[] coordZ2;
        float[] rotateY;
        float[] coordX;
        int start, end, per;
        boolean needFiltercolor = false;
        int feg = 180;
        if (isMirror) {
            start = size - 1;
            end = -1;
            per = -1;
        } else {
            start = 0;
            end = size;
            per = 1;
        }
        int i = start;
        float transformRatio;
        float drawTransformRatio = 1.0f;
        oldCoordX = getOldCoordX();
        if(currentDegreeX > 180 - prepare){//180 ~ 170
            return;
        }else if(currentDegreeX > prepare){//170 ~ 10
            feg = -180;
            needFiltercolor = true;
            transformRatio = (currentDegreeX - prepare)/(float)(180 - 2*prepare);
//			Log.i("--lijun", "startAngel:"+transformRatio * feg);
            coordZ2 = getCoordZ2(transformRatio * feg);
            rotateY = getRotateY(transformRatio * feg);
            coordX = getCoordX(transformRatio * feg);
        }else if(currentDegreeX > -prepare){//10 ~ -10
            transformRatio = Math.abs(currentDegreeX/(float)prepare);
            coordZ2 = getCoordZ2(0);
            rotateY = getRotateY(0);
            coordX = getCoordX(0);
            drawTransformRatio = transformRatio;
        }else if(currentDegreeX > (-180+prepare)){//-10 ~ -170
            feg = 180;
            needFiltercolor = true;
            transformRatio = (-currentDegreeX - prepare)/(float)(180 - 2*prepare);
//			Log.i("--lijun", "startAngel:"+(1-transformRatio) * feg);
            coordZ2 = getCoordZ2(transformRatio * feg);
            rotateY = getRotateY(transformRatio * feg);
            coordX = getCoordX(transformRatio * feg);
        }else{//-170 ~ -180
            return;
        }
        while (i != end) {
            if (needFiltercolor && coordZ2[i] > mRadius) {
                mSpherePaint.setColorFilter(mColorMatrixFilter);
            } else {
                mSpherePaint.setColorFilter(null);
            }
            drawCanvas(canvas, bitmaps.get(i),
                    coordX[i], coordY[i],
                    rotateX[i], rotateY[i],
                    coordZ1[i], coordZ2[i],
                    oldCoordX[i], oldCoordY[i],
                    drawTransformRatio, mSpherePaint);

            i = i + per;
        }
    }

    /**
     * @param canvas
     * @param bitmaps
     * @param isMirror 是否倒序画
     */
    private void drawHalfSphereNextPage(Canvas canvas, List<Bitmap> bitmaps,
                                        boolean isMirror) {
        int size = bitmaps.size();
        float[] coordZ2;
        float[] rotateY;
        float[] coordX;
        int start, end, per;
        boolean needFiltercolor = false;
        int feg = 180;
        if (isMirror) {
            start = size - 1;
            end = -1;
            per = -1;
        } else {
            start = 0;
            end = size;
            per = 1;
        }
        int i = start;
        float transformRatio;
        float drawTransformRatio = 1.0f;
        oldCoordX = getOldCoordX();
        if(currentDegreeX > 180 - prepare){//180 ~ 170
            transformRatio = Math.abs((180-currentDegreeX)/(float)prepare);
            coordZ2 = getCoordZ2(0);
            rotateY = getRotateY(0);
            coordX = getCoordX(0);
            drawTransformRatio = transformRatio;
        }else if(currentDegreeX > prepare){//170 ~ 10
            feg = 180;
            needFiltercolor = true;
            transformRatio = (currentDegreeX - prepare)/(float)(180 - 2*prepare);
//				Log.i("--lijun", "startAngel:"+(1-transformRatio)* feg);
            coordZ2 = getCoordZ2((1-transformRatio) * feg);
            rotateY = getRotateY((1-transformRatio) * feg);
            coordX = getCoordX((1-transformRatio) * feg);
        }else if(currentDegreeX > -prepare){//10 ~ -10
            return;
        }else if(currentDegreeX > (-180+prepare)){//-10 ~ -170
            feg = -180;
            needFiltercolor = true;
            transformRatio = (-currentDegreeX - prepare)/(float)(180 - 2*prepare);
//				Log.i("--lijun", "startAngel:"+(1-transformRatio) * feg);
            coordZ2 = getCoordZ2((1-transformRatio) * feg);
            rotateY = getRotateY((1-transformRatio) * feg);
            coordX = getCoordX((1-transformRatio) * feg);
        }else{//-170 ~ -180
            transformRatio = Math.abs((currentDegreeX+180)/(float)prepare);
            coordZ2 = getCoordZ2(0);
            rotateY = getRotateY(0);
            coordX = getCoordX(0);
            drawTransformRatio = transformRatio;
        }
        while (i != end) {
            if (needFiltercolor && coordZ2[i] > mRadius) {
                mSpherePaint.setColorFilter(mColorMatrixFilter);
            } else {
                mSpherePaint.setColorFilter(null);
            }
            drawCanvas(canvas, bitmaps.get(i), coordX[i], coordY[i],
                    rotateX[i], rotateY[i], coordZ1[i], coordZ2[i],
                    oldCoordX[i], oldCoordY[i], drawTransformRatio, mSpherePaint);

            i = i + per;
        }
    }
}