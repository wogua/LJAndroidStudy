package com.lijun.androidstudy.floatmeltitask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.lijun.androidstudy.R;

public class FloatMultiTaskFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.floatmultitask_preference);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isOpen = sp.getBoolean("float_multi_task", false);
        CheckBoxPreference floatSwitchPreference = (CheckBoxPreference) findPreference("float_multi_task");
        floatSwitchPreference.setOnPreferenceChangeListener(this);

//        Intent intent = new Intent("com.malata.floatmultitask.action.changestatus");
//        intent.putExtra("open", isOpen);
//        getActivity().sendBroadcast(intent);
//        if(isOpen){
//            Intent startService = new Intent(getActivity(), FloatMultiTaskService.class);
//            getActivity().startService(startService);
//        }else {
//            Intent stopService = new Intent(getActivity(), FloatMultiTaskService.class);
//            getActivity().stopService(stopService);
//        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        if ("float_multi_task".equals(key)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean isOpen = sp.getBoolean("float_multi_task", false);
            Intent intent = new Intent("com.malata.floatmultitask.action.changestatus");
            intent.putExtra("open", !isOpen);
            getActivity().sendBroadcast(intent);
        }
        return true;
    }
}
