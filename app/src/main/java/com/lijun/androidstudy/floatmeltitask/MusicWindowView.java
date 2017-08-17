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
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by laiyang on 15-7-8.
 */
public class MusicWindowView extends LinearLayout implements View.OnClickListener,
        View.OnLongClickListener, View.OnTouchListener {

    private static final String TAG = "MusicWindowView";
    /**
     * update song name
     */
    public final static int MSG_UPDATE_SONG_NAME = 1001;
    /**
     * music play success
     */
    public final static int MSG_PLAY_SUCCESS = 1002;
    /**
     * stop to play music
     */
    public final static int MSG_PLAY_STOP = 1003;
    /**
     * update current lrc index in lrc list
     */
    public final static int MSG_SET_LRC_INDEX = 1004;

    public static MusicWindowView musicView;

    private WindowManager.LayoutParams mParams;
    /**
     * title layout
     */
    private RelativeLayout mrlTitle;

    private ImageButton mibPlay;

    private ImageButton mibNext;

    private ImageButton mibPre;

    private ImageButton mivBack;

    private ImageButton mivMinimize;

    private TextView mtvSongName;

    private LrcTextView mLrcView;

    private boolean isPlaying;

    private float touchX;

    private float touchY;

    private float xInScreen;

    private float yInScreen;

    private WindowManager windowManager;

    private static int statusBarHeight;
    // music files info
    private static ArrayList<HashMap<String, String>> musics;

    public static void setLrcContents(List<LrcContent> lrcContents) {
        musicView.mLrcView.setLrcContents(lrcContents);
        musicView.mLrcView.postInvalidate();
    }

    public static ArrayList<HashMap<String, String>> getMusics() {
        return musics;
    }

    public static MusicWindowView getMusicView() {
        return musicView;
    }

    public static void sendMessage(int what) {
        musicView.mHandler.sendEmptyMessage(what);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SONG_NAME:
                    mtvSongName.setText(getCurrentSongName());
                    break;
                case MSG_PLAY_SUCCESS:
                    isPlaying = true;
                    mibPlay.setImageResource(R.drawable.app_pause_button_bg);
                    break;
                case MSG_PLAY_STOP:
                    isPlaying = false;
                    mibPlay.setImageResource(R.drawable.app_play_button_bg);
                    break;
                case MSG_SET_LRC_INDEX:
                    mLrcView.setIndex((Integer) msg.obj);
                    mLrcView.postInvalidate();
                    break;
                default:
                    break;
            }
        }
    };

    private LinearLayout layout;

    public MusicWindowView(Context context) {
        super(context);
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.floatingwindowmusic, this);
        layout = (LinearLayout) findViewById(R.id.floatingWholerect);
        mrlTitle = (RelativeLayout) findViewById(R.id.windowcontrolrect);
        mibPlay = (ImageButton) findViewById(R.id.pause);
        mibNext = (ImageButton) findViewById(R.id.next);
        mibPre = (ImageButton) findViewById(R.id.prev);
        mivBack = (ImageButton) findViewById(R.id.btn_tofloatingwindowmain);
        mivMinimize = (ImageButton) findViewById(R.id.btn_tofloatingbutton);
        mtvSongName = (TextView) findViewById(R.id.songname);
        mLrcView = (LrcTextView) findViewById(R.id.lrcView);
        mivBack.setOnClickListener(this);
        mivMinimize.setOnClickListener(this);
        mibPlay.setOnClickListener(this);
        mibNext.setOnClickListener(this);
        mibPre.setOnClickListener(this);
        mrlTitle.setOnLongClickListener(this);
        mrlTitle.setOnTouchListener(this);
        // read music files
        loadAudio();
        if (0 == musics.size()) { // no song,can't click play button
            mibPlay.setEnabled(false);
            mibNext.setEnabled(false);
            mibPre.setEnabled(false);
            mtvSongName.setText(context.getString(R.string.no_song));
        }
        // if music is playing,show it's lrc
        if (BackTaskService.isPlaying()) {
            isPlaying = true;
            mibPlay.setImageResource(R.drawable.app_pause_normal);
            mLrcView.setLrcContents(BackTaskService.getLrcContents());
            mLrcView.setIndex(BackTaskService.getCurrentMusicIndex());
            mtvSongName.setText(musics.get(BackTaskService.getCurrentMusicIndex()).get("title"));
        } else {
            mibPlay.setImageResource(R.drawable.app_play_button_bg);
        }
        // play animation
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_in);
        layout.setAnimation(anim);
        musicView = this;
    }

    /**
     * set this view's layout params
     *
     * @param params
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mibPlay)) {
            Intent intent = new Intent();
            if (!isPlaying) {// play music
                intent.putExtra("MSG", BackTaskService.PLAY_MUSIC);
                mibPlay.setImageResource(R.drawable.app_pause_button_bg);
                isPlaying = true;
            } else {// pause music
                intent.putExtra("MSG", BackTaskService.MUSIC_PAUSE);
                mibPlay.setImageResource(R.drawable.app_play_button_bg);
                isPlaying = false;
            }
            intent.setClass(getContext(), BackTaskService.class);
            getContext().startService(intent);
            mtvSongName.setText(getCurrentSongName());
        } else if (v.equals(mivBack)) {// back to main button
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    musicView.setVisibility(View.GONE);
                    MultiTaskManager.removeMusicWindow(getContext());
                    MultiTaskManager.createMainWindow(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            layout.startAnimation(anim);

        } else if (v.equals(mivMinimize)) {// to float button
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    musicView.setVisibility(View.GONE);
                    MultiTaskManager.removeMusicWindow(getContext());
                    MultiTaskManager.createFLoatButton(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            layout.startAnimation(anim);
        } else if (v.equals(mibNext)) {// play next music
            Intent intent = new Intent();
            intent.putExtra("MSG", BackTaskService.PLAY_MUSIC_NEXT);
            intent.setClass(getContext(), BackTaskService.class);
            getContext().startService(intent);
        } else if (v.equals(mibPre)) {// play pre music
            Intent intent = new Intent();
            intent.putExtra("MSG", BackTaskService.PLAY_MUSIC_PRE);
            intent.setClass(getContext(), BackTaskService.class);
            getContext().startService(intent);
        }
    }

    /**
     * get current play audio name
     *
     * @return
     */
    private String getCurrentSongName() {
        if (0 == musics.size()) {
            return "";
        }
        return musics.get(BackTaskService.getCurrentMusicIndex()).get("title");
    }

    /**
     * send message to current handler
     *
     * @param what msg type
     * @param obj  message content
     */
    public static void sendMessage(int what, Object obj) {
        Message msg = musicView.mHandler.obtainMessage(what, obj);
        musicView.mHandler.sendMessage(msg);
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
//        mrlTitle.setOnTouchListener(this);
        return false;
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

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();

                mParams.x = (int) (xInScreen - touchX);
                mParams.y = (int) (yInScreen - touchY);
                windowManager.updateViewLayout(this, mParams);
                Log.i(TAG, "action_move");
                break;
            case MotionEvent.ACTION_UP:
                isLongClick = true;
                mrlTitle.setOnLongClickListener(this);
                break;
        }
        return false;
    }

    /**
     * get status bar height
     * use java inflect to load com.android.internal.R$dimen
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
     * get external audios in media store
     */
    public void loadAudio() {
        musics = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> music;
        ContentResolver resolver = getContext().getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA}, null, null, null);
        while (cursor.moveToNext()) {
            music = new HashMap<String, String>();
            music.put("title", cursor.getString(0));
            music.put("artist", cursor.getString(1));
            music.put("data", cursor.getString(2));
            musics.add(music);
            music = null;
        }
        cursor.close();
    }

}

