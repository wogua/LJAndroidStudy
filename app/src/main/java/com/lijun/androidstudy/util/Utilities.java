package com.lijun.androidstudy.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.View;

public final class Utilities {

    public static int screenWidth = 480;
    public static int screenHeight = 800;


    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param root The root view to make the coordinates relative to.
     * @param coord The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     *         this scale factor is assumed to be equal in X and Y, and so if at any point this
     *         assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(View descendant,
                                                           View root, int[] coord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = { coord[0], coord[1] };

        View v = descendant;
        while (v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text
            // position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }

            v0.getMatrix().mapPoints(pt);
            pt[0] += v0.getLeft();
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /*
     * 切割bitmap
     */
    public static List<Bitmap> split(Bitmap bitmap, int row, int column) {
        List<Bitmap> pieces = new ArrayList<Bitmap>(row * column);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        final int pieceWidth = width / column;
        final int pieceHeight = height / row;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                int xValue = j * pieceWidth;
                int yValue = i * pieceHeight;
                pieces.add(Bitmap.createBitmap(bitmap, xValue, yValue,pieceWidth, pieceHeight));
            }
        }
        return pieces;
    }

    public static boolean startActivitySafety(Context context, Intent intent){
        try{
            context.startActivity(intent);
        }catch(ActivityNotFoundException ex ){
            return false;
        }
        return true;

    }

    /**
     * 缩放Bitmap
     * @param source bitmap
     * @param w target width
     * @param h target height
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap source, int w, int h) {
        int width = source.getWidth();
        int height = source.getHeight();
        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(source, 0, 0, width, height,
                matrix, true);
        return resizedBitmap;
    }

    /**
     * 获取arrays 中图片资源id
     * @param res
     * @param iconsRes
     * @return
     */
    public static int[] getIds(Resources res, int iconsRes) {
        if (iconsRes == 0) {
            return null;
        }
        TypedArray array = res.obtainTypedArray(iconsRes);
        int n = array.length();
        int ids[] = new int[n];
        for (int i = 0; i < n; ++i) {
            ids[i] = array.getResourceId(i, 0);
        }
        array.recycle();
        return ids;
    }
}