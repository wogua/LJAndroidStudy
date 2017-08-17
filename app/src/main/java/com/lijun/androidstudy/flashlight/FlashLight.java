package com.lijun.androidstudy.flashlight;

import java.util.List;
import java.util.zip.Inflater;

import com.lijun.androidstudy.R;


import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * @author lijun
 */
public class FlashLight extends Activity {
    public final static String TAG = "FlashLight";

    private Camera m_Camera = null;
    private Camera.Parameters parameters;
    private ImageView mFlashView;
    private boolean isOn = false;
    private SoundPool sp;
    private int sourceid;

    private boolean exitIsOn = false;

    private int screenWidth;
    private int screenHeigh;
    private Rect turnArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_flash_light);
//		createPhoneListener();

        sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        sourceid = sp.load(this, R.raw.flashlight_turn_sound, 0);
        mFlashView = (ImageView) findViewById(R.id.flashturn);
        openLightOnWhenCreate();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeigh = dm.heightPixels;

    }


//	private Handler mHandler = new Handler(){
//		public void handleMessage(Message msg) {
//			switch(msg.what) {
//			case 1:
//				if(msg.arg1 == 0){
//					exitIsOn = false;
//				} else {
//					exitIsOn = true;
//				}
//				ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//
//				List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
//
//				RunningTaskInfo rti = runningTasks.get(0);
//				ComponentName component = rti.topActivity;
//				String componentName = component.getClassName();
//				Log.i("--FlashLight", "Activity"+componentName);
//				if(componentName != null){
//					if(!componentName.endsWith("FlashLight")){
//						mHandler.sendEmptyMessage(2);
//					}
//				}
//				break;
//			case 2:
//				FlashLight.this.onDestroy();
//			}
//		}
//	};

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        closeLightOffWhenExit();
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
//		Message m = mHandler.obtainMessage(1, isOn?1:0, 0);
//		mHandler.sendMessageDelayed(m, 100);
        exitIsOn = isOn;
        closeLightOffWhenExit();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (exitIsOn) {
            openLightOnWhenCreate();
        }
    }

    public void openLightOn() {
        isOn = true;
        playSound();
        mFlashView.setBackgroundResource(R.drawable.flash_on_bg);
        mFlashView.invalidate();
        try {
            if (null == m_Camera) {
                m_Camera = Camera.open();
            }
            parameters = m_Camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            m_Camera.setParameters(parameters);
            m_Camera.startPreview();
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(this, R.string.camera_connect_error, 2000);
        }
    }

    public void openLightOnWhenCreate() {
        isOn = true;
        mFlashView.setBackgroundResource(R.drawable.flash_on_bg);
        mFlashView.invalidate();
        try {
            if (null == m_Camera) {
                m_Camera = Camera.open();
            }
            parameters = m_Camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            m_Camera.setParameters(parameters);
            m_Camera.startPreview();
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(this, R.string.camera_connect_error, 2000);
        }
    }

    public void closeLightOff() {
        isOn = false;
        playSound();
        mFlashView.setBackgroundResource(R.drawable.flash_off_bg);
        mFlashView.invalidate();
        if (m_Camera != null) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            m_Camera.setParameters(parameters);
            m_Camera.startPreview();
        }
    }

    public void closeLightOffWhenExit() {
        isOn = false;
        mFlashView.setBackgroundResource(R.drawable.flash_off_bg);
        mFlashView.invalidate();
        if (m_Camera != null) {
            m_Camera.stopPreview();
            m_Camera.release();
            m_Camera = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
                this.onDestroy();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void playSound() {
        Log.d(TAG, "flashlight playSound");
        AudioManager am = (AudioManager) getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = am.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
                || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }

        float audioMaxVolumn = am
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolumn = am
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolumn / audioMaxVolumn;

        sp.play(sourceid, volumnRatio, volumnRatio, 1, 0, 1);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Log.i("--lijun--", "Flashlight onTouchEvent x = " + x + "  y = "
                    + y);

            int contentTop = getWindow()
                    .findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int l = (int) (screenWidth * 0.5 - (90.0 / 540) * screenWidth * 0.5);
            int r = (int) (screenWidth * 0.5 + (90.0 / 540) * screenWidth * 0.5);
            int t = screenHeigh
                    - (int) (255.0 / (960 - 38) * (screenHeigh - contentTop));
            int b = screenHeigh
                    - (int) (115.0 / (960 - 38) * (screenHeigh - contentTop));
            turnArea = new Rect(l, t, r, b);
            if (turnArea.contains(x, y)) {
                if (isOn) {
                    closeLightOff();
                } else {
                    openLightOn();
                }
            }
        }
        return super.onTouchEvent(event);
    }


//	public void createPhoneListener() {
//		TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//		telephony.listen(new OnePhoneStateListener(),
//				PhoneStateListener.LISTEN_CALL_STATE);
//	}

//	class OnePhoneStateListener extends PhoneStateListener {
//
//		public void onCallStateChanged(int state, String incomingNumber) {
//			switch (state) {
//			case TelephonyManager.CALL_STATE_RINGING:
//				Log.i(TAG, "[Listener]�ȴ��ӵ绰:" + incomingNumber);
////				FlashLight.this.onDestroy();
//				break;
//			case TelephonyManager.CALL_STATE_IDLE:
//				Log.i(TAG, "[Listener]�绰�Ҷ�:" + incomingNumber);
//				break;
//			case TelephonyManager.CALL_STATE_OFFHOOK:
//				Log.i(TAG, "[Listener]ͨ����:" + incomingNumber);
//				break;
//			}
//			super.onCallStateChanged(state, incomingNumber);
//		}
//	}
}
