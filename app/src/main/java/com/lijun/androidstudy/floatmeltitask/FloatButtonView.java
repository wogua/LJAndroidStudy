package com.lijun.androidstudy.floatmeltitask;

import com.lijun.androidstudy.R;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

public class FloatButtonView extends LinearLayout implements View.OnClickListener,
        View.OnTouchListener, View.OnLongClickListener {

    private static final String TAG = "TouchPointView";
    // WindowManager object,use to update location in Screen
    private WindowManager mWindowManager;
    // float button view
    private View mTouchView;
    // whole view layout,use to play animation
    private LinearLayout mWholeLayout;
    // float button's height
    public static int viewHeight;
    // float button's width
    public static int viewWidth;
    // height of status bar
    private static int statusBarHeight;
    // the view's layout params
    private WindowManager.LayoutParams mParams;
    // touch event action down X position
    private float touchX;
    // touch event action down Y position
    private float touchY;
    // touch X position in screen
    private float xInScreen;
    // touch Y position in screen
    private float yInScreen;

    private Context mContext;

    public FloatButtonView(Context context) {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.touch_point, this);
        mWholeLayout = (LinearLayout) findViewById(R.id.touchWholeButton);
        mTouchView = findViewById(R.id.touch_point);
        viewHeight = mTouchView.getLayoutParams().height;
        viewWidth = mTouchView.getLayoutParams().width;
        setOnClickListener(this);
        setOnLongClickListener(this);
        setOnTouchListener(this);
        // play animation
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_in);
        mWholeLayout.setAnimation(anim);
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }


    @Override
    public void onClick(View v) {
        // play animation
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // not use
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // to main window
                MultiTaskManager.removeFloatButton(getContext());
                MultiTaskManager.createMainWindow(getContext());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // no use
            }
        });
        mWholeLayout.startAnimation(anim);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isLongClick) {
            touchX = event.getX();
            touchY = event.getY();
            return false;
        }
        Log.d("--lj--", "onTouch");
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
                mWindowManager.updateViewLayout(this, mParams);
                Log.i(TAG, "action_move");
                break;
            case MotionEvent.ACTION_UP:
                setOnClickListener(this);
                isLongClick = true;
                setOnLongClickListener(this);
                break;
            default:
                break;
        }
        return true;
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
     * if true,long click listener is available
     */
    private boolean isLongClick = true;

    @Override
    public boolean onLongClick(View v) {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0, 50}, -1);
        setOnLongClickListener(null);
        setOnClickListener(null);
        isLongClick = false;
        return false;
    }
}
