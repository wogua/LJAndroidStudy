package com.lijun.androidstudy.upshadowlockscreen;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.util.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class USLockScreenView extends FrameLayout {

    LinearGradient lg;
    TransitionDrawable td;

    Paint p = new Paint();
    Paint mEraserPaint = new Paint();

    private static Bitmap mTopPic;
    private static Bitmap mShadePic;
    private static Bitmap mShadePicTemp;
    private static Bitmap mLastPartShadePicTemp;
    private int currentShadeY;
    private Bitmap mBg;
    private float startY;
    private float currentY;
    private float relativeY;
    private static boolean SLIDING = false;
    private static int ShadePicHight = 200;
    private int screenWidth, screenHigh;

    private int[] mPixels;
    private int[] mNewPixels;
    private int[] mLastPartPixels;
    private int[] mLastPartNewPixels;
    private float perAlpha;

    private boolean UNLOCKED = false;

    public USLockScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public USLockScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public USLockScreenView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        screenWidth = metric.widthPixels;
        screenHigh = metric.heightPixels;
        mShadePicTemp = Bitmap.createBitmap(screenWidth, ShadePicHight, Bitmap.Config.ARGB_8888);
        mTopPic = mBg = getRes(context);
    }

    public Bitmap getRes(Context context) {
        Resources rec = context.getResources();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) rec
                .getDrawable(R.drawable.front_bg);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Bitmap resizeBitmap = Utilities.resizeBitmap(bitmap, screenWidth, screenHigh);
        return resizeBitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTopPic != null) {
            canvas.drawBitmap(mTopPic, 0, 0, null);
        }
        if (mShadePic != null) {
            canvas.drawBitmap(mShadePic, 0, currentShadeY, null);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (UNLOCKED)
            return true;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTopPic = mBg;
            mShadePic = null;
            startY = currentY = event.getY();
            SLIDING = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            SLIDING = false;
            currentY = screenHigh;
            relativeY = 0;
            mTopPic = mBg;
            mShadePic = null;
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            currentY = event.getY();
            relativeY = startY - currentY;
            currentShadeY = (int) (screenHigh - ShadePicHight + 10 - relativeY);
            if (relativeY > 10) {
                clipPic();
                invalidate();
            }
        }

        return super.onTouchEvent(event);
    }

    public void clipPic() {
        if (relativeY <= 5) {
            mTopPic = mBg;
            mShadePic = null;
        } else if (relativeY > (screenHigh - ShadePicHight + 10)) {
            mTopPic = null;
            mShadePic = onShadeLastPart(Bitmap.createBitmap(mBg, 0, 0,
                    screenWidth, ShadePicHight + currentShadeY));
        } else {
            mTopPic = Bitmap
                    .createBitmap(mBg, 0, 0, screenWidth, currentShadeY);
            mShadePic = onShade(Bitmap.createBitmap(mBg, 0, currentShadeY,
                    screenWidth, ShadePicHight));
        }

    }


    private void onTrigger() {
        UNLOCKED = true;
    }

    public void resetTriggler() {
        UNLOCKED = false;
    }

    private Bitmap onShade(Bitmap bm) {
        int w = bm.getWidth();
        int h = bm.getHeight();
        mPixels = new int[w * h];
        mNewPixels = new int[w * h];
        bm.getPixels(mPixels, 0, w, 0, 0, screenWidth, ShadePicHight);
        int pixel, alpha, red, green, blue;
        for (int i = 0; i < h; i++) {
            alpha = 255 - (int) (255 * i / h);
            Log.d("--ljun--", "alpha:" + alpha);
            for (int j = 0; j < w; j++) {
                pixel = mPixels[i * w + j];
                red = (pixel >> 16) & 0xff;
                green = (pixel >> 8) & 0xff;
                blue = (pixel) & 0xff;
                mNewPixels[i * w + j] = (alpha << 24) | (red << 16)
                        | (green << 8) | (blue);
            }
        }
        mShadePicTemp.setPixels(mNewPixels, 0, screenWidth, 0, 0, screenWidth, ShadePicHight);
        return mShadePicTemp;
    }

    private Bitmap onShadeLastPart(Bitmap bm) {
        int w = bm.getWidth();
        int h = bm.getHeight();
        mLastPartPixels = new int[w * h];
        mLastPartNewPixels = new int[w * h];
        mLastPartShadePicTemp = Bitmap.createBitmap(w, h,
                Bitmap.Config.ARGB_8888);
        int aa = (int) ((currentShadeY + ShadePicHight) / (float) ShadePicHight * 255);
        bm.getPixels(mLastPartPixels, 0, w, 0, 0, w, h);
        int pixel, alpha, red, green, blue;
        for (int i = 0; i < h; i++) {
            alpha = aa - (int) (aa * i / h);
            for (int j = 0; j < w; j++) {
                pixel = mPixels[i * w + j];
                red = (pixel >> 16) & 0xff;
                green = (pixel >> 8) & 0xff;
                blue = (pixel) & 0xff;
                mLastPartNewPixels[i * w + j] = (alpha << 24) | (red << 16)
                        | (green << 8) | (blue);
            }
        }
        mLastPartShadePicTemp.setPixels(mLastPartNewPixels, 0, w, 0, 0, w, h);
        return mLastPartShadePicTemp;
    }
}
