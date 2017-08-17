package com.lijun.androidstudy.welcomeViewPager;

import com.lijun.androidstudy.launcher.LJLauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class GuideActivity extends Activity {

	boolean isFirstLoad = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,    
    	        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	
		SharedPreferences preferences = getSharedPreferences("first_pref",
				MODE_PRIVATE);

		isFirstLoad = preferences.getBoolean("isFirstLoad", true);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = null;
				if (isFirstLoad) {
					intent = new Intent(GuideActivity.this, WelcomeActivity.class);
				} else {
					intent = new Intent(GuideActivity.this, LJLauncher.class);
				}
				GuideActivity.this.startActivity(intent);
				GuideActivity.this.finish();
			}
		}, 100);
	}
}
