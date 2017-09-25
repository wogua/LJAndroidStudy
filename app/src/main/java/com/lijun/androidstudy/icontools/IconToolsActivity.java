package com.lijun.androidstudy.icontools;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import com.lijun.androidstudy.R;

import org.json.JSONArray;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class IconToolsActivity extends AppCompatActivity {

    public static final String TAG = "XmlBuildTools";

    public static final String AUTHORITY = "com.android.launcher3.settings".intern();
    public static final String TABLE_NAME = "favorites";
    private final Uri URI = Uri.parse("content://" +
            AUTHORITY + "/" + TABLE_NAME);
    public static final String SCREEN = "screen";
    public static final String TITLE = "title";
    public static final String CELLX = "cellX";
    public static final String CELLY = "cellY";
    public static final String INTENT = "intent";
    public static final String CONTAINER = "container";
    public static final String ITEMTYPE = "itemType";

    public static final String FAVORITE = "favorite";
    public static final String PACKAGENAME = "launcher:packageName";
    public static final String CLASSNAME = "launcher:className";
    public static final String XMLSCREEN = "launcher:screen";
    public static final String X = "launcher:x";
    public static final String Y = "launcher:y";
    String enter = "\r\n"/* System.getProperty("line.separator")*/;


    public static final int REQUEST_PERMISSION_ALL = 0;
    TreeSet<ScreenIconBean> mFolderBean = new TreeSet<ScreenIconBean>();

    TreeSet<ScreenIconBean> mIconBean = new TreeSet<ScreenIconBean>(new Comparator<ScreenIconBean>() {
        @Override
        public int compare(ScreenIconBean o1, ScreenIconBean o2) {
            int i = 100;
            int j = 10;
            return o1.screen * i + o1.cellY * j + o1.cellX - (o2.screen * i + o2.cellY * j + o2.cellX);
        }
    });

    TreeSet<ThemeIconBean> mThemeIconBean = new TreeSet<ThemeIconBean>(new Comparator<ThemeIconBean>() {
        @Override
        public int compare(ThemeIconBean o1, ThemeIconBean o2) {
            return o1.isSystem?-1:1;
        }
    });
    public static String[] sAllPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INSTALL_SHORTCUT};

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icontools);
        checkPermission();

        View viewThemeConfig = findViewById(R.id.theme_config_button);
        View viewScreenConfig = findViewById(R.id.screen_config_button);
        viewThemeConfig.setOnClickListener(myOnClickListener);
        viewScreenConfig.setOnClickListener(myOnClickListener);
    }

    public void initSettings(final File settings) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(settings);

                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(fos, "UTF-8");
                    serializer.startDocument("UTF-8", true);
                    serializer.startTag(null, "config");
                    serializer.startTag(null, "category");
                    serializer.text(enter);
                    for (ScreenIconBean bean : mIconBean) {
                        serializer.startTag(null, FAVORITE);//;
                        serializer.attribute(null, PACKAGENAME, bean.packageName);//serializer.text(enter);
                        serializer.attribute(null, CLASSNAME, bean.className);//serializer.text(enter);
                        serializer.attribute(null, XMLSCREEN, String.valueOf(bean.screen));//serializer.text(enter);
                        serializer.attribute(null, X, String.valueOf(bean.cellX));//serializer.text(enter);
                        serializer.attribute(null, Y, String.valueOf(bean.cellY));//serializer.text(enter);
                        serializer.endTag(null, FAVORITE);
                        serializer.text(enter);//serializer.text(enter);
                    }


                    //serializer.attribute(null, "name", "hot");
                    // server
                 /*   serializer.startTag(null, "item");
                    serializer.attribute(null, "id", "server");
                    serializer.attribute(null, "value", "");
                    serializer.endTag(null, "item");
                    // hid
                    serializer.startTag(null, "item");
                    serializer.attribute(null, "id", "hotel");
                    serializer.attribute(null, "value", "");
                    serializer.endTag(null, "item");
                    // room
                    serializer.startTag(null, "item");
                    serializer.attribute(null, "id", "room");
                    serializer.attribute(null, "value", "");
                    serializer.endTag(null, "item");
*/
                    serializer.endTag(null, "category");
                    serializer.endTag(null, "config");
                    serializer.endDocument();
                    serializer.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private void showToast(boolean success) {
        Toast.makeText(this, success ? "success" : "failed", Toast.LENGTH_SHORT).show();
    }

    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.screen_config_button) {
                setUnreadNumber(3);
                generateScreenConfig();
            } else if (v.getId() == R.id.theme_config_button) {
                setUnreadNumber2(6);
                testGetShortcutList();
                createShortCut();
                generateThemeIconConfig();
            }
        }
    };

    private boolean setUnreadNumber(int count){
        String method = "setBadge";
        Bundle b = new Bundle();
        b.putInt("count",count);
        try {
            Uri uri = Uri.parse("content://com.android.dlauncher.badge/badge");
            Bundle bundle = getContentResolver().call(uri, method, null, b);
            if (bundle != null && bundle.getBoolean("result")) {
                Log.d("lijun22", "setUnreadNumber true");
                return true;
            } else {
                Log.d("lijun22", "setUnreadNumber false");
                return false;
            }
        }catch (Exception e){
            Log.d("lijun22", "setUnreadNumber exception : " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    private boolean setUnreadNumber2(int count){
        String method = "setAppBadgeCount";
        Bundle b = new Bundle();
        b.putStringArrayList("app_shortcut_custom_id", null);
        b.putInt("app_badge_count", 10);
        try {
            Uri uri = Uri.parse("content://com.android.dlauncher.badge/badge");
            Bundle bundle = getContentResolver().call(uri, method, null, b);
            if (bundle != null && bundle.getBoolean("result")) {
                Log.d("lijun22", "setUnreadNumber2 true");
                return true;
            } else {
                Log.d("lijun22", "setUnreadNumber2 false");
                return false;
            }
        }catch (Exception e){
            Log.d("lijun22", "setUnreadNumber exception : " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    private void testGetShortcutList(){
        String method = "getShortcutList";
        Bundle b = new Bundle();
        try {
            Uri uri = Uri.parse("content://com.android.dlauncher.badge/badge");
            Bundle bundle = getContentResolver().call(uri, method, null, b);
            if (bundle != null) {
                Log.d("lijun22", "testGetShortcutList true");
                String result = bundle.getString("shortcut_list");
                JSONArray jsonArray = new JSONArray(result);
            } else {
                Log.d("lijun22", "testGetShortcutList false");
            }
        }catch (Exception e){
            Log.d("lijun22", "testGetShortcutList exception : " + e.toString());
            e.printStackTrace();
        }
    }

    private void createShortCut(){
        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,"TestBadgeShortCut"
                /*getString(R.string.app_name)*/);
        shortcut.putExtra("duplicate", false);
        Intent shortcutIntent = new Intent();
        shortcutIntent.setClassName("com.lijun.androidstudy.icontools",
                "com.lijun.androidstudy.icontools.IconToolsActivity");
        shortcutIntent.putExtra("app_shortcut_custom_id",
                "custom_id_001");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(
                this, R.drawable.icon_tools);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        sendBroadcast(shortcut,Manifest.permission.INSTALL_SHORTCUT);//com.android.launcher.permission.INSTALL_SHORTCUT
    }

    private void generateScreenConfig(){
        Cursor cursor = null;
        mIconBean.clear();
        try {
            cursor = getContentResolver().query(URI, null, null, null, null);
            cursor.moveToFirst();
            String[] names = cursor.getColumnNames();
            for (String name : names) {
                Log.d(TAG, "name=" + name);
            }
            Log.d(TAG, "size=" + cursor.getCount());
            do {
                if (cursor != null) {
                    int anInt = cursor.getInt(cursor.getColumnIndex(CONTAINER));
                    String cursorString = cursor.getString(cursor.getColumnIndex(INTENT));
                    if (cursor.getInt(cursor.getColumnIndex(ITEMTYPE)) == 2) {

                    }
                    if (anInt == -101 || cursorString == null) {
                        continue;
                    }

                    ScreenIconBean iconBean = new ScreenIconBean();
                    Intent intent = Intent.parseUri(cursorString, 0);
                    ComponentName component = intent.getComponent();
                    iconBean.className = component.getClassName();
                    iconBean.packageName = component.getPackageName();
                    iconBean.cellX = cursor.getInt(cursor.getColumnIndex(CELLX));
                    iconBean.cellY = cursor.getInt(cursor.getColumnIndex(CELLY));
                    iconBean.screen = cursor.getInt(cursor.getColumnIndex(SCREEN));
                    mIconBean.add(iconBean);
                    Log.d(TAG, "iconBean=" + iconBean.toString());
                }
                Log.d(TAG, "mIconBean.size()=" + mIconBean.size());
            }
            while (cursor.moveToNext());
                  /*  cursor.moveToNext();
                    for(int i = 0 ;i < 52 ;i++){
                        cursor.moveToNext();
                        if (cursor != null ) {
                            String[] names = cursor.getColumnNames();
                            for (String name : names) {
                                int index = cursor.getColumnIndex(name);
                                Log.d(TAG, name + "=" + cursor.getString(index));
                            }
                        }
                    }*/
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
                File file = new File(Environment.getExternalStorageDirectory() + "/default_workspace.xml");
                initSettings(file);
                showToast(true);
            }
        }
    }

    private void generateThemeIconConfig() {
        getThemeIconBeans();
        saveThemeConfig(Environment.getExternalStorageDirectory() + "/icon_config.xml");
    }

    private void getThemeIconBeans() {
        Cursor cursor = null;
        mThemeIconBean.clear();
        try {
            cursor = getContentResolver().query(URI, null, null, null, null);
            cursor.moveToFirst();
            String[] names = cursor.getColumnNames();
            for (String name : names) {
                Log.d(TAG, "name=" + name);
            }
            Log.d(TAG, "size=" + cursor.getCount());
            do {
                if (cursor != null) {
                    String cursorString = cursor.getString(cursor.getColumnIndex(INTENT));
                    if (cursorString == null) continue;
                    ThemeIconBean themeIconBean = new ThemeIconBean();
                    Intent intent = Intent.parseUri(cursorString, 0);
                    ComponentName component = intent.getComponent();
                    if(component == null){
                        Log.d(TAG, "intent=" + intent.getPackage());
                        continue;
                    }
                    themeIconBean.className = component.getClassName();
                    themeIconBean.packageName = component.getPackageName();
                    themeIconBean.isSystem = isSystemApp(component.getPackageName(),IconToolsActivity.this);
                    themeIconBean.title = cursor.getString(cursor.getColumnIndex(TITLE));
                    themeIconBean.iconName = "xxx.png";
                    mThemeIconBean.add(themeIconBean);
                }
            }
            while (cursor.moveToNext());
            Log.d(TAG, "themeIconBeens.size()=" + mThemeIconBean.size());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void enterItem(XmlSerializer serializer,String tag,String name,String value){
        try {
            serializer.startTag(null, tag);
            serializer.attribute("", "name", name);
            serializer.text(value);
            serializer.endTag(null, tag);
            serializer.text(enter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void entercomment(XmlSerializer serializer,String comment){
        try {
            serializer.comment(comment);
            serializer.text(enter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveThemeConfig(final String filePath) {
        if (mThemeIconBean == null || mThemeIconBean.size() <= 0) {
            showToast(false);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                boolean success = true;
                try {
                    fos = new FileOutputStream(filePath);
                    XmlSerializer serializer = Xml.newSerializer();
                    serializer.setOutput(fos, "UTF-8");
                    serializer.startDocument("UTF-8", true);
                    serializer.text(enter);
                    serializer.startTag(null, "resources");

                    serializer.text(enter);
                    entercomment(serializer,"主题名字");
                    enterItem(serializer,"string","theme_name","Monster Theme");
                    entercomment(serializer,"主题版本号");
                    enterItem(serializer,"string","theme_version","v1.1");
                    entercomment(serializer,"主题类型是否为异型");
                    enterItem(serializer,"bool","is_heteromorphic_theme","false");
                    entercomment(serializer,"三方没有重绘的图标在底板中的显示位置");
                    enterItem(serializer,"dimen","ptop","0dp");
                    enterItem(serializer,"bool","pleft","0dp");
                    enterItem(serializer,"bool","pright","0dp");
                    enterItem(serializer,"bool","pbottom","0dp");
                    serializer.text(enter);
                    entercomment(serializer,"日历日期文本颜色");
                    enterItem(serializer,"color","com.android.launcher3$dym_calendar_day_text","#eac796");
                    entercomment(serializer,"日历星期文本颜色");
                    enterItem(serializer,"color","com.android.launcher3$dym_calendar_week_text","#eac796");
                    entercomment(serializer,"天气文本颜色");
                    enterItem(serializer,"color","com.android.launcher3$dym_weather_text","#eac796");

                    serializer.text(enter);
                    serializer.startTag(null, "string-array");
                    serializer.text(enter);
                    boolean isSystem = true;
                    boolean firstOut = true;
                    entercomment(serializer," System App");
                    for (ThemeIconBean bean : mThemeIconBean) {
                        isSystem = bean.isSystem;
                        if(!isSystem && firstOut){
                            firstOut = false;
                            serializer.text(enter);
                            entercomment(serializer," ThirdPart App");
                        }
                        entercomment(serializer," " + bean.title+" ");
                        serializer.startTag(null, "item");
                        serializer.text(bean.getConfigString());
                        serializer.endTag(null, "item");
                        serializer.text(enter);
                    }

                    serializer.endTag(null, "string-array");
                    serializer.text(enter);
                    serializer.endTag(null, "resources");
                    serializer.endDocument();
                    serializer.flush();
                } catch (FileNotFoundException e) {
                    success = false;
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    success = false;
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    success = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    success = false;
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                final String toastString = success ? "success" : "failed";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IconToolsActivity.this, toastString, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }


    public static boolean isSystemApp(String packageName,Context context){
        PackageManager pm = context.getPackageManager();
        if("com.monster.launcher".equals(packageName))return true;
        if (packageName != null) {
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                return (info != null) && (info.applicationInfo != null) &&
                        ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
