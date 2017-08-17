package com.lijun.androidstudy.halo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import com.lijun.androidstudy.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class HaloView extends FrameLayout{
    private static int MAXSCROLL = 400;//解锁触发距离
    private static int HALOPIECE = 6;
    private static int STARTANIMALDISTANCE = 330;//发散光圈的距离
    HaloActivity mContext;
    private Bitmap[] mBitmaps;
    private ImageView[] mImageViews;//随手指移动的光晕
    private ImageView[] mImageAnimalViews;//点击屏幕后开始以动画形式移动的光晕
    private ImageView mImageViewHaloRing;//点击后开始逐渐变大的光圈
    private ImageView mImageViewHaloLong;//点击后开始旋转的光线
    private ImageView mImageViewHaloTwoColor;//对称彩虹图标随手指滑动
    private ImageView mImageViewHaloParticle;//发散光线图标随手指滑动
    private float oriX,oriY = -1;
    private float curX,curY = -1;
    private ArrayList<float[]> paths;
    private ArrayList<float[]> animalPaths;
    private ArrayList<float[]> animalScales;
    private float[] pathZ1 = {0.2f,0.4f,0.5f,0.8f,1.0f,1.2f};
    private float[] pathZ2 = {0.4f,0.6f,0.65f,0.8f,1.0f,1.2f};
    private float[] pathZ3 = {0.1f,0.3f,0.4f,0.7f,0.9f,1.2f};
    private float[] pathZ4 = {0.3f,0.5f,0.6f,0.8f,0.9f,1.2f};
    private float[] pathZ5 = {0.2f,0.5f,0.8f,1.0f,1.1f,1.2f};

    private float[] pathZZ1 = {0.2f,0.4f,0.5f,0.8f,1.0f,1.2f};
    private float[] pathZZ2 = {0.1f,0.6f,0.65f,0.8f,1.0f,1.2f};
    private float[] pathZZ3 = {0.1f,0.15f,0.2f,0.7f,0.9f,1.2f};
    private float[] pathZZ4 = {0.05f,0.1f,0.4f,0.7f,0.9f,1.2f};
    private float[] pathZZ5 = {0.4f,0.5f,0.8f,1.0f,1.1f,1.2f};

    private float[] pathScale1 = {0.2f,0.4f,0.5f,0.8f,1.0f,0.9f};
    private float[] pathScale2 = {0.1f,0.6f,0.65f,0.8f,1.0f,0.7f};
    private float[] pathScale3 = {0.8f,0.15f,0.2f,0.7f,0.9f,0.8f};
    private float[] pathScale4 = {0.7f,0.5f,0.5f,0.2f,0.7f,0.4f};
    private float[] pathScale5 = {0.1f,0.5f,0.6f,0.9f,0.9f,1.0f};

    private float[] curPath;
    private float[] curPathAnimal;
    private float[] curPathScale;
    private int[] picOrder;
    private int[] picOrderAnimal;
    private static int HALO_RING_WIDTH = 200;
    float m,n,p = 0;

    private VelocityTracker vTracker = null;

    private static final int TOUCH_STATE_REST = 0;// 手指离开屏幕状态
    private static final int TOUCH_STATE_SCROLLING = 1;
    private int mTouchState = TOUCH_STATE_REST;// 手指与屏幕是否接触状态
    private boolean endAnimalFinished = true;
    /* 控制动画播放时机 */
    private long startMoveTime;
    private long curTouchTime;
    private float vTrackerXYVelocity;

    private static float animScale;//滑动到一定程度后开始动画
    MyRunnable mRunnable;

    private SoundPool mSoundPool;
    private int mSoundID;

    private AlphaAnimation endAlphaAnimation;
    public HaloView(Context context) {
        super(context);
        this.mContext = (HaloActivity) context;
        init();
    }

    private void init() {

        mSoundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 10);
        mSoundID = mSoundPool.load(this.getContext(), R.raw.lens_flare_tap, 1);
        mBitmaps = new Bitmap[3];
        mImageViews = new ImageView[HALOPIECE];
        mImageAnimalViews  = new ImageView[HALOPIECE];
        for(int i = 0 ; i < HALOPIECE ; i ++){
            mImageViews[i] = (ImageView) mContext.getLayoutInflater().inflate(R.layout.halo_piece,this,false);
            mImageAnimalViews[i] = (ImageView) mContext.getLayoutInflater().inflate(R.layout.halo_piece,this,false);
            this.addView(mImageViews[i]);
        }
        for(int i = 0 ; i < HALOPIECE ; i ++){
            this.addView(mImageAnimalViews[i]);
        }

        mImageViewHaloRing = (ImageView) mContext.getLayoutInflater().inflate(R.layout.halo_ring,this,false);
        this.addView(mImageViewHaloRing);
        mImageViewHaloLong = (ImageView) mContext.getLayoutInflater().inflate(R.layout.halo_long,this,false);
        this.addView(mImageViewHaloLong);
        mImageViewHaloTwoColor  = (ImageView) mContext.getLayoutInflater().inflate(R.layout.halo_twocolor,this,false);
        this.addView(mImageViewHaloTwoColor);
        mImageViewHaloParticle  = (ImageView) mContext.getLayoutInflater().inflate(R.layout.halo_twocolor,this,false);
        this.addView(mImageViewHaloParticle);
        paths = new ArrayList<float[]>() {
        };
        paths.add(pathZ1);
        paths.add(pathZ2);
        paths.add(pathZ3);
        paths.add(pathZ4);
        paths.add(pathZ5);

        animalPaths = new ArrayList<float[]>() {
        };
        animalPaths.add(pathZZ1);
        animalPaths.add(pathZZ2);
        animalPaths.add(pathZZ3);
        animalPaths.add(pathZZ4);
        animalPaths.add(pathZZ5);
        animalScales= new ArrayList<float[]>() {
        };
        animalScales.add(pathScale1);
        animalScales.add(pathScale2);
        animalScales.add(pathScale3);
        animalScales.add(pathScale4);
        animalScales.add(pathScale5);
    }

    public void setBitmaps(Bitmap b1,Bitmap b2,Bitmap b3){
        if(mBitmaps == null){
            mBitmaps = new Bitmap[3];
        }
        mBitmaps[0] = b1;
        mBitmaps[1] = b2;
        mBitmaps[2] = b3;
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    updateImages();
                    mRunnable.next = true;
                    break;

                default:
                    break;
            }
        }
    };

    class MyRunnable implements Runnable{
        public boolean next = true;
        boolean finished = false;
        int a = 0;
        @Override
        public void run() {
            while(!finished && next && a < 10){
                next = false;
                animScale = 0.8f + 0.02f*a;
                Log.e("--lj--"," updateImages animScale :"+animScale);
                HaloView.this.mHandler.sendEmptyMessage(1);
                a ++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    private void updateImages(){
        float aX = curX - oriX;
        float aY = curY - oriY;
        float relateXY = (float) Math.sqrt((curX - oriX)*(curX - oriX)+(curX - oriX)*(curX - oriX));
        Log.d("--lj--"," updateImages animScale :"+animScale);
        for (int i = 0; i < HALOPIECE; i++) {
            mImageViews[i].setRotation(90*(relateXY/400));
            mImageViews[i].setTranslationX(aX*curPath[i]*animScale);
            mImageViews[i].setTranslationY(aY*curPath[i]*animScale);
            mImageViews[i].setScaleX(0.2f+curPath[i]*0.8f*animScale);
            mImageViews[i].setScaleY(0.2f+curPath[i]*0.8f*animScale);
        }
        mImageViewHaloTwoColor.layout((int)curX-250, (int)curY-250, (int)(curX + 250), (int)(curY + 250));
        mImageViewHaloTwoColor.setRotation(90*(relateXY/400));
        mImageViewHaloParticle.layout((int)curX-220, (int)curY-220, (int)(curX + 220), (int)(curY + 220));
        mImageViewHaloParticle.setRotation(90*(relateXY/400));

        invalidate();
    }

    private void playHolaStartAnimal(){
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.8f, 1.0f, 1.8f);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, (float) (-HALO_RING_WIDTH*0.4), 0, (float) (-HALO_RING_WIDTH*0.4));

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        set.addAnimation(translateAnimation);
        set.setDuration(2000);
        set.setInterpolator(new DecelerateInterpolator());
        set.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                mImageViewHaloRing.setVisibility(View.INVISIBLE);
            }
        });
        mImageViewHaloRing.startAnimation(set);


        AlphaAnimation alphaAnimation2 = new AlphaAnimation(1.0f, 0.0f);
        float startRotae = randWhich(359);
        RotateAnimation rotateAnimation2 = new RotateAnimation(startRotae, startRotae+50, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
        AnimationSet set2 = new AnimationSet(true);
        set2.addAnimation(alphaAnimation2);
        set2.addAnimation(rotateAnimation2);
        set2.setDuration(2000);
        set2.setInterpolator(new DecelerateInterpolator());
        set2.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                mImageViewHaloLong.setVisibility(View.INVISIBLE);
            }
        });
        mImageViewHaloLong.startAnimation(set2);

        float direction = (float) Math.toRadians(randWhich(359));//发散方向随机
        for(int i = 0; i < HALOPIECE; i++){
            Log.d("--lijun--", "aaaa");
            TranslateAnimation translateAnimationPer = new TranslateAnimation(0, (float) ( STARTANIMALDISTANCE*Math.sin(direction))*curPathAnimal[i], 0, (float) ( STARTANIMALDISTANCE*Math.cos(direction))*curPathAnimal[i]);
            AlphaAnimation alphaAnimationPer = new AlphaAnimation(1.0f, 0.0f);
            ScaleAnimation scaleAnimationPer = new ScaleAnimation(curPathScale[i], curPathScale[i]*1.5f, curPathScale[i], curPathScale[i]*1.5f);
            AnimationSet setPer = new AnimationSet(true);
            setPer.setDuration(2000);
            setPer.addAnimation(translateAnimationPer);
            setPer.addAnimation(alphaAnimationPer);
            setPer.addAnimation(scaleAnimationPer);
            setPer.setFillAfter(true);
            setPer.setInterpolator(new DecelerateInterpolator());
            mImageAnimalViews[i].startAnimation(setPer);
        }
    }

    private void startDrawHalo(){
        if(!endAnimalFinished){
            endAlphaAnimation.cancel();
        }
        startMoveTime = Calendar.getInstance().getTimeInMillis();
        animScale = 0.8f;
        picOrder = randRange(6, 3);
        picOrderAnimal = randRange(6, 3);
        for(int i = 0 ; i < HALOPIECE ; i ++){
            mImageViews[i].setVisibility(View.VISIBLE);
            mImageViews[i].setImageBitmap(mBitmaps[picOrder[i]]);
            mImageAnimalViews[i].setVisibility(View.VISIBLE);
            mImageAnimalViews[i].setImageBitmap(mBitmaps[picOrderAnimal[i]]);
        }
        curPath = paths.get(randWhich(paths.size()));
        curPathAnimal = animalPaths.get(randWhich(animalPaths.size()));
        curPathScale = animalScales.get(randWhich(animalScales.size()));


        mImageViewHaloRing.setVisibility(View.VISIBLE);
        mImageViewHaloLong.setVisibility(View.VISIBLE);
        mImageViewHaloTwoColor.setVisibility(View.VISIBLE);
        mSoundPool.play(mSoundID, 1.0f, 1.0f, 1, 0, 0.5f);
        playHolaStartAnimal();
        requestLayout();
        invalidate();
    }

    private void onDrawHalo() {
        if (curPath == null || curPath.length == 0)
            return;

        curTouchTime = Calendar.getInstance().getTimeInMillis();
        long intervalTime = curTouchTime - startMoveTime;
        float relateXY = (float) Math.sqrt((curX - oriX)*(curX - oriX)+(curX - oriX)*(curX - oriX));
//		Log.d("--lj--"," intervalTime:" + intervalTime + "   relateXY:" + relateXY +"  TrackerXY:"+vTrackerXYVelocity);
        if(relateXY > 120 && intervalTime < 500 && vTrackerXYVelocity < 80){
            mRunnable = new MyRunnable();
            new Thread(mRunnable).start();
        }
        updateImages();
    }

    private void endDrawHalo(){
        endAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        endAlphaAnimation.setDuration(1000);
        endAlphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                endAnimalFinished = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                if(mTouchState == TOUCH_STATE_SCROLLING){
                    return;
                }
                for(int i = 0 ; i < HALOPIECE ; i ++){
                    mImageViews[i].setVisibility(View.GONE);
                    mImageAnimalViews[i].setVisibility(View.GONE);
                }
                mImageViewHaloRing.setVisibility(View.INVISIBLE);
                mImageViewHaloLong.setVisibility(View.INVISIBLE);
                mImageViewHaloTwoColor.setVisibility(View.INVISIBLE);
                invalidate();
                endAnimalFinished = true;
            }
        });
        this.startAnimation(endAlphaAnimation);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        // TODO Auto-generated method stub
        if(oriX == -1 || oriY == -1 )return;
        for(int i = 0 ; i < HALOPIECE ; i ++){
            mImageViews[i].layout((int)oriX-60, (int)oriY-60, (int)(oriX + 60), (int)(oriY + 60));
        }
        for(int i = 0 ; i < HALOPIECE ; i ++){
            mImageAnimalViews[i].layout((int)oriX-60, (int)oriY-60, (int)(oriX + 60), (int)(oriY + 60));
        }
        mImageViewHaloRing.layout((int)oriX-HALO_RING_WIDTH/2, (int)oriY-HALO_RING_WIDTH/2, (int)(oriX + HALO_RING_WIDTH/2), (int)(oriY + HALO_RING_WIDTH/2));
        mImageViewHaloLong.layout((int)oriX-250, (int)oriY-60, (int)(oriX + 250), (int)(oriY + 60));
        mImageViewHaloTwoColor.layout((int)curX-250, (int)curY-250, (int)(curX + 250), (int)(curY + 250));
        Log.d("--lijun--", "curX : " + curX);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = (int) event.getX();
        float y = (int) event.getY();
//		Log.d("--lj--"," x:" + x + "   y:" + y);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if(vTracker == null){
                vTracker = VelocityTracker.obtain();
            }else{
                vTracker.clear();
            }
            vTracker.addMovement(event);
            oriX = curX = x;
            oriY = curY = y;

            startDrawHalo();
            onDrawHalo();
        }else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            mTouchState = TOUCH_STATE_REST;
            vTracker.clear();
            vTracker.recycle();
            vTracker = null;
            oriX = curX = -1;
            oriY = curY = -1;
            endDrawHalo();
        }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mTouchState = TOUCH_STATE_SCROLLING;
            vTracker.addMovement(event);
            vTracker.computeCurrentVelocity(1000);
//			Log.i("--lj--", "vTracker X : " + vTracker.getXVelocity());
//			Log.i("--lj--", "vTracker Y : " + vTracker.getYVelocity());
            vTrackerXYVelocity = (float) Math.sqrt(vTracker.getXVelocity()
                    * vTracker.getXVelocity() + vTracker.getYVelocity()
                    * vTracker.getYVelocity());
            curX = x;
            curY = y;
            onDrawHalo();
        }
        return true;
    }

    /**
     * 获取num个0-max的随机数
     * @param num
     * @param max
     * @return
     */
    public int[] randRange(int num, int max) {
        int result[] = new int[num];
        Random rand = new Random();
        for(int i = 0 ; i < num ; i ++){
            result[i] = rand.nextInt(max);
        }
        return result;
    }
    public int randWhich(int len) {
        Random rand = new Random();
        return rand.nextInt(len);
    }
}