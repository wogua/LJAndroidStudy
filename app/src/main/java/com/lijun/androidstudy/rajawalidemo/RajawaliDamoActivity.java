package com.lijun.androidstudy.rajawalidemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.rajawalidemo.skybox.SkyboxRenderer;

import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.ISurface;

/**
 * Created by lijun on 17-12-6.
 */

public class RajawaliDamoActivity extends AppCompatActivity {

    Renderer mRenderer;
    protected ISurface mRenderSurface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photograph);

        mRenderSurface = (ISurface) findViewById(R.id.photograph_surface);

        mRenderer = new SkyboxRenderer(this);
        mRenderSurface.setSurfaceRenderer(mRenderer);
    }
}
