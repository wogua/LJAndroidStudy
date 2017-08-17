package com.lijun.androidstudy.floatmeltitask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class FloatMultiTaskReceiver extends BroadcastReceiver {

    public static boolean SERVICE_STARTING = false;
    private SharedPreferences sp;

    public FloatMultiTaskReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        if ("com.malata.floatmultitask.action.changestatus".equals(action)) {
            boolean isOpen = intent.getBooleanExtra("open", false);
            if (isOpen) {
                startService(context);
            } else {
                stopService(context);
            }
        } else if ("com.malata.floatmultitask.action.close".equals(action)) {// close float button
            sp = context.getSharedPreferences("float_multi_task", Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("float_multi_task", false);
            editor.commit();
            // close sms float window
            Intent i = new Intent("com.malata.floatmultitask.action.sms.close");
            i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);

        } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {// when boot completed,show float button
            sp = context.getSharedPreferences("float_multi_task", Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
            if (sp.getBoolean("float_multi_task", false)) {
                Intent startService = new Intent(context, FloatMultiTaskService.class);
                context.startService(startService);
            }
        } else if ("com.malata.floatmultitask.action.showfloatbutton".equals(action)) {// show float button
            Intent i = new Intent("com.malata.floatmultitask.action.sms.close");
            i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);
            MultiTaskManager.createFLoatButton(context);
        } else if ("com.malata.floatmultitask.action.showmainwindow".equals(action)) {// show main window
            Intent i = new Intent("com.malata.floatmultitask.action.sms.close");
            i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);
            MultiTaskManager.createMainWindow(context);
        } else if ("com.malata.floatmultitask.action.cleanbesidesms".equals(action)) {// clear other flaot window beside sms float window
            MultiTaskManager.removeNoteWindow(context);
            MultiTaskManager.removeMusicWindow(context);
            MultiTaskManager.removeVideoWindow(context);
            MultiTaskManager.removeFloatButton(context);
            MultiTaskManager.removeMainWindow(context);
        } else if ("com.malata.floatmultitask.action.enableautoshowsms".equals(action)) {// set auto show sms float window enabled
            Log.i("haha", "get set auto show sms");
            sp = context.getSharedPreferences("float_multi_task", Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isAutoShowSms", true);
            editor.commit();
        } else if ("com.malata.floatmultitask.action.disableautoshowsms".equals(action)) {// set auto show sms float window disabled
            Log.i("haha", "get set auto show sms false");
            sp = context.getSharedPreferences("float_multi_task", Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isAutoShowSms", false);
            editor.commit();
        }
    }

    private void startService(Context context) {
        if (SERVICE_STARTING) return;
        Log.d("--lijun--", "startService FloatMultiTaskService");
        Intent startService = new Intent(context, FloatMultiTaskService.class);
        context.startService(startService);
        SERVICE_STARTING = true;
    }

    private void stopService(Context context) {
        if (!SERVICE_STARTING) return;
        Log.d("--lijun--", "stopService FloatMultiTaskService");
        Intent stopService = new Intent(context, FloatMultiTaskService.class);
        context.stopService(stopService);
        SERVICE_STARTING = false;
    }
}
