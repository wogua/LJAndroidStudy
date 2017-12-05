package com.lijun.androidstudy.rajawalidemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.lijun.androidstudy.R;

import org.rajawali3d.view.ISurface;

/**
 * Created by lijun on 17-12-5.
 */

public class Photograph extends AppCompatActivity {

    PhotographRenderer mRenderer;
    protected ISurface mRenderSurface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photograph);

        mRenderSurface = (ISurface) findViewById(R.id.photograph_surface);

        mRenderer = new PhotographRenderer(this);
        mRenderSurface.setSurfaceRenderer(mRenderer);
    }
}
