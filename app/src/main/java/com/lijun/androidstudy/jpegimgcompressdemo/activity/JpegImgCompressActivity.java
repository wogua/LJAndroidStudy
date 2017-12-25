package com.lijun.androidstudy.jpegimgcompressdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.jpegimgcompressdemo.util.FileSizeUtil;
import com.lijun.androidstudy.jpegimgcompressdemo.util.MPermissionsUtils;
import com.lijun.androidstudy.jpegimgcompressdemo.util.camera.CameraCore;
import com.lijun.androidstudy.jpegimgcompressdemo.util.camera.CameraProxy;

import java.io.File;

import me.xiaosai.imagecompress.ImageCompress;


/**
 * @author XiaoSai
 * @version V1.0.0
 * @Description TODO
 * @Class JpegImgCompressActivity
 * @Copyright: Copyright (c) 2016
 */
public class JpegImgCompressActivity extends Activity implements CameraCore.CameraResult, View.OnClickListener {
    private Button choose_image, camera_image, switch_source, switch_target;
    private TextView sizeTextView;
    private CameraProxy cameraProxy;
    private ImageView choose_bit;
    private String sourceFilePath, targetFilePath;
    /**
     * SD卡根目录
     */
    private final String externalStorageDirectory = Environment.getExternalStorageDirectory().getPath() + "/picture/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jpegimgcompress);
        //压缩后保存临时文件目录
        File tempFile = new File(externalStorageDirectory);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        cameraProxy = new CameraProxy(this, JpegImgCompressActivity.this);
        choose_image = (Button) findViewById(R.id.choose_image);
        choose_image.setOnClickListener(this);
        choose_bit = (ImageView) findViewById(R.id.choose_bit);
        camera_image = (Button) findViewById(R.id.camera_image);
        camera_image.setOnClickListener(this);
        switch_source = (Button) findViewById(R.id.button_switch_source);
        switch_source.setOnClickListener(this);
        switch_target = (Button) findViewById(R.id.button_switch_target);
        switch_target.setOnClickListener(this);
        sizeTextView = (TextView) findViewById(R.id.img_size);
    }


    //拍照选图片成功回调
    @Override
    public void onCameraSuccess(final String filePath) {
        sourceFilePath = filePath;
        File file = new File(filePath);
        if (file.exists()) {
            ImageCompress.with(this)
                    .load(filePath)
                    .setTargetDir(externalStorageDirectory)
                    .ignoreBy(150)
                    .setOnCompressListener(new ImageCompress.OnCompressListener() {
                        @Override
                        public void onStart() {
                            Log.e("compress", "onStart : " + sourceFilePath);
                        }

                        @Override
                        public void onSuccess(String filePath) {
                            Log.e("compress", "onSuccess=" + filePath);
                            targetFilePath = filePath;
                            choose_bit.setImageBitmap(BitmapFactory.decodeFile(filePath));
                            showSize();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Log.e("compress", "onError");
                        }
                    }).launch();
        }
    }

    //拍照选图片失败回调
    @Override
    public void onCameraFail(String message) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MPermissionsUtils.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        cameraProxy.onResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cameraProxy.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        cameraProxy.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.choose_image) {
            cameraProxy.getPhoto2Album();
        } else if (id == R.id.camera_image) {
            String cameraPath = externalStorageDirectory + System.currentTimeMillis() / 1000 + ".jpg";
            cameraProxy.getPhoto2Camera(cameraPath);
        } else if (id == R.id.button_switch_source) {
            if (isExisttPath(sourceFilePath)) {
                choose_bit.setImageBitmap(BitmapFactory.decodeFile(sourceFilePath));
            }
        } else if (id == R.id.button_switch_target) {
            if (isExisttPath(targetFilePath)) {
                choose_bit.setImageBitmap(BitmapFactory.decodeFile(targetFilePath));
            }
        }
    }

    private boolean isExisttPath(String path) {
        if (path == null || path.equals("")) return false;
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    private void showSize(){
        String sizeText = "";
        if (isExisttPath(sourceFilePath)) {
            sizeText = "压缩前大小:"+FileSizeUtil.getFileOrFilesSize(sourceFilePath,FileSizeUtil.SIZETYPE_KB)+"KB";
        }
        if (isExisttPath(targetFilePath)) {
            sizeText =sizeText+ "\n压缩后大小:"+FileSizeUtil.getFileOrFilesSize(targetFilePath,FileSizeUtil.SIZETYPE_KB)+"KB";
        }
        sizeTextView.setText(sizeText);
    }
}
  
