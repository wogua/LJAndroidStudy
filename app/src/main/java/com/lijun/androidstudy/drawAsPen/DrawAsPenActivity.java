package com.lijun.androidstudy.drawAsPen;

import com.lijun.androidstudy.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class DrawAsPenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawTrapezoid(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
//		getMenuInflater().inflate(R.menu.drawaspen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.draw_by_pen:
                setContentView(new DrawTrapezoid(this));
                break;
            case R.id.draw_by_picture:
                setContentView(new DrawTrapezoid(this));
                break;
            default:
                break;
        }
        return true;
    }

}
