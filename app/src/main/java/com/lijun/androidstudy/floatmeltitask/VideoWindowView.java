package com.lijun.androidstudy.floatmeltitask;

import com.lijun.androidstudy.R;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * filename:VideoWindowView.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-8
 * author: laiyang
 * <p>
 * <p>
 * <p>
 * Modification History
 * -----------------------------------
 * <p>
 * -----------------------------------
 */
public class VideoWindowView extends LinearLayout implements View.OnClickListener,
        View.OnLongClickListener, View.OnTouchListener {

    private static VideoWindowView mVideoWindow;
    public static final int MSG_PALY_SUCCESS = 1002;

    private WindowManager.LayoutParams mParams;

    private ImageButton mibPlay;

    private ImageButton mibPre;

    private ImageButton mibNext;

    private ImageButton mibMovieView;

    private ImageView mivBack;

    private ImageView mivMinimize;

    private boolean isPlaying;

    private SurfaceView mSurfaceView;

    private ProgressBar mProgressBar;

    private RelativeLayout mrlTitle;

    private RelativeLayout mrlBottomBar;

    private RelativeLayout mrlContent;

    private RelativeLayout mVideoWholeView;

    private static ArrayList<String> videos;

    private WindowManager windowManager;

    private static int statusBarHeight;

    private static final String TAG = "VideoWindowView";

    private float touchX;

    private float touchY;

    private float xInScreen;

    private float yInScreen;

    private TextView mtvNoVideo;

    public VideoWindowView(Context context) {
        super(context);
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.video_window, this);
        mVideoWholeView = (RelativeLayout) findViewById(R.id.videoWholeView);
        mibPlay = (ImageButton) findViewById(R.id.videoplayer_floatingwindowvideo_content_controller_playvideo);
        mibPre = (ImageButton) findViewById(R.id.videoplayer_floatingwindowvideo_content_controller_lastvideo);
        mibNext = (ImageButton) findViewById(R.id.videoplayer_floatingwindowvideo_content_controller_nextvideo);
        mibMovieView = (ImageButton) findViewById(R.id.videoplayer_floatingwindowvideo_content_controller_tomovieview);
        mProgressBar = (ProgressBar) findViewById(R.id.videoplayer_floatingwindowvideo_content_controller_progressbar);
        mrlTitle = (RelativeLayout) findViewById(R.id.rl_title_bar);
        mrlBottomBar = (RelativeLayout) findViewById(R.id.rl_bottom_bar);
        mrlContent = (RelativeLayout) findViewById(R.id.rl_content_bar);
        mtvNoVideo = (TextView) findViewById(R.id.tv_no_video);
        mProgressBar.setMax(100);
        mivBack = (ImageView) findViewById(R.id.iv_back);
        mivMinimize = (ImageView) findViewById(R.id.iv_minimize);
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_video);
        mivBack.setOnClickListener(this);
        mivMinimize.setOnClickListener(this);
        mibPlay.setOnClickListener(this);
        mibNext.setOnClickListener(this);
        mibPre.setOnClickListener(this);
        mibMovieView.setOnClickListener(this);
        mrlTitle.setOnLongClickListener(this);
        mrlTitle.setOnTouchListener(this);
        mVideoWindow = this;
        // load videos in media store
        loadVideo();
        if (0 == videos.size()) {
            mibNext.setEnabled(false);
            mibPlay.setEnabled(false);
            mibPre.setEnabled(false);
            mtvNoVideo.setVisibility(View.VISIBLE);
        } else {
            mtvNoVideo.setVisibility(View.GONE);
        }
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_button_view_in);
        mVideoWholeView.startAnimation(anim);
    }

    public static ArrayList<String> getVideos() {
        return videos;
    }

    /**
     * message to update progress bar
     */
    public final static int MSG_UPDATE_PROGRESS_BAR = 1001;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS_BAR:
                    mProgressBar.setProgress((Integer) msg.obj);
                    break;
                case MSG_PALY_SUCCESS:
                    isPlaying = true;
                    mibPlay.setImageResource(R.drawable.app_pause_button_bg);
                    enableJumpButton();
                    break;
                default:
                    break;
            }
        }
    };

    private void enableJumpButton() {
        mibNext.setEnabled(true);
        mibPre.setEnabled(true);
    }

    private void disableJumpButton() {
        mibNext.setEnabled(false);
        mibPre.setEnabled(false);
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    @Override
    public void onClick(View v) {

        if (v.equals(mibPlay)) {//
            Intent intent = new Intent();
            if (!isPlaying) {
                intent.putExtra("MSG", BackTaskService.PLAY_VIDEO);
                isPlaying = true;
                mibPlay.setImageResource(R.drawable.app_pause_button_bg);
            } else {
                intent.putExtra("MSG", BackTaskService.VIDEO_PAUSE);
                isPlaying = false;
                mibPlay.setImageResource(R.drawable.app_play_button_bg);
            }
            intent.setClass(getContext(), BackTaskService.class);
            getContext().startService(intent);
            if (View.GONE == mSurfaceView.getVisibility()) {
                mSurfaceView.setVisibility(VISIBLE);
            }

        } else if (v.equals(mivBack)) {//back to main window
            if (!BackTaskService.isPlayMusic()) {//stop to play video
                Intent intent = new Intent(getContext(), BackTaskService.class);
                getContext().stopService(intent);
            }
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mVideoWholeView.setVisibility(View.GONE);
                    MultiTaskManager.removeVideoWindow(getContext());
                    MultiTaskManager.createMainWindow(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mVideoWholeView.startAnimation(anim);
        } else if (v.equals(mivMinimize)) {
            if (!BackTaskService.isPlayMusic()) {
                Intent intent = new Intent(getContext(), BackTaskService.class);
                getContext().stopService(intent);
            }
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {

                }

                public void onAnimationEnd(Animation animation) {
                    mVideoWholeView.setVisibility(View.GONE);
                    MultiTaskManager.removeVideoWindow(getContext());
                    MultiTaskManager.createFLoatButton(getContext());
                }

                public void onAnimationRepeat(Animation animation) {

                }
            });
            mVideoWholeView.startAnimation(anim);
        } else if (v.equals(mibNext)) {
            if (View.GONE == mSurfaceView.getVisibility()) {
                mSurfaceView.setVisibility(VISIBLE);
            }
            Intent intent = new Intent();
            intent.putExtra("MSG", BackTaskService.PLAY_VIDEO_NEXT);
            intent.setClass(getContext(), BackTaskService.class);
            getContext().startService(intent);
            disableJumpButton();
        } else if (v.equals(mibPre)) {
            if (View.GONE == mSurfaceView.getVisibility()) {
                mSurfaceView.setVisibility(VISIBLE);
            }
            Intent intent = new Intent();
            intent.putExtra("MSG", BackTaskService.PLAY_VIDEO_PRE);
            intent.setClass(getContext(), BackTaskService.class);
            getContext().startService(intent);
            disableJumpButton();
        } else if (v.equals(mibMovieView)) {
            mrlBottomBar.setVisibility(View.GONE);
            mrlTitle.setVisibility(View.GONE);
            mrlContent.setOnClickListener(this);
        } else if (v.equals(mrlContent)) {
            mrlBottomBar.setVisibility(View.VISIBLE);
            mrlTitle.setVisibility(View.VISIBLE);
            mrlContent.setOnClickListener(null);
        }
    }

    /**
     * send message
     *
     * @param what message type
     */
    public static void sendMessage(int what) {
        mVideoWindow.mHandler.sendEmptyMessage(what);
    }

    /**
     * send message
     *
     * @param what message type
     * @param obj  message content
     */
    public static void sendMessage(int what, Object obj) {
        Message msg = mVideoWindow.mHandler.obtainMessage(what, obj);
        mVideoWindow.mHandler.sendMessage(msg);
    }

    /**
     * return surfaceholder of surfaceview
     */
    public static SurfaceHolder getSurfaceHodler() {
        SurfaceHolder holder = mVideoWindow.mSurfaceView.getHolder();
        return holder;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isLongClick) {
            touchX = event.getX();
            touchY = event.getY();
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "action down");
                touchX = event.getX();
                touchY = event.getY();

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "preX:" + mParams.x);
                Log.d(TAG, "preY:" + mParams.y);
                Log.d(TAG, "nowX:" + event.getRawX());
                Log.d(TAG, "nowY:" + event.getRawY());
                Log.d(TAG, "downX:" + touchX);
                Log.d(TAG, "downY:" + touchY);

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();

                mParams.x = (int) (xInScreen - touchX);
                mParams.y = (int) (yInScreen - touchY);
                windowManager.updateViewLayout(this, mParams);
                Log.i(TAG, "action_move");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "action up");
                mrlTitle.setOnLongClickListener(this);
                isLongClick = true;
//                mrlTitle.setOnTouchListener(null);
                break;
        }
        return false;
    }

    /**
     * if true,long click listener is available
     */
    private boolean isLongClick = true;

    @Override
    public boolean onLongClick(View v) {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0, 50}, -1);
        mrlTitle.setOnLongClickListener(null);
        isLongClick = false;
        return false;
    }

    /**
     * get status bar height
     *
     * @return
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    /**
     * load videos in media store
     */
    public void loadVideo() {
        videos = new ArrayList<String>();
        ContentResolver resolver = getContext().getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.DATA}, null, null, null);
        while (cursor.moveToNext()) {
            videos.add(cursor.getString(0));
        }
        cursor.close();
    }
}
