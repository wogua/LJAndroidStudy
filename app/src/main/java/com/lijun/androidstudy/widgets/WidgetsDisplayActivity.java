package com.lijun.androidstudy.widgets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by lijun on 17-8-18.
 */

public class WidgetsDisplayActivity extends Activity{

    private View mWidgetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layoutid = getIntent().getIntExtra("layout",-1);

        if(layoutid < 0 ){
            finish();
        }
        setContentView(layoutid);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
