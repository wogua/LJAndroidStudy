package com.lijun.progressbar.leafloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lijun on 17-10-14.
 * 水面波浪效果
 */

public class WaveView extends View {

    NextFrameAction nextFrameAction;
    RectF rectF;
    Paint paint;
    Paint paint2;
    Paint paint3;
    Path path;
    Path path1;
    int width;
    int height;
    int w = 0;
    double startTime;
    int waveAmplitude;
    int waveRange;
    int highLevel;

    Paint mPaint, mPaint2;

    PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

    public WaveView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPaint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint2 = new Paint(); //设置一个笔刷大小是3的黄色的画笔
        mPaint2.setAntiAlias(true);
        mPaint2.setColor(Color.BLUE);
        mPaint2.setStyle(Paint.Style.FILL);
        mPaint2.setXfermode(xfermode);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawCircle(width / 2, height / 2, width / 2 - 5, paint2);
        //canvas.drawArc(rectF,90.0f-145.0f/2.0f,145.0f,false,paint);
        canvas.drawPath(path, paint);
        canvas.drawPath(path1, paint3);
        postDelayed(nextFrameAction, 4);
    }

    private void init() {
        nextFrameAction = new NextFrameAction();
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        waveRange = width;
        rectF = new RectF(5, 5, width - 5, height - 5);
        paint = new Paint();
        paint2 = new Paint();
        paint3 = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint3.setAntiAlias(true);
        paint3.setColor(0x99ff0000);

        paint2.setAntiAlias(true);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(5);
        paint2.setColor(Color.RED);
        path = new Path();
        path1 = new Path();
        startTime = System.currentTimeMillis();
        waveAmplitude = 20;
        highLevel = (int) (height * (0.5) + waveAmplitude);
    }

    protected class NextFrameAction implements Runnable {
        @Override
        public void run() {
            path.reset();
            path1.reset();
            path.addArc(rectF, 90.0f - 145.0f / 2.0f, 145.0f);
            path1.addArc(rectF, 90.0f - 145.0f / 2.0f, 145.0f);
            w += 5;
            if (w >= (width - 5) * 2) {
                w = 0;
            }
            for (int i = 5; i < width - 5; i++) {
                path.lineTo(i, (float) (highLevel + waveAmplitude * Math.cos((float) (i + w) / (float) (width - 5) * Math.PI)));
                path1.lineTo(i, (float) (highLevel - waveAmplitude * Math.cos((float) (i + w) / (float) (width - 5) * Math.PI)));
            }
            path.close();
            path1.close();
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        init();
    }
}
