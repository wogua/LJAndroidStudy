package com.lijun.androidstudy.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by lijun on 17-10-10.
 */

public class PhotoUtils {

    public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawable2bitmap(drawable); // drawable 转换成 bitmap
        Matrix matrix = new Matrix();   // 创建操作图片用的 Matrix 对象
        float scaleWidth = ((float) w / width);   // 计算缩放比例
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);         // 设置缩放比例
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);       // 建立新的 bitmap ，其内容是对原 bitmap 的缩放后的图
        return new BitmapDrawable(newbmp);       // 把 bitmap 转换成 drawable 并返回
    }

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

    public static Bitmap compositeByBitmap(Bitmap srcBmp, Bitmap maskBmp, Bitmap bgBmp, Bitmap zmBmp, boolean isWidget) {
        if (bgBmp == null || maskBmp == null || zmBmp == null) return srcBmp;
        Paint paint = new Paint();
        if (!zmBmp.isMutable()) {
            // 设置图片为背景为透明
            zmBmp = zmBmp.copy(Bitmap.Config.ARGB_8888, true);
        }
        if (!maskBmp.isMutable()) {
            // 设置图片为背景为透明
            maskBmp = maskBmp.copy(Bitmap.Config.ARGB_8888, true);
        }
        if (!srcBmp.isMutable()) {
            // 设置图片为背景为透明
            srcBmp = srcBmp.copy(Bitmap.Config.ARGB_8888, true);
        }
        if (!bgBmp.isMutable()) {
            // 设置图片为背景为透明
            bgBmp = bgBmp.copy(Bitmap.Config.ARGB_8888, true);
        }
        //First Draw the source into background
        Canvas canvas = new Canvas(zmBmp);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.save();
        int srcW = srcBmp.getWidth();
        int srcH = srcBmp.getHeight();
        int bgW = zmBmp.getWidth();
        int bgH = zmBmp.getHeight();
        if (srcW != bgW || srcH != bgH) {
            float scalW = ((float) bgW) / srcW;
            float scalH = ((float) bgH) / srcH;
            float scale = isWidget ? (scalW > scalH ? scalH : scalW) : (scalW < scalH ? scalH : scalW);
            srcBmp = zoom(srcBmp, scale);
        }
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas.drawBitmap(srcBmp, (zmBmp.getWidth() - srcBmp.getWidth()) / 2, (zmBmp.getHeight() - srcBmp.getHeight()) / 2, paint);
        canvas.restore();
        //Second Mask the source which is dealed on first step.
        Canvas canvas2 = new Canvas(maskBmp);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas2.save();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas2.translate((maskBmp.getWidth() - zmBmp.getWidth()) / 2,
                (maskBmp.getHeight() - zmBmp.getHeight()) / 2);
        canvas2.drawBitmap(zmBmp, 0, 0, paint);
        canvas2.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas2.restore();
        paint.setXfermode(null);
        canvas2.setBitmap(null);
        paint.reset();
        //Second Mask the source which is dealed on first step.
        Canvas canvas3 = new Canvas(bgBmp);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas3.save();
        canvas3.translate((bgBmp.getWidth() - maskBmp.getWidth()) / 2,
                (bgBmp.getHeight() - maskBmp.getHeight()) / 2);
        canvas3.drawBitmap(maskBmp, 0, 0, paint);
        canvas3.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas3.restore();
        canvas3.setBitmap(null);
        Log.i("xxxx", "bgBmp = " + bgBmp.getHeight() + " , " + bgBmp.getWidth());
        return bgBmp;
    }

    /**
     * 图标添加Loading...水印
     *
     * @return
     */
    public static Bitmap mergeLoadingBitmap(Bitmap backBitmap) {
        if (backBitmap == null || backBitmap.isRecycled()) {
            return null;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        paint.setColor(Color.parseColor("#aaff0000"));
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        paint.setXfermode(null);
        paint.reset();
        paint.setColor(Color.parseColor("#ffffffff"));
        paint.setTextSize(30);
        Typeface font = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC);
        paint.setTypeface(font);
        canvas.drawText("Loading...", 13, 90, paint);
        canvas.setBitmap(null);
        return bitmap;
    }
}
