package com.lijun.androidstudy.floatmeltitask;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.util.Utilities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;

/**
 * filename:MainWindowView.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-7
 * author: laiyang
 * <p>
 * show main window
 * <p>
 * Modification History
 * -----------------------------------
 * <p>
 * -----------------------------------
 */
public class MainWindowView extends LinearLayout implements View.OnClickListener,
        View.OnTouchListener, View.OnLongClickListener {
    // content layout
    private RelativeLayout mrlContent;
    // title bar layout
    private RelativeLayout mrlTitle;
    // bottom bar layout
    private RelativeLayout mrlButtom;
    // WindowManager layout params
    private WindowManager.LayoutParams mParams;
    // inflater layout xml
    private LayoutInflater mInflater;

    private LinearLayout mllVideo;

    private LinearLayout mllMusic;

    private LinearLayout mllNote;

    private LinearLayout mllMsg;
    private LinearLayout mMainWindowView;
    private ImageView mivMinimize;
    private ImageView mivFloatSetting;
    private static final String TAG = "MainWindowView";

    private WindowManager windowManager;
    // status bar height
    private static int statusBarHeight;

    private float touchX;

    private float touchY;

    private float xInScreen;

    private float yInScreen;

    public MainWindowView(Context context) {
        super(context);
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.main_window, this);
        mMainWindowView = (LinearLayout) findViewById(R.id.mainWindowView);
        mrlContent = (RelativeLayout) findViewById(R.id.rl_content_bar);
        mrlTitle = (RelativeLayout) findViewById(R.id.rl_title_bar);
        // inflater content layout
        mInflater.inflate(R.layout.task_select_content, mrlContent);

        // inflater title layout
        mInflater.inflate(R.layout.task_select_title_bar, mrlTitle);
        mivFloatSetting = (ImageView) findViewById(R.id.float_setting);
        mllVideo = (LinearLayout) findViewById(R.id.ll_video);
        mllMusic = (LinearLayout) findViewById(R.id.ll_music);
        mllNote = (LinearLayout) findViewById(R.id.ll_note);
        mllMsg = (LinearLayout) findViewById(R.id.ll_msg);
        // set buttons click listener
        mllVideo.setOnClickListener(this);
        mllMusic.setOnClickListener(this);
        mllNote.setOnClickListener(this);
        mllMsg.setOnClickListener(this);
        mivFloatSetting.setOnClickListener(this);
        mivMinimize = (ImageView) findViewById(R.id.iv_minimize);
        mivMinimize.setOnClickListener(this);
        mrlTitle.setFocusable(true);
        mrlTitle.setOnLongClickListener(this);
        mrlTitle.setOnTouchListener(this);
        // play animation
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_in);
        mMainWindowView.setAnimation(anim);
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mllMsg)) {//send broadcast to show sms float window
            MultiTaskManager.removeMainWindow(getContext());
            MultiTaskManager.createFLoatButton(getContext());
            Intent i = new Intent("com.malata.floatmultitask.action.sms.show");
            i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            getContext().sendBroadcast(i);
        } else if (v.equals(mllMusic)) {//to music float window
            MultiTaskManager.removeMainWindow(getContext());
            MultiTaskManager.createMusicWindow(getContext());
        } else if (v.equals(mllNote)) {//to note float window
            MultiTaskManager.removeMainWindow(getContext());
            MultiTaskManager.createNoteWindow(getContext());
        } else if (v.equals(mllVideo)) {//to video float window
            MultiTaskManager.removeMainWindow(getContext());
            MultiTaskManager.createVideoWindow(getContext());
        } else if (v.equals(mivMinimize)) {//to float button
            // play animation
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // show float button and remove this
                    mMainWindowView.setVisibility(View.GONE);
                    MultiTaskManager.removeMainWindow(getContext());
                    MultiTaskManager.createFLoatButton(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mMainWindowView.startAnimation(anim);
        } else if (v.equals(mivFloatSetting)) {// start to float settings and to show float button
            // play animation
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mMainWindowView.setVisibility(View.GONE);
                    MultiTaskManager.removeMainWindow(getContext());
                    MultiTaskManager.createFLoatButton(getContext());
                    Intent intent = new Intent();
                    intent.setClassName("com.lijun.androidstudy", "com.lijun.androidstudy.floatmeltitask.FloatMultiTaskActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Utilities.startActivitySafety(getContext(), intent);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mMainWindowView.startAnimation(anim);

        }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            Log.d(TAG, "outside down1");
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // show float button and remove this
                    mMainWindowView.setVisibility(View.GONE);
                    MultiTaskManager.removeMainWindow(getContext());
                    MultiTaskManager.createFLoatButton(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mMainWindowView.startAnimation(anim);
            return false;
        }
        return false;
    }

    /**
     * onTouchListener
     * move view in window
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (isLongClick) {
            touchX = event.getX();
            touchY = event.getY();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "action down");
                touchX = event.getX();
                touchY = event.getY();

                break;
            case MotionEvent.ACTION_MOVE:

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                Log.d(TAG, "preX:" + mParams.x);
                Log.d(TAG, "preY:" + mParams.y);
                Log.d(TAG, "nowX:" + xInScreen);
                Log.d(TAG, "nowY:" + yInScreen);
                Log.d(TAG, "touchX:" + touchX);
                Log.d(TAG, "touchY:" + touchY);

                mParams.x = (int) (xInScreen - touchX);
                mParams.y = (int) (yInScreen - touchY);
                windowManager.updateViewLayout(this, mParams);

                Log.i(TAG, "action_move");

                break;

            case MotionEvent.ACTION_UP:
                Log.d(TAG, "action up");
                mrlTitle.setOnLongClickListener(this);
                isLongClick = true;
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * if true,set long click listener available
     */
    private boolean isLongClick = true;

    @Override
    public boolean onLongClick(View v) {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0, 50}, -1);
        isLongClick = false;
        mrlTitle.setOnLongClickListener(null);
        return false;
    }
}
