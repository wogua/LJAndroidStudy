package com.lijun.shapeimage.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import com.lijun.androidstudy.util.PhotoUtils;

import java.lang.ref.WeakReference;

/**
 * Created by lijun on 17-10-10.
 */

public class ComposeRenderShapeView extends CustomShapeImageView {
    private static final String TAG = ComposeRenderShapeView.class.getSimpleName();
    private Paint mShaderPaint;
    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    private int mMaxSide;

    private static final int[] mColors = new int[] {
            Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE
    };
    private static final float[] mPositions = new float[] {
            0, 0.6f, 1f
    };

    public ComposeRenderShapeView(Context context) {
        this(context,null);
    }

    public ComposeRenderShapeView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ComposeRenderShapeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mShaderPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mMaxSide = mWidth > mHeight ? mWidth : mHeight;
        createShader();
    }

    private void createShader() {
        Drawable drawable = getDrawable();
        mBitmap = PhotoUtils.drawable2bitmap(drawable);
        // 创建位图渲染
        BitmapShader bitmapShader = new BitmapShader(mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        // 创建环形渐变
//        RadialGradient radialGradient = new RadialGradient(mWidth / 2, mHeight / 2, mMaxSide / 2,
//                Color.TRANSPARENT, Color.WHITE, Shader.TileMode.MIRROR);
        RadialGradient radialGradient = new RadialGradient(mWidth / 2, mHeight / 2, mMaxSide / 2,
                mColors, mPositions, Shader.TileMode.MIRROR);
        // 创建组合渐变，由于直接按原样绘制就好，所以选择Mode.SRC_OVER
        ComposeShader composeShader = new ComposeShader(bitmapShader, radialGradient,
                PorterDuff.Mode.SRC_OVER);
        // 将组合渐变设置给paint
        mShaderPaint.setShader(composeShader);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInEditMode()) {
            canvas.drawCircle(mWidth/2,mHeight/2,mMaxSide/2,mShaderPaint);
        } else {
            super.onDraw(canvas);
        }
    }
}
