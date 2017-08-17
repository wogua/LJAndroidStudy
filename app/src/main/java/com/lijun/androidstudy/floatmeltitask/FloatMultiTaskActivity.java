package com.lijun.androidstudy.floatmeltitask;

import com.lijun.androidstudy.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class FloatMultiTaskActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.floatmultitask_preference);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isOpen = sp.getBoolean("float_multi_task", false);
        SwitchPreference floatSwitchPreference = (SwitchPreference) findPreference("float_multi_task");
        floatSwitchPreference.setOnPreferenceChangeListener(this);

        Intent intent = new Intent("com.malata.floatmultitask.action.changestatus");
        intent.putExtra("open", isOpen);
        sendBroadcast(intent);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        final String key = preference.getKey();
        if ("float_multi_task".equals(key)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isOpen = sp.getBoolean("float_multi_task", false);
            Intent intent = new Intent("com.malata.floatmultitask.action.changestatus");
            intent.putExtra("open", !isOpen);
            sendBroadcast(intent);
        }
        return true;
    }
}
