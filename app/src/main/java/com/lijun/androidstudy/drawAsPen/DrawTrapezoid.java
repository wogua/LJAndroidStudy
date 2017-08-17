package com.lijun.androidstudy.drawAsPen;

import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawTrapezoid extends SurfaceView {
    private static int MAX_WITCH_PIXEL = 30;// 划线的最大宽度
    private static float FRONT_GRADE_RATE = 0.4f;// 渐变粗的长度比例
    private static float BACK_GRADE_RATE = 0.25f;// 渐变粗的长度比例

    ArrayList<FloatPoint> pathPoint = new ArrayList<FloatPoint>();
    private Canvas mBitmapCanvas;
    private Canvas mCanvas;
    private SurfaceHolder sfh;
    private Paint mPaint;
    private Path mPath1;
    private Path mPath2;
    private Rect mInvalidRect = new Rect();
    private Bitmap mBitmap;

    Thread mDrawThread = null;
    private boolean isRecording = false;
    private boolean isDrawing = false;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 3;

    FloatPoint p0, p1, s1, s2, s3, s4;
    Paint mLinePaint = new Paint();

    public final static String ENCODING = "UTF-8";

    static String filename = "/sys/devices/platform/mtk-tpd/gesture";


    public DrawTrapezoid(Context context) {
        super(context);

        // TODO Auto-generated constructor stub
        init();

//		pathPoint = readFromFile(filename);
        //new Thread(mDrawThread).start();

        mDrawThread = new Thread(DrawRunnable);
        mDrawThread.start();
    }

    private void init() {

        mBitmap = Bitmap.createBitmap(
                getResources().getDisplayMetrics().widthPixels, getResources()
                        .getDisplayMetrics().heightPixels, Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);
        sfh = this.getHolder();

        mPath1 = new Path();
        mPath2 = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(1);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        mLinePaint.setColor(Color.RED);
        mLinePaint.setDither(true);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeJoin(Paint.Join.BEVEL);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeWidth(3);

        s1 = new FloatPoint();
        s2 = new FloatPoint();
        s3 = new FloatPoint();
        s4 = new FloatPoint();


    }

    FloatPoint pS1 = new FloatPoint();
    FloatPoint pS2 = new FloatPoint();
    FloatPoint pS3 = new FloatPoint();
    FloatPoint pS4 = new FloatPoint();
    private static final int ORI_FALL = -1;
    private static final int ORI_L_T = 1;
    private static final int ORI_L_B = 2;
    private static final int ORI_R_T = 3;
    private static final int ORI_R_B = 4;
    private static final int ORI_DOWN = 5;
    private static final int ORI_UP = 6;
    private static final int ORI_RIGHT = 7;
    private static final int ORI_LEFT = 8;
    private int curOri = -1;
    private int preOri = -1;
    private FloatPoint prePoint1 = new FloatPoint();
    private FloatPoint prePoint2 = new FloatPoint();
    private FloatPoint p00 = new FloatPoint();

    /**
     * 计算两点的方向，（象限）
     *
     * @param p0
     * @param p1
     * @return
     */
    private int getOrientation(FloatPoint p0, FloatPoint p1) {
        if (p1.x > p0.x && p1.y > p0.y) { // 左上->右下
            return ORI_L_T;
        } else if (p1.x > p0.x && p1.y < p0.y) {// 左下->右上
            return ORI_L_B;
        } else if (p1.x < p0.x && p1.y > p0.y) {// 右上->左下
            return ORI_R_T;
        } else if (p1.x < p0.x && p1.y < p0.y) {// 右下->左上
            return ORI_R_B;
        } else if (p1.x == p0.x && p1.y > p0.y) {// 垂直向下
            return ORI_DOWN;
        } else if (p1.x == p0.x && p1.y < p0.y) {// 垂直向上
            return ORI_UP;
        } else if (p1.x > p0.x && p1.y == p0.y) {// 水平向右
            return ORI_RIGHT;
        } else if (p1.x < p0.x && p1.y == p0.y) {// 水平向左
            return ORI_LEFT;
        }
        return ORI_FALL;

    }

    /**
     * 计算并绘制每一小段
     *
     * @param p0
     * @param p1
     * @param curWith
     * @param perWitch
     */
    private void culAndDrawEachSegment(FloatPoint p0, FloatPoint p1, float curWith, float perWitch) {
        if (p0.x == p1.x && p0.y == p1.y) {
            return;
        }
        curOri = getOrientation(p0, p1);
        float tan = (float) (p0.y - p1.y) / (p0.x - p1.x);
        float sin = Math.abs((float) (tan / Math.sqrt(tan * tan + 1)));
        float cos = Math.abs(sin / tan);
        //s1,s2,s3,s4为p0,p1计算出来的一个梯形顶点
        if (curOri == ORI_L_T) {// 左上->右下
            s1.x = (int) (p0.x + curWith / 2 * sin);
            s1.y = (int) (p0.y - curWith / 2 * cos);

            s2.x = (int) (p0.x - curWith / 2 * sin);
            s2.y = (int) (p0.y + curWith / 2 * cos);

            s3.x = (int) (p1.x + (curWith + perWitch) / 2 * sin);
            s3.y = (int) (p1.y - (curWith + perWitch) / 2 * cos);

            s4.x = (int) (p1.x - (curWith + perWitch) / 2 * sin);
            s4.y = (int) (p1.y + (curWith + perWitch) / 2 * cos);
        } else if (curOri == ORI_L_B) {// 左下->右上
            s1.x = (int) (p0.x - curWith / 2 * sin);
            s1.y = (int) (p0.y - curWith / 2 * cos);

            s2.x = (int) (p0.x + curWith / 2 * sin);
            s2.y = (int) (p0.y + curWith / 2 * cos);

            s3.x = (int) (p1.x - (curWith + perWitch) / 2 * sin);
            s3.y = (int) (p1.y - (curWith + perWitch) / 2 * cos);

            s4.x = (int) (p1.x + (curWith + perWitch) / 2 * sin);
            s4.y = (int) (p1.y + (curWith + perWitch) / 2 * cos);
        } else if (curOri == ORI_R_T) {// 右上->左下
            s1.x = (int) (p0.x + curWith / 2 * sin);
            s1.y = (int) (p0.y + curWith / 2 * cos);

            s2.x = (int) (p0.x - curWith / 2 * sin);
            s2.y = (int) (p0.y - curWith / 2 * cos);

            s3.x = (int) (p1.x + (curWith + perWitch) / 2 * sin);
            s3.y = (int) (p1.y + (curWith + perWitch) / 2 * cos);

            s4.x = (int) (p1.x - (curWith + perWitch) / 2 * sin);
            s4.y = (int) (p1.y - (curWith + perWitch) / 2 * cos);
        } else if (curOri == ORI_R_B) {// 右下->左上
            s1.x = (int) (p0.x - curWith / 2 * sin);
            s1.y = (int) (p0.y + curWith / 2 * cos);

            s2.x = (int) (p0.x + curWith / 2 * sin);
            s2.y = (int) (p0.y - curWith / 2 * cos);

            s3.x = (int) (p1.x - (curWith + perWitch) / 2 * sin);
            s3.y = (int) (p1.y + (curWith + perWitch) / 2 * cos);

            s4.x = (int) (p1.x + (curWith + perWitch) / 2 * sin);
            s4.y = (int) (p1.y - (curWith + perWitch) / 2 * cos);
        } else if (curOri == ORI_DOWN) {// 垂直向下
            s1.x = (int) (p0.x + curWith / 2);
            s1.y = (int) p0.y;

            s2.x = (int) (p0.x - curWith / 2);
            s2.y = (int) p0.y;

            s3.x = (int) (p1.x + curWith / 2);
            s3.y = (int) p1.y;

            s4.x = (int) (p1.x - curWith / 2);
            s4.y = (int) p1.y;
        } else if (curOri == ORI_UP) {// 垂直向上
            s1.x = (int) (p0.x - curWith / 2);
            s1.y = (int) p0.y;

            s2.x = (int) (p0.x + curWith / 2);
            s2.y = (int) p0.y;

            s3.x = (int) (p1.x - curWith / 2);
            s3.y = (int) p1.y;

            s4.x = (int) (p1.x + curWith / 2);
            s4.y = (int) p1.y;
        } else if (curOri == ORI_RIGHT) {// 水平向右
            s1.x = (int) p0.x;
            s1.y = (int) (p0.y - curWith / 2);

            s2.x = (int) p0.x;
            s2.y = (int) (p0.y + curWith / 2);

            s3.x = (int) p1.x;
            s3.y = (int) (p1.y - curWith / 2);

            s4.x = (int) p1.x;
            s4.y = (int) (p1.y + curWith / 2);
        } else if (curOri == ORI_LEFT) {// 水平向左
            s1.x = (int) p0.x;
            s1.y = (int) (p0.y + curWith / 2);

            s2.x = (int) p0.x;
            s2.y = (int) (p0.y - curWith / 2);

            s3.x = (int) p1.x;
            s3.y = (int) (p1.y + curWith / 2);

            s4.x = (int) p1.x;
            s4.y = (int) (p1.y - curWith / 2);
        } else {
            s1.x = s2.x = s3.x = s4.x = (int) p0.x;
            s1.y = s2.y = s3.y = s4.y = (int) p0.y;
        }
        if (curWith == 0) {//如果是第一段
            quadPath(true, prePoint1, s1);
            quadPath(false, prePoint2, s2);
            prePoint1.set(s1.x, s1.y);
            prePoint2.set(s2.x, s2.y);
        } else {
            quadPath(true, prePoint1, new FloatPoint((s1.x + pS3.x) / 2,
                    (s1.y + pS3.y) / 2));
            quadPath(false, prePoint2, new FloatPoint((s2.x + pS4.x) / 2,
                    (s2.y + pS4.y) / 2));
            prePoint1.set((s1.x + pS3.x) / 2, (s1.y + pS3.y) / 2);
            prePoint2.set((s2.x + pS4.x) / 2, (s2.y + pS4.y) / 2);
        }

        pS1.set(s1.x, s1.y);
        pS2.set(s2.x, s2.y);
        pS3.set(s3.x, s3.y);
        pS4.set(s4.x, s4.y);

        //画一定宽度的线填充空心曲线
        float length = (float) Math.sqrt((p0.x - p1.x) * (p0.x - p1.x) + (p0.y - p1.y) * (p0.y - p1.y));
        if (length >= 6) {
            int cc = (int) (length / 3);
            float dx = p1.x - p0.x;
            float dy = p1.y - p0.y;
            for (int a = 0; a < cc; a++) {
                if (p00 == null || (p00.x == 0 && p00.y == 0)) {
                    p00 = new FloatPoint();
                    p00.set(p0.x + dx / cc * a, p0.y + dy / cc * a);
                    continue;
                }
                drawPerLine(p00.x, p00.y, (p0.x + dx / cc * a), (p0.y + dy / cc * a), (p0.x + dx / cc * (a + 1)), (p0.y + dy / cc * (a + 1)), (curWith + perWitch * (a + 1) / cc / 2));
                p00.set(p0.x + dx / cc * a, p0.y + dy / cc * a);
            }
        } else {
            drawPerLine(p00, p0, p1, (curWith + perWitch / 2));
            p00 = p0;
        }
        drawCanvas();
        drawBitmapCanvas();
        //postInvalidate();
    }


    private void quadPath(boolean isPath1, FloatPoint p1, FloatPoint p2) {
        float dx = Math.abs(p2.x - p1.x);
        float dy = Math.abs(p2.y - p1.y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            if (isPath1) {
                mPath1.quadTo(p1.x, p1.y, (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            } else {
                mPath2.quadTo(p1.x, p1.y, (p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            }

        }
    }

    private void drawPerLine(FloatPoint p0, FloatPoint p1, FloatPoint p2, float cw) {
        if (mBitmapCanvas != null) {
            mLinePaint.setStrokeWidth(cw);
            //mBitmapCanvas.drawLine(p1.x, p1.y, p2.x, p2.y, mLinePaint);
            if (!(p0 == null || (p0.x == 0 && p0.y == 0))) {
                mBitmapCanvas.drawLine((p0.x + p1.x) / 2, (p0.y + p1.y) / 2,
                        (p1.x + p2.x) / 2, (p1.y + p2.y) / 2, mLinePaint);
            }
        }
    }

    private void drawPerLine(float x0, float y0, float x1, float y1, float x2, float y2, float cw) {
        if (mBitmapCanvas != null) {
            mLinePaint.setStrokeWidth(cw);
            //mBitmapCanvas.drawLine(p1.x, p1.y, p2.x, p2.y, mLinePaint);
            if (x0 != 0 && y0 != 0) {
                mBitmapCanvas.drawLine((x0 + x1) / 2, (y0 + y1) / 2,
                        (x1 + x2) / 2, (y1 + y2) / 2, mLinePaint);
            }
        }
    }

    private void drawCanvas() {
        if (mBitmapCanvas != null) {
            mBitmapCanvas.drawPath(mPath1, mPaint);
            mBitmapCanvas.drawPath(mPath2, mPaint);
        }
    }

    private void clearCanvas() {
        if (mBitmapCanvas != null) {
            Paint p = new Paint();
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mBitmapCanvas.drawPaint(p);
            mBitmapCanvas.drawColor(Color.BLACK);
            drawBitmapCanvas();
            //postInvalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (isDrawing)
            return true;
        int x = (int) event.getX();
        int y = (int) event.getY();
        FloatPoint FloatPoint = new FloatPoint(x, y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pathPoint.clear();
                pathPoint.add(FloatPoint);
                isRecording = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    pathPoint.add(FloatPoint);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isRecording) {
                    pathPoint.add(FloatPoint);
                    isRecording = false;
                    mDrawThread = new Thread(DrawRunnable);
                    mDrawThread.start();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    Runnable DrawRunnable = new Runnable() {
        @Override
        public void run() {

            // TODO Auto-generated method stub
            isDrawing = true;
            int size = pathPoint.size();
            if (size <= 10) {
                isDrawing = false;
                return;
            }
            int frontArea = (int) (size * FRONT_GRADE_RATE);
            int backArea = (int) (size * (1 - BACK_GRADE_RATE));
            float perGrad = (float) MAX_WITCH_PIXEL / frontArea;
            long perSleepTime = 200 / size;

            for (int i = 0; i < pathPoint.size(); i++) {
                if (i == 0) {
                    touch_start();
                } else if (i == 1) {
                    culAndDrawEachSegment(pathPoint.get(i - 1), pathPoint.get(i), 0,
                            perGrad);
                } else if (i > 0 && i < frontArea) {
                    culAndDrawEachSegment(pathPoint.get(i - 1), pathPoint.get(i), i
                            * perGrad, perGrad);
                } else if (i >= frontArea && i < backArea) {
                    culAndDrawEachSegment(pathPoint.get(i - 1), pathPoint.get(i),
                            MAX_WITCH_PIXEL, 0);
                } else if (i >= backArea && i < (pathPoint.size() - 1)) {
                    culAndDrawEachSegment(pathPoint.get(i - 1), pathPoint.get(i),
                            MAX_WITCH_PIXEL - (i - backArea) * perGrad,
                            -perGrad);
                } else if (i == size - 1) {
                    touch_up();
                }

                try {
                    Thread.sleep(perSleepTime);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            //postInvalidate();
        }
    };

    private void touch_start() {
        Log.i("--lijun--", "touch_start");
        clearCanvas();
        preOri = -1;
        p00 = null;
        pS1.set(pathPoint.get(0).x, pathPoint.get(0).y);
        pS2.set(pathPoint.get(0).x, pathPoint.get(0).y);
        pS3.set(pathPoint.get(0).x, pathPoint.get(0).y);
        pS4.set(pathPoint.get(0).x, pathPoint.get(0).y);
        prePoint1.set(pathPoint.get(0).x, pathPoint.get(0).y);
        prePoint2.set(pathPoint.get(0).x, pathPoint.get(0).y);
        mPath1.reset();
        mPath2.reset();
        mPath1.moveTo(pathPoint.get(0).x, pathPoint.get(0).y);
        mPath2.moveTo(pathPoint.get(0).x, pathPoint.get(0).y);
    }

    private void touch_up() {
        isDrawing = false;
        mPath1.lineTo(pathPoint.get(pathPoint.size() - 1).x,
                pathPoint.get(pathPoint.size() - 1).y);
        mPath2.lineTo(pathPoint.get(pathPoint.size() - 1).x,
                pathPoint.get(pathPoint.size() - 1).y);
        drawCanvas();
        drawBitmapCanvas();
    }

    private void drawBitmapCanvas() {
        try {
            mCanvas = sfh.lockCanvas();
            if (mCanvas != null) {
                mCanvas.drawBitmap(mBitmap, 0, 0, null);
                invalidate();
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (mCanvas != null)
                sfh.unlockCanvasAndPost(mCanvas);
        }
    }

    /**
     * 用于浮点计算的Point，解决多次计算，精度下降问题
     *
     * @author Administrator
     */
    private class FloatPoint {
        public float x;
        public float y;

        public FloatPoint() {
            super();
        }

        public FloatPoint(float X, float Y) {
            super();
            x = X;
            y = Y;
        }

        public void set(float X, float Y) {
            x = X;
            y = Y;
        }
    }


    /**
     * 获取灭屏时的触摸轨迹
     */
    //moka add to read file
    public ArrayList<FloatPoint> readFromFile(String fileName) {

        String ss, toString;

        ArrayList<FloatPoint> result = new ArrayList<FloatPoint>();

        try {

            FileInputStream file = new FileInputStream(fileName);

            int length = file.available();

            byte[] msg = new byte[length];
            file.read(msg);

            ss = EncodingUtils.getString(msg, ENCODING);

            String[] sss = ss.split(" ");
            Log.d("Moka", "sss size " + sss.length);
            float px = 0;
            float py = 0;
            for (int m = 0; m < sss.length; m++) {

                if (m % 2 == 0) {
                    py = Float.parseFloat(sss[m]);
                    result.add(new FloatPoint(py, px));
                } else {
                    px = Float.parseFloat(sss[m]);
                }
            }

            file.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        Log.d("Moka", "result size " + result.size());
        return result;
    }


}