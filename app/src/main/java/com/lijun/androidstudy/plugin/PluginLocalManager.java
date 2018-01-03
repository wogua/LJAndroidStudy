package com.lijun.androidstudy.plugin;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.didi.virtualapk.PluginManager;

import java.io.File;
import java.util.HashMap;

/**
 * Created by lijun on 18-1-3.
 */

public class PluginLocalManager {

    private static HashMap<String,String> sPlugins = new HashMap<>();//<包名,apk路径>
    static {
        sPlugins.put("plugin.lijun.com.myplugin","myplugin.apk");
    }
    public static boolean loadPlugin(Context context, int pluginType) {
        String apkName = null;
        String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/plugin/" + apkName);
        return loadPlugin(context, apkPath);
    }

    /**
     * 加载插件
     */
    public static boolean loadPlugin(Context context, String apkName) {
        String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/plugin/" + apkName);
        File _file = new File(apkPath);
        boolean mFileExists = _file.exists();
        Log.d("PluginLocalManager","loadPlugin --- _path = " + apkPath+",  mFileExists = " + mFileExists );
        if (mFileExists) {
            try {
                PluginManager.getInstance(context).loadPlugin(_file);
            } catch (Exception _e) {
                _e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public static void startPluginActivity(Context context,Intent intent) {
        if(intent == null || intent.getComponent() == null)return;
        String packageName = intent.getComponent().getPackageName();
        String className = intent.getComponent().getClassName();
        if(PluginManager.getInstance(context).getLoadedPlugin(packageName) == null){
            loadPlugin(context,sPlugins.get(packageName));
        }else {
            Intent _intent = new Intent();
            _intent.setClassName(packageName, className);
            context.startActivity(_intent);
        }
    }
}
