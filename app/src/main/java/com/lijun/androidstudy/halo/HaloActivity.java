package com.lijun.androidstudy.halo;

import com.lijun.androidstudy.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;

public class HaloActivity extends Activity {
    private Bitmap halolightGreen;
    private Bitmap halolightBlue;
    private Bitmap halolightOrange;
    HaloView mHaloView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
//		setContentView(new HaloView(this));
        mHaloView = new HaloView(this);
        LayoutInflater inflater = getLayoutInflater();
        setContentView(mHaloView);
        for (int i = 0; i < 6; i++) {
            ImageView v = (ImageView) inflater.inflate(R.layout.halo_piece, mHaloView, false);
            mHaloView.addView(v);
        }
        halolightGreen = ((BitmapDrawable) getResources().getDrawable(
                R.drawable.keyguard_flare_hexagon_green)).getBitmap();
        halolightBlue = ((BitmapDrawable) getResources().getDrawable(
                R.drawable.keyguard_flare_hexagon_blue)).getBitmap();
        halolightOrange = ((BitmapDrawable) getResources()
                .getDrawable(R.drawable.keyguard_flare_hexagon_orange))
                .getBitmap();

        mHaloView.setBitmaps(halolightGreen, halolightBlue, halolightOrange);

    }

}
