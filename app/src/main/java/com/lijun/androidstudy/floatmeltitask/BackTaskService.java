package com.lijun.androidstudy.floatmeltitask;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * file name:BackTaskService.java
 * Copyright MALATA ,ALL rights reserved.
 * 2015-7-29
 * author:laiyang
 * <p>
 * The class is to control play music and video,extends to Service
 * <p>
 * Modification history
 * -------------------------------------
 * <p>
 * -------------------------------------
 */
public class BackTaskService extends Service implements Runnable,
        MediaPlayer.OnCompletionListener {

    // command to play music
    public static final int PLAY_MUSIC = 1;
    // command to play video
    public static final int PLAY_VIDEO = 3;
    // command to pause music
    public static final int MUSIC_PAUSE = 2;
    // command to pause video
    public static final int VIDEO_PAUSE = 22;
    // command to play next music
    public static final int PLAY_MUSIC_NEXT = 4;
    // command to play pre music
    public static final int PLAY_MUSIC_PRE = 5;
    // command to play next video
    public static final int PLAY_VIDEO_NEXT = 6;
    // command to play pre video
    public static final int PLAY_VIDEO_PRE = 7;
    /**
     * true is service playing music and false is service playing video
     */
    private static boolean isPlayMusic = false;
    /**
     * MediaPlayer's object
     */
    public static MediaPlayer mMediaPlayer = null;
    // command type
    private int mCommandType;

    public static boolean isPlaying() {
        return isPlaying;
    }

    /**
     * true means player is playing now, false means player is pause or stop
     */
    private static boolean isPlaying = false;

    /**
     * get current process of play music
     *
     * @return
     */
    public static int getCurrentMusicIndex() {
        return currentMusicIndex;
    }

    /**
     * get current process of play video
     *
     * @return
     */
    public static int getCurrentVideoIndex() {
        return currentVideoIndex;
    }

    /**
     * current music index in music list
     */
    private static int currentMusicIndex = 0;
    /**
     * current video index in video list
     */
    private static int currentVideoIndex = 0;
    /**
     * current music process
     */
    private static int currentMusicProcess = 0;
    /**
     * current video process
     */
    private static int currentVideoProcess = 0;

    /**
     * @return return if MediaPlayer is playing musice
     */
    public static boolean isPlayMusic() {
        return isPlayMusic;
    }

    /**
     * musics info
     */
    private ArrayList<HashMap<String, String>> musics;
    /**
     * music's lrc list
     */
    private static List<LrcContent> mLrcContents;
    /**
     * video list
     */
    private List<String> videos;
    /**
     * flag to Update process bar's thread, true the thread is alive
     */
    private static boolean isThreadAlive = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (mMediaPlayer != null) {// if MediaPlayer not null,reset
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        // init flags
        isPlaying = false;
        isThreadAlive = true;
        // start update processbar
        new Thread(this).start();
        mMediaPlayer = new MediaPlayer();
        // set onCompletion listener
        mMediaPlayer.setOnCompletionListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // when destroy service,release memory
        isPlaying = false;
        isThreadAlive = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        System.out.println("service onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mCommandType = intent.getIntExtra("MSG", PLAY_MUSIC);
        // when musicNames = null,get music names datas
        if (musics == null && (mCommandType == MUSIC_PAUSE || mCommandType == PLAY_MUSIC || mCommandType == PLAY_MUSIC_NEXT || mCommandType == PLAY_MUSIC_PRE)) {
            currentMusicIndex = 0;
            musics = MusicWindowView.getMusics();
//            musicDir = MusicWindowView.musicRootPath;
            if (musics == null) {// musicNames = null, return
                return super.onStartCommand(intent, flags, startId);
            }
        }
        // when videoNames = null,get video names datas
        if (videos == null && (mCommandType == PLAY_VIDEO || mCommandType == PLAY_VIDEO_NEXT || mCommandType == PLAY_VIDEO_PRE || mCommandType == VIDEO_PAUSE)) {
            videos = VideoWindowView.getVideos();
            currentVideoIndex = 0;
            if (videos == null) {// videoNames = null, return
                return super.onStartCommand(intent, flags, startId);
            }
        }
        // control play music or video
        switch (mCommandType) {
            case PLAY_MUSIC://play music
                playMusic();
                break;
            case PLAY_MUSIC_NEXT://next music
                isPlaying = false;
                currentMusicProcess = 0;
                currentMusicIndex++;
                if (musics != null && currentMusicIndex >= musics.size()) {
                    currentMusicIndex = 0;
                }
                playMusic();
                break;
            case PLAY_MUSIC_PRE://pre music
                isPlaying = false;
                currentMusicProcess = 0;
                currentMusicIndex--;
                if (currentMusicIndex < 0) {
                    currentMusicIndex = musics.size() - 1;
                }
                playMusic();
                break;
            case PLAY_VIDEO://play video
                playVideo();
                break;
            case MUSIC_PAUSE:// pause music
                if (mMediaPlayer.isPlaying()) {// music is playing
                    currentMusicProcess = mMediaPlayer.getCurrentPosition();
                    mMediaPlayer.pause();
                    isPlaying = false;
                } else {// music no play,continue to play
                    mMediaPlayer.seekTo(currentMusicProcess);
                    mMediaPlayer.start();
                    isPlaying = true;
                }
                break;
            case VIDEO_PAUSE:// pause video
                if (mMediaPlayer.isPlaying()) {// if is playing video, pause it
                    currentVideoProcess = mMediaPlayer.getCurrentPosition();
                    mMediaPlayer.pause();
                    isPlaying = false;
                } else {// if not playing, continue to play
                    mMediaPlayer.seekTo(currentVideoProcess);
                    mMediaPlayer.start();
                    isPlaying = true;
                }
                break;
            case PLAY_VIDEO_NEXT://next video
                isPlaying = false;
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                currentVideoProcess = 0;
                currentVideoIndex++;
                if (videos != null && currentVideoIndex >= videos.size()) {
                    currentVideoIndex = 0;
                }

                playVideo();
                break;
            case PLAY_VIDEO_PRE://pre video
                isPlaying = false;
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                currentVideoProcess = 0;
                currentVideoIndex--;
                if (currentVideoIndex < 0) {
                    currentVideoIndex = videos.size() - 1;
                }
                playVideo();
                break;
            default:
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void playVideo() {
        // set now MediaPlayer is playing video
        isPlayMusic = false;

        if (videos == null || 0 == videos.size() || currentVideoIndex > videos.size()) {
            // video resource not init, return
            return;
        }
        try {
            // reset media player
            mMediaPlayer.reset();

            if (currentVideoIndex < videos.size()) {
                mMediaPlayer.setDataSource(videos.get(currentVideoIndex));
            }
            SurfaceHolder holder = VideoWindowView.getSurfaceHodler();
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
            Thread.sleep(100);

            mMediaPlayer.setDisplay(holder);

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.seekTo(currentVideoProcess);
                    // play video
                    mp.start();
                    // set service is playing
                    isPlaying = true;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // send msg to update video window ui
                    VideoWindowView.sendMessage(VideoWindowView.MSG_PALY_SUCCESS);

                }
            });
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("static-access")
    public void playMusic() {
        // set current is playing music
        isPlayMusic = true;
        if (musics == null || musics.size() == 0) {
            return;
        }
        // set current music name
        MusicWindowView.sendMessage(MusicWindowView.MSG_UPDATE_SONG_NAME);
        try {
            // reset mediaplayer
            mMediaPlayer.reset();
            // set music data source
            if (currentMusicIndex < musics.size()) {
                mMediaPlayer.setDataSource(musics.get(currentMusicIndex).get("data"));
            }
            // prepare to play music
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(currentMusicProcess);
            mMediaPlayer.start();
            // set this flag media player is plaing
            isPlaying = true;
            // play success, update ui
            MusicWindowView.sendMessage(MusicWindowView.MSG_PLAY_SUCCESS);
            try {
                // get music's lrc
                mLrcContents = LrcReader.getLrcContents(musics.get(currentMusicIndex).get("data"));
                MusicWindowView.setLrcContents(mLrcContents);

            } catch (IOException e) {
                // read lrc file field
                e.printStackTrace();
            }
            if (mLrcContents != null && mLrcContents.size() != 0) {// if current music has lrc,show it
                new UpdateLrcThread().start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // update video process bar
    @Override
    public void run() {
        int CurrentPosition = 0;
        int total = 0;
        while (isThreadAlive) {// if true,update processbar
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (mMediaPlayer != null && !isPlayMusic) {// if is playing video,update process bar

                try {
                    Thread.sleep(1000);
                    if (mMediaPlayer != null && isPlaying) {
                        CurrentPosition = mMediaPlayer.getCurrentPosition();
                        total = mMediaPlayer.getDuration();
                        if (total != 0) {
                            VideoWindowView.sendMessage(VideoWindowView.MSG_UPDATE_PROGRESS_BAR, CurrentPosition * 100 / total);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // play completion,jump to next
        if (isPlayMusic) {
            isPlaying = false;
            currentMusicIndex++;
            if (currentMusicIndex >= musics.size()) {
                currentMusicIndex = 0;
            }
            playMusic();
        } else {
            isPlaying = false;
            currentVideoIndex++;
            if (currentVideoIndex >= videos.size()) {
                currentVideoIndex = 0;
            }
            playVideo();
        }
    }

    /**
     * get lrc contents of current music
     *
     * @return
     */
    public static List<LrcContent> getLrcContents() {
        return mLrcContents;
    }

    /**
     * Thread to update lrc view
     */
    private class UpdateLrcThread extends Thread {
        @Override
        public void run() {
            // current position
            int current = mMediaPlayer.getCurrentPosition();
            // mills of music
            int duration = mMediaPlayer.getDuration();
            // index of lrc list
            int index = 0;
            while (isPlayMusic && isPlaying && current < duration) {//is playing
                // send lrc index to MusicView
                MusicWindowView.sendMessage(MusicWindowView.MSG_SET_LRC_INDEX, index);
                int i = index;
                // calculate lrc index
                current = mMediaPlayer.getCurrentPosition();
                for (; i < mLrcContents.size() - 1; i++) {
                    if (mLrcContents.get(i + 1).getTime() >= current) {
                        index = i;
                        break;
                    }
                }
                if (i == mLrcContents.size() - 1 && mLrcContents.get(i).getTime() > current) {
                    index = i;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
