package com.lijun.progressbar.leafloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lijun on 17-10-14.
 */

public class TestView extends View {
    Paint mPaint,mPaint2;
    RectF rect = new RectF(0,0,300,300);

    PorterDuffXfermode xfermode=new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

    public TestView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
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

//        canvas.drawRect(rect, mPaint);
//        canvas.drawArc(rect,110,140,false,mPaint);

        canvas.drawRect(rect,mPaint);
        canvas.drawCircle(300,150,150,mPaint2);
    }
}
