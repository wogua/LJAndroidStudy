package com.lijun.androidstudy.widgets;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.lijun.androidstudy.R;

import java.util.ArrayList;

/**
 * Created by lijun on 17-8-18.
 */
public class WidgetsActivity extends ListActivity {

    static String[] s_widgets_name = new String[]{
            "AVLoading Display",
            "AVLoadingIndicatorView",
            "Loading58",
            "ShapeImageView",
            "LeafLoadingProgressBar",
            "WaveView",
    };

    static int[] s_widgets_layout = new int[]{
            R.layout.loadingindicator_display,
            R.layout.loading_indicator,
            R.layout.loading_58_display,
            R.layout.shapeimage,
            R.layout.leafloading,
            R.layout.waveview,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, s_widgets_name);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, WidgetsDisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("layout",s_widgets_layout[position]);
        startActivity(intent);
    }
}
