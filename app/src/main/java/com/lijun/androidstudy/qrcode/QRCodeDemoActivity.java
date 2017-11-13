package com.lijun.androidstudy.qrcode;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lijun.androidstudy.R;
import com.xys.libzxing.zxing.activity.CaptureActivity;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lijun on 17-10-31.
 */

public class QRCodeDemoActivity extends AppCompatActivity {

    private TextView mTextView;
    private EditText mEditText;
    private ImageView mImageView;
    private CheckBox mCheckBox;

    private BluetoothAdapter bluetoothAdapter;

    public static String filePath;
    public static String btAddress;
    public static String btName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.erweima_test);
        initView();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void initView() {
        mTextView = (TextView) this.findViewById(R.id.tv_showResult);
        mEditText = (EditText) this.findViewById(R.id.et_text);
        mImageView = (ImageView) this.findViewById(R.id.img_shouw);
        mCheckBox = (CheckBox) this.findViewById(R.id.cb_logo);
    }

    //扫描二维码
    //https://cli.im/text?2dd0d2b267ea882d797f03abf5b97d88二维码生成网站
    public void scan(View view) {
        startActivityForResult(new Intent(this, CaptureActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String result = bundle.getString("result");
                mTextView.setText(result);
                connectFile(result);
            }
        }

    }

    //生成二维码 可以设置Logo
    public void make(View view) {

        String input = mEditText.getText().toString();
        if (input.equals("")) {
            Toast.makeText(this, "输入不能为空", Toast.LENGTH_SHORT).show();
        } else {
            Bitmap qrCode = EncodingUtils.createQRCode(input, 500, 500,
                    mCheckBox.isChecked() ? BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher) : null);
            mImageView.setImageBitmap(qrCode);
        }
    }

    private void connectFile(String result) {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "本地蓝牙不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            filePath = jsonObject.getString("qr_content");
            btAddress = jsonObject.getString("bt_address");
            btName = jsonObject.getString("bt_name");
        } catch (JSONException e) {
            e.printStackTrace();
            filePath = null;
            btAddress = null;
            btName = null;
        }
        Log.d(QRCodeShareActivity.TAG,"scan result filePath = " + filePath);
        Log.d(QRCodeShareActivity.TAG,"scan result btAddress = " + btAddress);
        Log.d(QRCodeShareActivity.TAG,"scan result btName = " + btName);

        String Address = bluetoothAdapter.getAddress(); //获取本机蓝牙MAC地址
        String Name = bluetoothAdapter.getName();   //获取本机蓝牙名称
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
        } else {
            bluetoothAdapter.startDiscovery();
        }
    }

}
