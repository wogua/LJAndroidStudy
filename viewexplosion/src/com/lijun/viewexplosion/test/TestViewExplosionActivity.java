package com.lijun.viewexplosion.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.launcher.LJLauncher;

/**
 * Created by lijun on 17-11-21.
 */

public class TestViewExplosionActivity extends AppCompatActivity {

    CheckBox mCheckBox;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_view_explosion);
        mCheckBox = (CheckBox) findViewById(R.id.explosion_checkbox);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent("com.lijun.androidstudy.EXPLOSION_CHANGED");
                intent.putExtra("opened",isChecked);
                TestViewExplosionActivity.this.sendBroadcast(intent);
            }
        });
        mCheckBox.setChecked(LJLauncher.sExpolosionMode);
    }
}
