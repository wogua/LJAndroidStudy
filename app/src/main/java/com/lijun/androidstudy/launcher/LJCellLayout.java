package com.lijun.androidstudy.launcher;

import com.lijun.androidstudy.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class LJCellLayout extends ViewGroup {
    private LJLauncher mContext = null;
    private int mCellGapX,mCellGapY;
    private int mCellWidth,mCellHeight;
    private int mCountX,mCountY;

    private float FOREGROUND_ALPHA_DAMPER = 0.65f;
    private Drawable mOverScrollForegroundDrawable;
    private Drawable mOverScrollLeft;
    private Drawable mOverScrollRight;
    private int mForegroundAlpha = 0;

    private Drawable scrollCubeBg;

    public LJCellLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mContext = (LJLauncher) context;
        init();
    }

    public LJCellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = (LJLauncher) context;
        init();
    }

    public LJCellLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mContext = (LJLauncher) context;
        init();
    }
    private void init(){
        final Resources res = getResources();
        mCountX = LJLauncher.ROWS;
        mCountY = LJLauncher.COLUMNS;
        mOverScrollLeft = res.getDrawable(R.drawable.overscroll_glow_left);
        mOverScrollRight = res.getDrawable(R.drawable.overscroll_glow_right);
        scrollCubeBg = res.getDrawable(R.drawable.scroll_cube_bg);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childWidthSize = widthSize - (getPaddingLeft() + getPaddingRight());
        int childHeightSize = heightSize
                - (getPaddingTop() + getPaddingBottom());
        int cw = childWidthSize / mCountX;
        int ch = childHeightSize / mCountY;
        if (cw != mCellWidth || ch != mCellHeight) {
            mCellWidth = cw;
            mCellHeight = ch;
        }

        int numWidthGaps = mCountX - 1;
        int numHeightGaps = mCountY - 1;
        int hFreeSpace = childWidthSize - (mCountX * mCellWidth);
        int vFreeSpace = childHeightSize - (mCountY * mCellHeight);
        mCellGapX = hFreeSpace / numWidthGaps;
        mCellGapY = vFreeSpace / numHeightGaps;

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        int offset = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() -
                (mCountX * mCellWidth);
        int left = getPaddingLeft() + (int) Math.ceil(offset / 2f);
        int top = getPaddingTop();
        int count = getChildCount();

        if(count == 0 ) {
            return;
        }else{
            if(mCellWidth <= 0 || mCellHeight <= 0)return;
            for(int i = 0 ; i < getChildCount() ; i ++){
                final View childView = getChildAt(i);
                childView.layout(left+mCellGapX + i % mCountY* (mCellWidth + mCellGapX),
                        top+mCellGapY + i / mCountY* (mCellHeight+mCellGapY),
                        left+mCellGapX + i % mCountY* (mCellWidth + mCellGapX) + mCellWidth,
                        top+mCellGapY + i / mCountY* (mCellHeight+mCellGapY) + mCellHeight);
            }
        }
    }
    /*
     * 用于显示背景的半透明效果
     */
    void setOverScrollAmount(float r, boolean left) {
        if (left && mOverScrollForegroundDrawable != mOverScrollLeft) {
            mOverScrollForegroundDrawable = mOverScrollLeft;
        } else if (!left && mOverScrollForegroundDrawable != mOverScrollRight) {
            mOverScrollForegroundDrawable = mOverScrollRight;
        }

        r *= FOREGROUND_ALPHA_DAMPER;
        mForegroundAlpha = (int) Math.round((r * 255));
        mOverScrollForegroundDrawable.setAlpha(mForegroundAlpha);
        invalidate();
    }

    public void resetOverscrollTransforms(){
        setTranslationX(0);
        setRotationY(0);
        setOverScrollAmount(0, false);
        setPivotX(getMeasuredWidth() / 2);
        setPivotY(getMeasuredHeight() / 2);
        setScaleX(1);
        setScaleY(1);
    }

    public void resetCubeScroll(){
        setRotationY(0);
        setPivotX(getMeasuredWidth() / 2);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);
        int scrollstyle = mContext.getScrollStyle();
        if (scrollstyle == LJWorkSpace.SCROLL_STYLE_3DROTA) {
            if(mForegroundAlpha > 0){
                mOverScrollForegroundDrawable.setBounds(0, 0, getRight(), getBottom());
                mOverScrollForegroundDrawable.draw(canvas);
            }
        }else if(scrollstyle == LJWorkSpace.SCROLL_STYLE_CUBE){
            if(scrollCubeBg != null){
//        		Log.d("--lijun--", "draw scrollCubeBg");
                scrollCubeBg.setBounds(0, 0, getRight(), getBottom());
                scrollCubeBg.draw(canvas);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
    }
}