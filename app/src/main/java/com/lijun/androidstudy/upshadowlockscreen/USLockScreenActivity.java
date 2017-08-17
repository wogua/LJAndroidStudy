package com.lijun.androidstudy.upshadowlockscreen;

import com.lijun.androidstudy.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class USLockScreenActivity extends Activity {

    USLockScreenView mUSLockScreenView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lock_screen);
        mUSLockScreenView = (USLockScreenView) findViewById(R.id.lockview);
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mUSLockScreenView != null) {
            mUSLockScreenView.resetTriggler();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
//		return super.onTouchEvent(event);
        return mUSLockScreenView.onTouchEvent(event);
    }
}
