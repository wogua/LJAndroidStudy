package com.lijun.androidstudy.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;

/**
 * Created by lijun on 17-10-10.
 */

public class PhotoUtils {

    public static Bitmap zoom(Bitmap bmpBg, float scale) {
        if (bmpBg == null) return null;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bm = Bitmap.createBitmap(bmpBg, 0, 0, bmpBg.getWidth(), bmpBg.getHeight(), matrix,
                true);
        return bm;
    }

    public static Bitmap zoom(Bitmap bmpBg, float width, float hight) {
        if (bmpBg == null) return null;
        float scaleX = width / bmpBg.getWidth();
        float scaleY = hight / bmpBg.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        Bitmap bm = Bitmap.createBitmap(bmpBg, 0, 0, bmpBg.getWidth(), bmpBg.getHeight(), matrix,
                true);
        return bm;
    }

    public static Bitmap drawable2bitmap(Drawable dw) {
        if (dw == null) return null;
        // 创建新的位图
        int w = dw.getIntrinsicWidth();
        int h = dw.getIntrinsicHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }
        Bitmap bg = Bitmap.createBitmap(dw.getIntrinsicWidth(),
                dw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        // 创建位图画板
        Canvas canvas = new Canvas(bg);
        // 绘制图形
        dw.setBounds(0, 0, dw.getIntrinsicWidth(), dw.getIntrinsicHeight());
        dw.draw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        // 释放资源
        canvas.setBitmap(null);
        return bg;
    }
}
