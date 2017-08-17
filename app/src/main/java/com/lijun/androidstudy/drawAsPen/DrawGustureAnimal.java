package com.lijun.androidstudy.drawAsPen;


import java.util.List;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DrawGustureAnimal extends ViewGroup {
    static final int MESSAGE_ANIMAL_END = 102;

    List<Drawable> mDrawables;
    ImageView mImageView;
    AnimationDrawable mAnim;
    TPGensturePreChar mActivity;
    private int animalLeft, animalTop, animalRight, animalBottom;

    public DrawGustureAnimal(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mActivity = (TPGensturePreChar) context;
        setBackgroundColor(0xFF000000);
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setmDrawables(List<Drawable> mDrawables) {
        this.mDrawables = mDrawables;
    }

    public void startAnimal(Context context) {
        this.removeAllViews();
        mImageView = new ImageView(context);
        addView(mImageView);

        mAnim = new AnimationDrawable();
        for (int i = 0; i < mDrawables.size(); i++) {
            mAnim.addFrame(mDrawables.get(i), TPGensturePreChar.FRAME_DELAY);
        }
        mAnim.setOneShot(true);
        mImageView.setBackground(mAnim);
        mAnim.start();
        new Thread(drawRunnable).start();
    }

    Runnable drawRunnable = new Runnable() {
        @Override
        public void run() {
            int m = 0;
            while (m < 30) {//!mAnim.isEnd()
                m++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            mHandler.sendEmptyMessage(MESSAGE_ANIMAL_END);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MESSAGE_ANIMAL_END:
                    mActivity.endDrawAninal();
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        int size = getChildCount();
        if (size <= 0) return;
        View v = getChildAt(0);
        v.layout(animalLeft, animalTop, animalRight, animalBottom);
    }

    public void setPosition(int l, int t, int r, int b) {
        animalLeft = l;
        animalTop = t;
        animalRight = r;
        animalBottom = b;
    }

}
