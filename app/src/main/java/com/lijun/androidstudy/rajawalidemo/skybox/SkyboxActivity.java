package com.lijun.androidstudy.rajawalidemo.skybox;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.lijun.androidstudy.R;

import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.ISurface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijun on 17-12-6.
 */

public class SkyboxActivity extends AppCompatActivity {

    SkyboxRenderer mRenderer;
    protected ISurface mRenderSurface;

    private Spinner spinner;
    private List<String> data_list;
    private int[] resIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skybox);

        spinner = (Spinner) findViewById(R.id.spinner_skybox);

        //数据
        data_list = new ArrayList<String>();
        data_list.add("天空海洋");
        data_list.add("深圳前海");
        resIds = new int[2];
        resIds[0] = R.drawable.vr_wallpaper1;
        resIds[1] = R.drawable.vr_wallpaper2;

        ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arr_adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < resIds.length && position >= 0) {
                    mRenderer.onPicSourceChanged(resIds[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRenderSurface = (ISurface) findViewById(R.id.photograph_surface);

        mRenderer = new SkyboxRenderer(this);
        mRenderSurface.setSurfaceRenderer(mRenderer);
    }
}
