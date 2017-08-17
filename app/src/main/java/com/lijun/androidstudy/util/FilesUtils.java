package com.lijun.androidstudy.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lijun on 17-8-17.
 */

public class FilesUtils {

    /**
     * 复制asset文件到指定目录
     */
    public static void AssetToSD(Context context, String assetpath, String SDpath) {
        String sdcardPath = Environment.getExternalStorageDirectory().toString() + SDpath;
        AssetManager asset = context.getAssets();
        //循环的读取asset下的文件，并且写入到SD卡
        String[] filenames = null;
        FileOutputStream out = null;
        InputStream in = null;
        try {
            createFileDirOrNot(sdcardPath);
            filenames = asset.list(assetpath);
            if (filenames.length > 0) {//说明是目录
                //创建目录
                getDirectory(assetpath);
                for (String fileName : filenames) {
                    AssetToSD(context, assetpath + "/" + fileName, sdcardPath + "/" + fileName);
                }
            } else {//说明是文件，直接复制到SD卡
                File SDFlie = new File(sdcardPath);
                String path = assetpath.substring(0, assetpath.lastIndexOf("/"));
                getDirectory(path);

                if (!SDFlie.exists()) {
                    SDFlie.createNewFile();
                }
                //将内容写入到文件中
                in = asset.open(assetpath);
                out = new FileOutputStream(SDFlie);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = in.read(buffer)) != -1) {
                    out.write(buffer, 0, byteCount);
                }
                out.flush();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    //分级建立文件夹
    public static void getDirectory(String path) {
        //对SDpath进行处理，分层级建立文件夹
        String[] s = path.split("/");
        String str = Environment.getExternalStorageDirectory().toString();
        for (int i = 0; i < s.length; i++) {
            str = str + "/" + s[i];
            File file = new File(str);
            if (!file.exists()) {
                file.mkdir();
            }
        }

    }

    //创建文件,包括其目录
    public static boolean createFileDirOrNot(String fileName){
        try {
            File rootDir = new File(fileName);
            if (!rootDir.exists() || !rootDir.isDirectory()) {
                boolean create = rootDir.mkdirs();
                rootDir.setExecutable(true);//设置可执行权限
                rootDir.setReadable(true);//设置可读权限
                rootDir.setWritable(true);//设置可写权限
            }
            File file = new File(fileName);
            file.createNewFile();
            file.setExecutable(true);//设置可执行权限
            file.setReadable(true);//设置可读权限
            file.setWritable(true);//设置可写权限
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取assets 目录文件的绝对路径
     *
     * @param context
     * @param fileName
     * @return
     */
    public static String getAssetsCacheFile(Context context, String fileName) {
        File cacheFile = new File(context.getCacheDir(), fileName);
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cacheFile.getAbsolutePath();
    }
}
