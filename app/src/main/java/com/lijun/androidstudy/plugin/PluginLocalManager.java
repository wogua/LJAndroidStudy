package com.lijun.androidstudy.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.didi.virtualapk.PluginManager;
import com.lijun.androidstudy.R;
import com.lijun.androidstudy.launcher.LJCellView;
import com.lijun.androidstudy.util.PhotoUtils;

import java.io.File;
import java.util.HashMap;

/**
 * Created by lijun on 18-1-3.
 */

public class PluginLocalManager {

    private static HashMap<String, String> sPlugins = new HashMap<>();//<包名,apk路径>

    static {
        sPlugins.put("plugin.lijun.com.myplugin", "myplugin.apk");
        sPlugins.put("com.ygkj.chelaile.standard", "chelaile.apk");
        sPlugins.put("com.andromeda.androbench2", "androbench2.apk");
    }

    /**
     * 加载插件
     */
    public static void loadPlugin(final Context context, String apkName, final LJCellView view) {
        String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/plugin/" + apkName);
        final File _file = new File(apkPath);
        boolean mFileExists = _file.exists();
        Log.d("PluginLocalManager", "loadPlugin --- _path = " + apkPath + ",  mFileExists = " + mFileExists);
        if (mFileExists) {
            AsyncTask<Void, Void, Boolean> as = new AsyncTask<Void, Void, Boolean>() {
                long dTime;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        dTime = System.currentTimeMillis();
                        Drawable drawable = context.getDrawable(R.drawable.ic_loading);
                        drawable.setAlpha(190);
                        view.setForegroundDrawable(drawable);
                    }
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        PluginManager.getInstance(context).loadPlugin(_file);
                    } catch (Exception _e) {
                        _e.printStackTrace();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    Log.d("PluginLocalManager", "loadPlugin result : " + result);
                    if (result) {
                        Log.d("PluginLocalManager", "currentThread 2 : " + Thread.currentThread());
                        Toast.makeText(context, "插件加载完成", Toast.LENGTH_SHORT).show();
                    }
                    if (System.currentTimeMillis() - dTime > 1000) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            view.setForegroundDrawable(null);
                        }
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    view.setForegroundDrawable(null);
                                }
                            }
                        }, 1000);
                    }

                }
            };
            as.execute();
        }
    }

//    public static void startPluginActivity(Context context,Intent intent) {
//        if(intent == null || intent.getComponent() == null)return;
//        String packageName = intent.getComponent().getPackageName();
//        Toast.makeText(context,"startPluginActivity ...",Toast.LENGTH_SHORT).show();
//        Log.d("PluginLocalManager","currentThread 1 : " + Thread.currentThread());
//        String className = intent.getComponent().getClassName();
//        if(PluginManager.getInstance(context).getLoadedPlugin(packageName) == null){
//            loadPlugin(context,sPlugins.get(packageName));
//        }else {
//            Intent _intent = new Intent();
//            _intent.setClassName(packageName, className);
//            context.startActivity(_intent);
//        }
//    }

    public static void startPluginActivity(Context context, LJCellView view) {
        Intent intent = view.getIntent();
        if (intent == null || intent.getComponent() == null) return;
        String packageName = intent.getComponent().getPackageName();
        Toast.makeText(context, "startPluginActivity ...", Toast.LENGTH_SHORT).show();
        Log.d("PluginLocalManager", "currentThread 1 : " + Thread.currentThread());
        String className = intent.getComponent().getClassName();
        if (PluginManager.getInstance(context).getLoadedPlugin(packageName) == null) {
            loadPlugin(context, sPlugins.get(packageName), view);
        } else {
            Intent _intent = new Intent();
            _intent.setClassName(packageName, className);
            context.startActivity(_intent);
        }
    }
}
