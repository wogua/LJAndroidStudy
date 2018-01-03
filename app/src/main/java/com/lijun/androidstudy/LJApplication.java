package com.lijun.androidstudy;

import com.didi.virtualapk.PluginManager;
import com.lijun.androidstudy.util.Utilities;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

public class LJApplication extends Application {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        Utilities.screenWidth = wm.getDefaultDisplay().getWidth();
        Utilities.screenHeight = wm.getDefaultDisplay().getHeight();
    }

    @Override
    protected void attachBaseContext(Context base)  {
        super.attachBaseContext(base);
        PluginManager.getInstance(base).init();
    }
}
