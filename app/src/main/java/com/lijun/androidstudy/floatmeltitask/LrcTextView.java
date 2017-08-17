package com.lijun.androidstudy.floatmeltitask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.List;

/**
 * filename:LrcTextView.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-16
 * author: laiyang
 * <p>
 * custom view to show lrc content
 * <p>
 * Modification History
 * -----------------------------------
 * <p>
 * -----------------------------------
 */
public class LrcTextView extends TextView {

    private int viewHeight;

    private int viewWidth;

    private final int HIGH_LIGHT_COLOR = Color.WHITE;

    private final int LOW_LIGHT_COLOR = Color.GRAY;

    private Paint hightLightPaint;

    private Paint lowLightPaint;

    public void setLrcContents(List<LrcContent> lrcContents) {
        this.lrcContents = lrcContents;
    }

    private List<LrcContent> lrcContents;

    public void setIndex(int index) {
        this.index = index;
    }

    private int index;

    public LrcTextView(Context context) {
        super(context);
        init();
    }

    public LrcTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LrcTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setFocusable(true);

        hightLightPaint = new Paint();
        hightLightPaint.setColor(HIGH_LIGHT_COLOR);
        hightLightPaint.setAntiAlias(true);
        hightLightPaint.setTextAlign(Paint.Align.CENTER);//set text center

        lowLightPaint = new Paint();
        lowLightPaint.setColor(LOW_LIGHT_COLOR);
        lowLightPaint.setAntiAlias(true);
        lowLightPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lrcContents == null || lrcContents.size() == 0 || index >= lrcContents.size()) {
            return;
        }
        lowLightPaint.setTextSize(18);
        lowLightPaint.setTypeface(Typeface.DEFAULT);
        hightLightPaint.setTextSize(24);
        hightLightPaint.setTypeface(Typeface.SERIF);

        int temp = viewHeight / 2;//set text height center
        for (int i = index - 1; i >= 0; i--) {//  draw pre lrc
            temp = temp - 25;
            canvas.drawText(lrcContents.get(i).getContent(), viewWidth / 2, temp, lowLightPaint);
        }
        temp = viewHeight / 2;
        // draw current lrc
        canvas.drawText(lrcContents.get(index).getContent(), viewWidth / 2, temp, hightLightPaint);
        // draw after lrc
        for (int i = index + 1; i < lrcContents.size(); i++) {
            temp = temp + 25;
            canvas.drawText(lrcContents.get(i).getContent(), viewWidth / 2, temp, lowLightPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // get view height and width
        viewHeight = h;
        viewWidth = w;
    }
}
