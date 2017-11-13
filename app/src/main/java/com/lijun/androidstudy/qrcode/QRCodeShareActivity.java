package com.lijun.androidstudy.qrcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.lijun.androidstudy.R;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lijun on 17-11-10.
 */

public class QRCodeShareActivity  extends AppCompatActivity {

    public static final String TAG = "QRShare";
    ImageView mQRImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.erweima_share);
        mQRImageView = (ImageView) findViewById(R.id.qr_img);

        JSONObject jsonObject = new JSONObject();

        if(getIntent()!=null){
            try {
                jsonObject.put("qr_content",getIntent().getStringExtra("qr_content"));
                jsonObject.put("bt_address",getIntent().getStringExtra("bt_address"));
                jsonObject.put("bt_name",getIntent().getStringExtra("bt_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG,"qrContent : " + jsonObject.toString());
        Bitmap qrCode = EncodingUtils.createQRCode(jsonObject.toString(), 500, 500,null);
        mQRImageView.setImageBitmap(qrCode);
    }

}
