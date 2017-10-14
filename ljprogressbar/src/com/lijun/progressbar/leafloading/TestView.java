package com.lijun.progressbar.leafloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lijun on 17-10-14.
 */

public class TestView extends View {
    Paint mPaint;
    RectF rect = new RectF(0,0,300,300);
    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPaint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {

//        canvas.drawRect(rect, mPaint);
        canvas.drawArc(rect,110,140,false,mPaint);
    }
}
