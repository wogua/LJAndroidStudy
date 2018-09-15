package com.lijun.androidstudy.drawAsPen;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.chinesematch.ChineseMatchUtil;
import com.lijun.androidstudy.chinesematch.PinyinSimilarity;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class DrawAsPenActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawTrapezoid(this));
        String aa = ChineseMatchUtil.getFullPinYinList("A陈宗健S3");
        String bb = ChineseMatchUtil.getPinyinSimilary("陈宗健");
        Log.d("lijun33","full === " + aa);
        Log.d("lijun33","all === " + bb);
    }

    String changeToOurWords(String input) {
        String output = input;
        output = new PinyinSimilarity(true).changeOurWordsWithPinyin(output);
        return output;
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
