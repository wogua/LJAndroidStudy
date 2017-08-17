package com.lijun.androidstudy.floatmeltitask;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * file name:MultiTaskManager.java
 * Copyright MALATA ,ALL rights reserved.
 * 2015-7-29
 * author:laiyang
 * <p>
 * The class is to create or remove FloatWindow
 * <p>
 * Modification history
 * -------------------------------------
 * <p>
 * -------------------------------------
 */
public class MultiTaskManager {
    /**
     * WindowManger,use to add view to Window
     */
    private static WindowManager mWindowManger;
    // All of float view object
    private static FloatButtonView mFloatButton;
    private static MainWindowView mMainWindow;
    private static MusicWindowView mMusicWindow;
    private static VideoWindowView mVideoWindow;
    private static NoteWindowView mNoteWindow;
    // float view's layout params
    private static WindowManager.LayoutParams mFloatButtonParams;
    private static WindowManager.LayoutParams mMainWindowParams;
    private static WindowManager.LayoutParams mMusicWindowParams;
    private static WindowManager.LayoutParams mVideoWindowParams;
    private static WindowManager.LayoutParams mNoteWindowParams;

    private static DisplayMetrics dm = new DisplayMetrics();

    /**
     * when FloatButton is null,init it and add it to window
     *
     * @param context application' context
     */
    public static void createFLoatButton(Context context) {
        // get WindowManger's instance
        WindowManager windowManager = getWindowManager(context);
        // get screen's width and height

        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        if (null == mFloatButton) {
            mFloatButton = new FloatButtonView(context);
            mFloatButtonParams = new WindowManager.LayoutParams();
            // set FloatButton init position align left and centre horizontal
            mFloatButtonParams.x = 0;
            mFloatButtonParams.y = screenHeight / 2;
            // set this view is under notification and below all application
            mFloatButtonParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            mFloatButtonParams.format = PixelFormat.RGBA_8888;
            mFloatButtonParams.gravity = Gravity.LEFT | Gravity.TOP;
            // set view width and height
            mFloatButtonParams.width = mFloatButton.viewWidth;
            mFloatButtonParams.height = mFloatButton.viewHeight;
            // set this view not touch modal and not focusable
            mFloatButtonParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        mFloatButton.setParams(mFloatButtonParams);
        // add view to window
        windowManager.addView(mFloatButton, mFloatButtonParams);
    }

    /**
     * when MainFloatWindow is null,init it and add it to window
     *
     * @param context application' context
     */
    public static void createMainWindow(Context context) {
        // get WindowManger's instance
        WindowManager windowManager = getWindowManager(context);
        // get screen's width and height
        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        if (mMainWindow == null) {
            mMainWindow = new MainWindowView(context);
            mMainWindowParams = new WindowManager.LayoutParams();
            // set view width and height
            setLayoutSize(context, mMainWindowParams);
            // set view center in screen
            mMainWindowParams.x = (screenWidth - mMainWindowParams.width) / 2;
            mMainWindowParams.y = (screenHeight - mMainWindowParams.height) / 2;
            // set this view is under notification and below all application
            mMainWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mMainWindowParams.format = PixelFormat.RGBA_8888;
            mMainWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            // set this view not touch modal and not focusable
            mMainWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }
        mMainWindow.setParams(mMainWindowParams);
        // add view to window
        windowManager.addView(mMainWindow, mMainWindowParams);
    }

    /**
     * when MusicWindow is null,init it and add it to window
     *
     * @param context application' context
     */
    public static void createMusicWindow(Context context) {
        // get WindowManger's instance
        WindowManager windowManager = getWindowManager(context);
        // get screen's width and height
        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        if (null == mMusicWindow) {
            mMusicWindow = new MusicWindowView(context);
            mMusicWindowParams = new WindowManager.LayoutParams();
            // set view width and height
            setLayoutSize(context, mMusicWindowParams);
            // set view center in screen
            mMusicWindowParams.x = (screenWidth - mMusicWindowParams.width) / 2;
            mMusicWindowParams.y = (screenHeight - mMusicWindowParams.height) / 2;
            // set this view is under notification and below all application
            mMusicWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mMusicWindowParams.format = PixelFormat.RGBA_8888;
            mMusicWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            // set this view not touch modal and not focusable
            mMusicWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            windowManager.removeView(mMusicWindow);
        }
        mMusicWindow.setParams(mMusicWindowParams);
        windowManager.addView(mMusicWindow, mMusicWindowParams);
    }

    /**
     * when VideoWindow is null,init it and add it to window
     *
     * @param context application' context
     */
    public static void createVideoWindow(Context context) {
        // get WindowManger's instance
        WindowManager windowManager = getWindowManager(context);
        // get screen's width and height
        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        if (null == mVideoWindow) {
            mVideoWindow = new VideoWindowView(context);
            mVideoWindowParams = new WindowManager.LayoutParams();
            // set view width and height
            setLayoutSize(context, mVideoWindowParams);
            // set view center in screen
            mVideoWindowParams.x = (screenWidth - mVideoWindowParams.width) / 2;
            mVideoWindowParams.y = (screenHeight - mVideoWindowParams.height) / 2;
            // set this view is under notification and below all application
            mVideoWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mVideoWindowParams.format = PixelFormat.RGBA_8888;
            mVideoWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            // set this view not touch modal and not focusable
            mVideoWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            windowManager.removeView(mVideoWindow);
        }
        mVideoWindow.setParams(mVideoWindowParams);
        windowManager.addView(mVideoWindow, mVideoWindowParams);
    }

    /**
     * when NoteWindow is null,init it and add it to window
     *
     * @param context application' context
     */
    public static void createNoteWindow(Context context) {
        // get WindowManger's instance
        WindowManager windowManager = getWindowManager(context);
        // get screen's width and height
        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        if (null == mNoteWindow) {
            mNoteWindow = new NoteWindowView(context);
            mNoteWindowParams = new WindowManager.LayoutParams();
            // set view width and height
            setLayoutSize(context, mNoteWindowParams);
            // set view width and height
            mNoteWindowParams.x = (screenWidth - mNoteWindowParams.width) / 2;
            mNoteWindowParams.y = (screenHeight - mNoteWindowParams.height) / 2;
            // set this view is under notification and below all application
            mNoteWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mNoteWindowParams.format = PixelFormat.RGBA_8888;
            mNoteWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            // set this view not touch modal and not focusable
            mNoteWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;// | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            windowManager.removeView(mNoteWindow);
        }
        mNoteWindow.setParams(mNoteWindowParams);
        windowManager.addView(mNoteWindow, mNoteWindowParams);
    }

    /**
     * get WindowManager's instance
     *
     * @param context application context
     * @return instance of WindowManager
     */
    private static WindowManager getWindowManager(Context context) {
        if (null == mWindowManger) {
            mWindowManger = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManger;
    }

    /**
     * remove MainWindow from Window
     *
     * @param context
     */
    public static void removeMainWindow(Context context) {
        if (null != mMainWindow) {
            if (null == mWindowManger) {
                getWindowManager(context);
            }
            mWindowManger.removeView(mMainWindow);
            mMainWindow = null;
        }
    }

    /**
     * remove FloatButton from Window
     *
     * @param context
     */
    public static void removeFloatButton(Context context) {
        if (null != mFloatButton) {
            if (null == mWindowManger) {
                getWindowManager(context);
            }
            mWindowManger.removeView(mFloatButton);
            mFloatButton = null;
        }
    }

    /**
     * remove MusicWindow from Window
     *
     * @param context
     */
    public static void removeMusicWindow(Context context) {
        if (null != mMusicWindow) {
            if (null == mWindowManger) {
                getWindowManager(context);
            }
            mWindowManger.removeView(mMusicWindow);
            mMusicWindow = null;
        }
    }

    /**
     * remove VidowWindow from Window
     *
     * @param context
     */
    public static void removeVideoWindow(Context context) {
        if (null != mVideoWindow) {
            if (null == mWindowManger) {
                getWindowManager(context);
            }
            mWindowManger.removeView(mVideoWindow);
            mVideoWindow = null;
        }
    }

    /**
     * remove NoteWindow from Window
     *
     * @param context
     */
    public static void removeNoteWindow(Context context) {
        if (null != mNoteWindow) {
            if (null == mWindowManger) {
                getWindowManager(context);
            }
            mWindowManger.removeView(mNoteWindow);
            mNoteWindow = null;
        }
    }

    /* public static void showFloatButton(Context context) {
        if(null != mMainWindow) {
            removeMainWindow(context);
        }
        createFLoatButton(context);
    }*/

    /**
     * 1. get screen dpi
     * 2. according to dpi,set params width and height
     *
     * @param context
     * @param params
     */
    public static void setLayoutSize(Context context, WindowManager.LayoutParams params) {
        int i = context.getResources().getDisplayMetrics().densityDpi;
        WindowManager windowManager = getWindowManager(context);
        // get screen's width and height
        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        params.width = screenWidth / 3 * 2;
        params.height = screenWidth / 3 * 2 + 50;
    }
}
