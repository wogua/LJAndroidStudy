package com.lijun.androidstudy.launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lijun.androidstudy.R;
import com.lijun.androidstudy.util.PhotoUtils;
import com.lijun.androidstudy.util.Utilities;
import com.lijun.viewexplosion.ExplosionField;
import com.lijun.viewexplosion.factory.ParticleFactory;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import android.graphics.BitmapFactory;
import android.widget.Toast;

public class LJLauncher extends Activity implements View.OnClickListener {
    private static String TAG = "LJMain";
    public static String[] sAllPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH};
    public static final int REQUEST_PERMISSION_ALL = 0;//add for checkAllPermission

    public static boolean sExpolosionMode = false;

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

    private View slideMenuView;
    private ListView slideMenuLlistView;
    int[] mSlideStyles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);

        checkPermission();
        mLauncherView = (View) findViewById(R.id.launcher);
        mWorkspace = (LJWorkSpace) findViewById(R.id.workspace);
        mInflater = getLayoutInflater();
        initSlidingMenu();
        bindWorkspace();
        registerBroadcastReceiver();
    }

    public void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.lijun.androidstudy.EXPLOSION_CHANGED");
        registerReceiver(mBroadcastReceiver, filter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.lijun.androidstudy.EXPLOSION_CHANGED")) {
                boolean isOpened = intent.getBooleanExtra("opened",false);
                onExpolosionChanged(isOpened);
            }
        }
    };

    private void onExpolosionChanged(boolean isOpened) {
        if (isOpened) {
            Toast.makeText(getApplicationContext(), "Explosion Mode Opened",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Explosion Mode Closed",
                    Toast.LENGTH_LONG).show();
        }
        sExpolosionMode = isOpened;
        if (mWorkspace != null && mWorkspace.getChildCount() > 0) {
            for (int i = 0; i < mWorkspace.getChildCount(); i++) {
                LJCellLayout ljCellLayout = (LJCellLayout) mWorkspace.getChildAt(i);
                for (int j = 0; j < ljCellLayout.getChildCount(); j++) {
                    LJCellView cellView = (LJCellView) ljCellLayout.getChildAt(j);
                    LJCellInfo cellInfo = cellView.getmCellInfo();
                    if (isOpened&&!"com.lijun.viewexplosion.test.TestViewExplosionActivity".equals(cellInfo.className)) {
                        ExplosionField explosionField = new ExplosionField(this, ParticleFactory.getRandomParticle(j)/*new FallingParticleFactory()*/);
                        explosionField.addListener(cellView);
                    } else {
                        cellView.setOnClickListener(this);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v instanceof LJCellView) {
            LJCellView cv = (LJCellView) v;
            Log.i(TAG, "onClick v : " + cv.getTitle());
            if (cv.intent != null) {
                startActivitySafety(cv.intent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
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

    private void bindWorkspace() {
        Log.e(TAG, "bindWorkspace");
        mCellInfos = (ArrayList<LJCellInfo>) loadCellsFromXml(R.xml.workspace);
        if (mCellInfos == null || mCellInfos.size() == 0) {
            Log.e(TAG, "no cell");
            return;
        }
        cellSize = mCellInfos.size();
        screens = (cellSize % (ROWS * COLUMNS) == 0) ? cellSize / (ROWS * COLUMNS) : (cellSize / (ROWS * COLUMNS) + 1);
        for (int i = 0; i < screens; i++) {
            LJCellLayout cellLaout = new LJCellLayout(this);
            int start = i * ROWS * COLUMNS;
            int end = (i + 1) * ROWS * COLUMNS - 1;
            end = end > (cellSize - 1) ? (cellSize - 1) : end;

            for (int j = start; j <= end; j++) {
                LJCellView cellView = createLJCellView(cellLaout, mCellInfos.get(j));
                ///for view explosion example
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

            Bitmap maskBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_mask);
            Bitmap bgBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_bg);
            Bitmap zoomTemp = BitmapFactory.decodeResource(res, R.drawable.ic_zoom_template);
            int tempW = zoomTemp.getWidth();
            int tempH = zoomTemp.getHeight();

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
//                cell.icon = PhotoUtils.zoom(BitmapFactory.decodeResource(res, a.getResourceId(R.styleable.Cell_icon, 0)), tempW, tempH);
                cell.icon = PhotoUtils.compositeByBitmap(BitmapFactory.decodeResource(res, a.getResourceId(R.styleable.Cell_icon, 0)),maskBitmap,bgBitmap,zoomTemp,false);

//                cell.icon = BitmapFactory.decodeResource(res, a.getResourceId(R.styleable.Cell_icon, 0));
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

    private LJCellView createLJCellView(ViewGroup parent, LJCellInfo cellInfo) {
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

    public int getScrollStyle() {
        if (mWorkspace != null) {
            return mWorkspace.mScrollStyle;
        } else {
            return LJWorkSpace.SCROLL_STYLE_CLASSICS;
        }
    }

    public void setScrollStyle(int style) {
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
        SharedPreferences sp = getSharedPreferences("LJLauncher", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(LJWorkSpace.SCROLL_STYLE_KEY, style);
        editor.commit();
    }

    class SlideMenuListAdapter extends BaseAdapter {

        private List<Map<String, Object>> data;
        private LayoutInflater layoutInflater;
        private Context context;

        public SlideMenuListAdapter(Context context, List<Map<String, Object>> data) {
            this.context = context;
            this.data = data;
            this.layoutInflater = LayoutInflater.from(context);
        }

        final class SlideMenuItem {
            ImageView slideMenuItemIconView;
            TextView slideMenuItemTextView;
            int slideStyle;
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
                itemLayout.slideMenuItemIconView = (ImageView) convertView.findViewById(R.id.slide_menu_icon);
                itemLayout.slideMenuItemTextView = (TextView) convertView.findViewById(R.id.slide_menu_title);
                itemLayout.slideStyle = mSlideStyles[position];
                convertView.setTag(itemLayout);
                convertView.setOnClickListener(mSlidingMenuItemClick);
            } else {
                itemLayout = (SlideMenuItem) convertView.getTag();
            }
            itemLayout.slideMenuItemIconView.setImageResource((Integer) data.get(position).get("icon"));
            itemLayout.slideMenuItemTextView.setText((String) data.get(position).get("title"));
            return convertView;
        }
    }


    public List<Map<String, Object>> getData() {
        int[] icons = Utilities.getIds(this.getResources(),
                R.array.slide_menu_icons);
        String[] titles = this.getResources().getStringArray(
                R.array.slide_menu_titles);

        mSlideStyles = this.getResources().getIntArray(R.array.slide_menu_values);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < icons.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("icon", icons[i]);
            map.put("title", titles[i]);
            list.add(map);
        }
        return list;
    }

    private View.OnClickListener mSlidingMenuItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SlideMenuListAdapter.SlideMenuItem itemLayout = (SlideMenuListAdapter.SlideMenuItem) v.getTag();
            setScrollStyle(itemLayout.slideStyle);
        }
    };

    public void initSlidingMenu() {
        LayoutInflater inflater = LayoutInflater.from(this);
        slideMenuView = inflater.inflate(R.layout.slide_menu, (ViewGroup) mLauncherView, false);
        slideMenuLlistView = (ListView) slideMenuView.findViewById(R.id.slide_menu_list);
        List<Map<String, Object>> data = getData();
        slideMenuLlistView.setAdapter(new SlideMenuListAdapter(this, data));

        SlidingMenu localSlidingMenu = new SlidingMenu(this);
        localSlidingMenu.setMode(SlidingMenu.LEFT);//设置左右滑菜单
        localSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);//设置要使菜单滑动，触碰屏幕的范围
        //localSlidingMenu.setTouchModeBehind(SlidingMenu.RIGHT);
        localSlidingMenu.setShadowWidthRes(R.dimen.sliding_menu_shadow_width);//设置阴影图片的宽度
        localSlidingMenu.setShadowDrawable(R.drawable.shadow);//设置阴影图片
        localSlidingMenu.setBehindOffsetRes(R.dimen.sliding_menu_behind_offset);//设置划出时主页面显示的剩余宽度
        localSlidingMenu.setFadeEnabled(true);//设置滑动时菜单的是否渐变
        localSlidingMenu.setFadeDegree(0.35F);//设置滑动时的渐变程度
        localSlidingMenu.attachToActivity(this, SlidingMenu.RIGHT);//使SlidingMenu附加在Activity右边
//		localSlidingMenu.setBehindWidthRes(R.dimen.left_drawer_avatar_size);//设置SlidingMenu菜单的宽度
        localSlidingMenu.setMenu(slideMenuView);//设置menu的布局文件
//        localSlidingMenu.toggle();//动态判断自动关闭或开启SlidingMenu
        localSlidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            public void onOpened() {

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_PERMISSION_ALL){//lijun add for checkAllPermission
            if (grantResults.length > 0) {

            }
        }
    }

    private void checkPermission() {
        List<String> noOkPermissions = new ArrayList<>();

        for (String permission : sAllPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                noOkPermissions.add(permission);
            }
        }
        if (noOkPermissions.size() <= 0)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(noOkPermissions.toArray(new String[noOkPermissions.size()]), REQUEST_PERMISSION_ALL);
        }
    }
}