package com.lijun.androidstudy.floatmeltitask;

import android.app.Activity;
import android.os.Bundle;

public class FloatMultiTaskActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        this.getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new FloatMultiTaskFragment())
                .commit();
    }
}
