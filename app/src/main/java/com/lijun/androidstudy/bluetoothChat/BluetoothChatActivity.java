package com.lijun.androidstudy.bluetoothChat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.qrcode.QRCodeShareActivity;

/**
 * Created by lijun on 17-10-23.
 */

public class BluetoothChatActivity extends Activity implements View.OnClickListener {

    public static final String TAG = "AppListActivity";
    private ListView listView;
    private List<Map<String, Object>> list;


    private BluetoothAdapter bluetoothAdapter;//本地蓝牙适配器


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "created");
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.bluetoothchat);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listView = (ListView) this.findViewById(R.id.bt_app_list);
        list = new ArrayList<Map<String, Object>>();
        List<PackageInfo> appListInfo = this.getPackageManager().getInstalledPackages(0);
        for (PackageInfo p : appListInfo) {
            if (p.applicationInfo.sourceDir.startsWith("/system/app/")) {
                continue;
            }
            Map<String, Object> map = new HashMap<String, Object>();
            Drawable icon = null;
            String appName = "";
            try {
                appName = this.getPackageManager().getApplicationLabel(p.applicationInfo).toString();
                icon = this.getPackageManager().getApplicationIcon(p.applicationInfo.packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("name", appName);
            map.put("package", p.applicationInfo.packageName);
            map.put("sourceDir", p.applicationInfo.sourceDir);
            map.put("icon", icon);
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.bluetoothchat_item, new String[]{"name", "icon"}, new int[]{R.id.bt_app_name, R.id.bt_app_icon});
        adapter.setViewBinder(new ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView && data instanceof Drawable) {
                    ImageView iv = (ImageView) view;
                    iv.setImageDrawable((Drawable) data);
                    return true;
                } else
                    return false;
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (list.get(position).get("sourceDir") != null) {
                    File f = new File(list.get(position).get("sourceDir").toString());
                    onShare(f);
                }
                return false;
            }
        });
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        Log.v(TAG, "destroy");
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }

    private void onShare(File file) {
        //分享应用
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_SEND);
//                    intent.setType("*/*");
//                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                    startActivity(intent);

        //用蓝牙分享应用
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_SEND);
//                    intent.setType("*/*");
//                    intent.setPackage("com.android.bluetooth");
//                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                    Intent chooser = Intent.createChooser(intent, "Share app");
//                    startActivity(chooser);

        //用蓝牙分享应用(跳过app chooser界面)
//                    Intent it = new Intent();
//                    it.setAction(Intent.ACTION_SEND);
//                    it.setType("*/*");
//                    it.setClassName("com.android.bluetooth","com.android.bluetooth.opp.BluetoothOppLauncherActivity");
//                    it.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                    startActivity(it);

        shareQR(file);
    }

    private void shareQR(File file) {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "本地蓝牙不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        String address = bluetoothAdapter.getAddress(); //获取本机蓝牙MAC地址
        String name = bluetoothAdapter.getName();   //获取本机蓝牙名称
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
        }
        Log.i(QRCodeShareActivity.TAG, "shareQR Bluetooth Address : " + address);
        Log.i(QRCodeShareActivity.TAG, "shareQR Bluetooth Name : " + name);

        Intent intent = new Intent(BluetoothChatActivity.this, QRCodeShareActivity.class);
        intent.putExtra("qr_content", file.getAbsolutePath());
        intent.putExtra("bt_address", address);
        intent.putExtra("bt_name", name);
        startActivityForResult(intent, 0);
    }
}
