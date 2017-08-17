package com.lijun.androidstudy.drawAsPen;

import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Runnable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import com.lijun.androidstudy.R;

import android.widget.FrameLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.WindowManager;

import android.widget.MediaController;

public class TPGensturePreChar extends Activity {

    List<Bitmap> mBitmaps;
    List<Drawable> mDrawables;

    List<Point> mPathPoint = new ArrayList<Point>();
    DrawGustureAnimal mDrawGustureAnimal;

    MediaController mMediaController;

    public static final int FRAME_DELAY = 30;
    public static final int ANIMAL_DELAY = 2500;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_UNKNOWN = 2;

    static String filename = "/sys/devices/platform/mtk-tpd/gesture";
    boolean mState = false;

    // 手势方向
    private static final String GENTURES_UP = "android.intent.action.GENTURES_UP";
    private static final String GENTURES_DOWN = "android.intent.action.GENTURES_DOWN";
    private static final String GENTURES_LEFT = "android.intent.action.GENTURES_LEFT";
    private static final String GENTURES_RIGHT = "android.intent.action.GENTURES_RIGHT";

    private int master_switch_state;
    private int c_switch_state;
    private int e_switch_state;
    private int m_switch_state;
    private int o_switch_state;
    private int up_switch_state;
    private int down_switch_state;
    private int left_right_switch_state;
    private int right_left_switch_state;

    private Runnable mRunnable = null;
    private Handler mHander = new Handler();

    PowerManager mPowerManager;

    public void onCreate(Bundle savedInstanceState) {
        Log.i("--lijun--", "TPGensturePreChar onCreate");
        Object service = getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class
                    .forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, 0x00000001);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);

        mDrawGustureAnimal = new DrawGustureAnimal(this);
        setContentView(mDrawGustureAnimal);
        registerBroadcastReceiver();// 注册广播
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(mDrawGustureAnimal != null){
            mDrawGustureAnimal.removeAllViews();
        }
        Log.i("--lijun--", "TPGensturePreChar onResume");
        Intent i = getIntent();
        String charactor = i.getStringExtra("gensture_charactor");

        master_switch_state = 1;
        c_switch_state = 1;
        e_switch_state = 1;
        m_switch_state = 1;
        o_switch_state = 1;
        up_switch_state = 1;
        down_switch_state = 1;
        left_right_switch_state = 1;
        right_left_switch_state = 1;

        if (null != charactor && (master_switch_state == 1)) {
            Bitmap bmp;
            if (("e".equals(charactor)) && (e_switch_state == 1)) {
                bmp = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smart_wake_e);
                mRunnable = new Runnable() {
                    public void run() {
                        Intent c = new Intent();
                        c.setClassName("com.android.browser",
                                "com.android.browser.BrowserActivity");
                        c.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(c);
                        overridePendingTransition(-1, -1);
                        finish();
                    }
                };

            } else if (("m".equals(charactor)) && (m_switch_state == 1)) {
                bmp = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smart_wake_w);
                mRunnable = new Runnable() {
                    public void run() {
                        Intent iIntent = new Intent(
                                "zx_tpgensture_m_music_play");
                        sendBroadcast(iIntent);
                        overridePendingTransition(-1, -1);
                        finish();
                    }
                };
            } else if (("c".equals(charactor)) && (c_switch_state == 1)) {
                bmp = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smart_wake_c);
                mRunnable = new Runnable() {
                    public void run() {

                        Intent c = new Intent();
                        c.setClassName("com.android.dialer",
                                "com.android.dialer.DialtactsActivity");
                        c.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(c);
                        overridePendingTransition(-1, -1);
                        finish();
                    }
                };
            }else if (("up".equals(charactor)) && (up_switch_state == 1)) {
                bmp = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smart_wake_up);
                mRunnable = new Runnable() {
                    public void run() {
                        Intent c = new Intent();
                        c.setClassName("com.android.dialer",
                                "com.android.dialer.DialtactsActivity");
                        c.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(c);
                        overridePendingTransition(-1, -1);
                        finish();
                    }
                };
            }else if (("down".equals(charactor)) && (down_switch_state == 1)) {
                bmp = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smart_wake_up);
                mRunnable = new Runnable() {
                    public void run() {
                        Intent mCamera = new Intent();
                        mCamera.setClassName("com.android.gallery3d",
                                "com.android.camera.CameraLauncher");
                        mCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(mCamera);
                        overridePendingTransition(-1, -1);
                        finish();
                    }
                };
            }else if (("left_right".equals(charactor)) && (down_switch_state == 1)) {
                bmp = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smart_wake_up);
                mRunnable = null;
            }else if (("right_left".equals(charactor)) && (down_switch_state == 1)) {
                bmp = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smart_wake_up);
                mRunnable = null;
            }else{
                finish();
                return;
            }

            mBitmaps = splitBitmap(bmp, 4, 5);
            mDrawables = getDrawable(mBitmaps);
            mDrawGustureAnimal.setmDrawables(mDrawables);
            mPathPoint = readFromFile(filename);
            startDrawAnimal();
        }else{
            finish();
        }
    }

    public void registerBroadcastReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(GENTURES_UP);
        filter.addAction(GENTURES_DOWN);
        filter.addAction(GENTURES_LEFT);
        filter.addAction(GENTURES_RIGHT);
        registerReceiver(mBroadcastReceiver, filter);// 注册广播
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            String action = intent.getAction();
            if (action.equals(GENTURES_UP)) {

                Intent i = new Intent("com.zx.tp.gensture.unlockscreen");
                sendBroadcast(i);

                Toast.makeText(getApplicationContext(), "ssss",
                        Toast.LENGTH_LONG).show();

            } else if (action.equals(GENTURES_DOWN)) {

                Toast.makeText(getApplicationContext(), "ssss",
                        Toast.LENGTH_LONG).show();

            } else if (action.equals(GENTURES_LEFT)) {

                Toast.makeText(getApplicationContext(), "ssss",
                        Toast.LENGTH_LONG).show();

            } else if (action.equals(GENTURES_RIGHT)) {

                Toast.makeText(getApplicationContext(), "ssss",
                        Toast.LENGTH_LONG).show();

            }

        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * 获取灭屏时的触摸轨迹
     */
    public ArrayList<Point> readFromFile(String fileName) {

        String ss;

        ArrayList<Point> result = new ArrayList<Point>();
        FileInputStream file = null;
        try {

            file = new FileInputStream(fileName);

            int length = file.available();

            byte[] msg = new byte[length];
            file.read(msg);

            ss = EncodingUtils.getString(msg, "UTF-8");

            String[] sss = ss.split(" ");

            int px = 0;
            int py = 0;
            for (int m = 0; m < sss.length; m++) {
                if (m % 2 == 1) {
                    py = Integer.parseInt(sss[m]);
                    result.add(new Point(px, py));
                } else {
                    px = Integer.parseInt(sss[m]);
                }
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();

        }finally{
            try {
                file.close();
                file = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Log.d("Moka", "result size " + result.size());
        return result;
    }

    private void startDrawAnimal() {
        Rect rect = new Rect();
        int size = mPathPoint.size();
        if (size <= 0)
            finish();
        Point point = mPathPoint.get(0);
        rect.set(point.x, point.y, point.x, point.y);
        for (int i = 0; i < mPathPoint.size(); i++) {
            Point pt = mPathPoint.get(i);
            rect.union(pt.x, pt.y);
        }
        mDrawGustureAnimal.setPosition(rect.left, rect.top, rect.right,
                rect.bottom);
        mDrawGustureAnimal.startAnimal(this);
    }

    public void endDrawAninal(){
        if(mRunnable != null){
            mHander.post(mRunnable);
        }else{
            screenOff();
            finish();
        }
    }

    /*
     * 切割图片，按xPiece列，yPiece行均分切割
     */
    private List<Bitmap> splitBitmap(Bitmap bitmap, int xPiece, int yPiece) {
        List<Bitmap> result = new ArrayList<Bitmap>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pieceWidth = width / xPiece;
        int pieceHeight = height / yPiece;
        for (int i = 0; i < yPiece; i++) {
            for (int j = 0; j < xPiece; j++) {
                Bitmap piece = Bitmap.createBitmap(bitmap, j * pieceWidth, i
                        * pieceHeight, pieceWidth, pieceHeight);
                result.add(piece);
            }
        }
        return result;
    }

    private List<Drawable> getDrawable(List<Bitmap> bms) {
        List<Drawable> result = new ArrayList<Drawable>();
        for (int i = 0; i < bms.size(); i++) {
            BitmapDrawable bd = new BitmapDrawable(getResources(), bms.get(i));
            result.add(bd);
        }
        //添加一张空图片刷新界面
        Drawable d = getResources().getDrawable(R.drawable.smart_wake_null);
        result.add(d);
        return result;
    }

    private void screenOff(){
        if(mPowerManager == null){
            mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        }
        if(mPowerManager.isScreenOn()){
            sendBroadcast(new Intent("com.zx.tp.gensture.lockscreen"));
//            mPowerManager.goToSleep(SystemClock.uptimeMillis());
        }
    }
}