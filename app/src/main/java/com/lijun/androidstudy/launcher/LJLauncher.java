package com.lijun.androidstudy.launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.util.Utilities;

import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.DataSetObserver;
import android.graphics.BitmapFactory;

public class LJLauncher extends Activity implements View.OnClickListener {
    private static String TAG = "LJMain";
    private LayoutInflater mInflater;
    private String TAG_WORKSPACE = "cells";

    View mLauncherView = null;
    LJWorkSpace mWorkspace = null;
    ArrayList<LJCellInfo> mCellInfos = null;

    static final int ROWS = 4;//行数
    static final int COLUMNS = 4;//列数
    int screenWidth = 0;
    private int cellSize = 0;
    private int screens = 0;

    private float downX,currenX,relativeX;
    private float downY,currenY,relativeY;
    private static int FLIP_MENU_Y = 20;//上滑大于FLIP_MENU_Y开始显示菜单
    private boolean toDrawMenu = true;
    private boolean isDrawingMenu = false;
    private View slideMenuView ;
    private ListView slideMenuLlistView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);

        mLauncherView = (View) findViewById(R.id.launcher);
        mWorkspace = (LJWorkSpace) findViewById(R.id.workspace);
        slideMenuView = (View) findViewById(R.id.slide_menu);
        slideMenuLlistView = (ListView) findViewById(R.id.slide_menu_list);
        List<Map<String, Object>> data=getData();
        slideMenuLlistView.setAdapter(new SlideMenuListAdapter(this,data));
        mInflater = getLayoutInflater();
        bindWorkspace();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v instanceof LJCellView) {
            LJCellView cv = (LJCellView)v;
            Log.i(TAG, "onClick v : " + cv.getTitle());
            if(cv.intent != null){
                startActivitySafety(cv.intent);
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        menu.add(0, 1, 0, R.string.scroll_style_classics_title);
        menu.add(0, 2, 0, R.string.scroll_style_cube_title);
        menu.add(0, 3, 0, R.string.scroll_style_columnar_title);
        menu.add(0, 4, 0, R.string.scroll_style_sphere_title);
        menu.add(0, 5, 0, R.string.scroll_style_3drota_title);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int style = 1;
        switch (item.getItemId()) {
            case 1:
                style = LJWorkSpace.SCROLL_STYLE_CLASSICS;
                break;
            case 2:
                style = LJWorkSpace.SCROLL_STYLE_CUBE;
                break;
            case 3:
                style = LJWorkSpace.SCROLL_STYLE_COLUMNAR;
                break;
            case 4:
                style = LJWorkSpace.SCROLL_STYLE_SPHERE;
                break;
            case 5:
                style = LJWorkSpace.SCROLL_STYLE_3DROTA;
                break;
        }
        setScrollStyle(style);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        float y = event.getY();
        float x = event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = currenX = x;
            downY = currenY = y;
            toDrawMenu = true;
        }else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            endSlideMenu();
        }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            currenX = x;
            currenY = y;
            relativeX = Math.abs(currenX - downX);
            relativeY = Math.abs(currenY - downY);
            Log.d("--lijun--", "relativeY:"+relativeY + "  relativeX:"+relativeX);
            if(currenY>downY){//下滑return
                return super.onTouchEvent(event);
            }
            if (relativeY < FLIP_MENU_Y) {
                if (relativeX > relativeY * 0.4 && relativeY > FLIP_MENU_Y/2) {
                    Log.e("--lijun--", "11");
                    toDrawMenu = false;
                }
            }else{
                if(toDrawMenu){
                    if(!isDrawingMenu){
                        startSlideMenu();
                    }else{
                        onSlideMenu();
                    }
                } else{
                    Log.e("--lijun--", "22");
                    toDrawMenu = false;
                }
            }

        }
        return super.onTouchEvent(event);
    }

    private void bindWorkspace() {
        Log.e(TAG, "bindWorkspace");
        mCellInfos = (ArrayList<LJCellInfo>) loadCellsFromXml(R.xml.workspace);
        if(mCellInfos == null || mCellInfos.size() == 0) {
            Log.e(TAG, "no cell");
            return;
        }
        cellSize = mCellInfos.size();
        screens = (cellSize%(ROWS*COLUMNS) == 0)?cellSize/(ROWS*COLUMNS):(cellSize/(ROWS*COLUMNS)+1);
        for(int i = 0 ; i < screens ; i ++){
            LJCellLayout cellLaout = new LJCellLayout(this);
            int start = i*ROWS*COLUMNS;
            int end = (i+1)*ROWS*COLUMNS -1;
            end = end > (cellSize-1) ? (cellSize-1):end;

            for(int j = start ; j <= end ; j ++){
                LJCellView cellView = createLJCellView(cellLaout,mCellInfos.get(j));
                cellLaout.addView(cellView);
            }
            mWorkspace.addView(cellLaout);
        }
        mWorkspace.invalidate();
    }

    private List<LJCellInfo> loadCellsFromXml(int resourceId) {
        List<LJCellInfo> cells = new ArrayList<LJCellInfo>();
        Resources res = getResources();
        try {
            XmlResourceParser parser = getResources().getXml(resourceId);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            beginDocument(parser, TAG_WORKSPACE);

            final int depth = parser.getDepth();

            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                LJCellInfo cell = new LJCellInfo();
                TypedArray a = obtainStyledAttributes(attrs, R.styleable.Cell);
                final String name = parser.getName();
                Log.d(TAG, "cell name : " + name);
                cell.name = a.getString(R.styleable.Cell_name);
                cell.className = a.getString(R.styleable.Cell_className);
                cell.packageName = a.getString(R.styleable.Cell_packageName);
                cell.icon = BitmapFactory.decodeResource(res, a.getResourceId(R.styleable.Cell_icon, 0));
                cells.add(cell);
                a.recycle();
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            Log.w(TAG, "Got exception parsing cells.", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.w(TAG, "Got exception parsing cells.", e);
        } catch (RuntimeException e) {
            Log.w(TAG, "Got exception parsing cells.", e);
        }
        return cells;
    }

    private static final void beginDocument(XmlPullParser parser,
                                            String firstElementName) throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
            ;
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found "
                    + parser.getName() + ", expected " + firstElementName);
        }
    }

    private LJCellView createLJCellView(ViewGroup parent ,LJCellInfo cellInfo ) {
        LJCellView cellView = (LJCellView) mInflater.inflate(R.layout.cell_layout,
                parent, false);
        cellView.setmCellInfo(cellInfo);
        cellView.setOnClickListener(this);
        return cellView;
    }

    private boolean startActivitySafety(Intent intent) {
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ex) {
            android.util.Log.d(TAG, "ActivityNotFoundException");
            return false;
        }
    }

    public int getScrollStyle(){
        if(mWorkspace != null){
            return mWorkspace.mScrollStyle;
        }else{
            return LJWorkSpace.SCROLL_STYLE_CLASSICS;
        }
    }

    public void setScrollStyle(int style){
        switch (style) {
            case LJWorkSpace.SCROLL_STYLE_CLASSICS:
            case LJWorkSpace.SCROLL_STYLE_CUBE:
            case LJWorkSpace.SCROLL_STYLE_3DROTA:
                break;
            case LJWorkSpace.SCROLL_STYLE_COLUMNAR:
                break;
            case LJWorkSpace.SCROLL_STYLE_SPHERE:
                break;


            default:
                break;
        }
        mWorkspace.scrollStyleChanged(style);
        SharedPreferences sp= getSharedPreferences("LJLauncher", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(LJWorkSpace.SCROLL_STYLE_KEY, style);
        editor.commit();
    }

    private void startSlideMenu(){
        isDrawingMenu = true;
        slideMenuView.setVisibility(View.VISIBLE);
        slideMenuView.setTranslationY(200);
        mLauncherView.invalidate();
    }

    private void onSlideMenu(){
        if((relativeY - FLIP_MENU_Y) >= 200){
            slideMenuView.setTranslationY(0);
        }else{
            slideMenuView.setTranslationY(200 - relativeY + FLIP_MENU_Y);
        }
        mLauncherView.invalidate();
    }

    private void endSlideMenu(){
        isDrawingMenu = false;
        slideMenuView.setVisibility(View.GONE);
    }

    class SlideMenuListAdapter extends BaseAdapter{

        private List<Map<String, Object>> data;
        private LayoutInflater layoutInflater;
        private Context context;
        public SlideMenuListAdapter(Context context,List<Map<String, Object>> data) {
            this.context=context;
            this.data=data;
            this.layoutInflater=LayoutInflater.from(context);
        }

        final class SlideMenuItem{
            ImageView slideMenuItemIconView;
            TextView slideMenuItemTextView;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            SlideMenuItem itemLayout = null;
            if (convertView == null) {
                itemLayout = new SlideMenuItem();
                convertView = mInflater.inflate(R.layout.slide_menu_list_item, null);
                itemLayout.slideMenuItemIconView =  (ImageView)convertView.findViewById(R.id.slide_menu_icon);
                itemLayout.slideMenuItemTextView =  (TextView)convertView.findViewById(R.id.slide_menu_title);
                convertView.setTag(itemLayout);
            }else{
                itemLayout = (SlideMenuItem)convertView.getTag();
            }
            itemLayout.slideMenuItemIconView.setImageResource((Integer)data.get(position).get("icon"));
            itemLayout.slideMenuItemTextView.setText((String)data.get(position).get("title"));
            return convertView;
        }
    }

    public List<Map<String, Object>> getData() {
        int[] icons = Utilities.getIds(this.getResources(),
                R.array.slide_menu_icons);
        String[] titles = this.getResources().getStringArray(
                R.array.slide_menu_titles);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < icons.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("icon", icons[i]);
            map.put("title", titles[i]);
            list.add(map);
        }
        return list;
    }
}