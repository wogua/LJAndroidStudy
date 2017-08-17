package com.lijun.androidstudy.ebook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.util.FilesUtils;
import com.lijun.androidstudy.util.Utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class EbookReader extends Activity {
    /** Called when the activity is first created. */
    private PageWidget mPageWidget;
    Bitmap mCurPageBitmap, mNextPageBitmap;
    Canvas mCurPageCanvas, mNextPageCanvas;
    BookPageFactory pagefactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mPageWidget = new PageWidget(this);
        setContentView(mPageWidget);

        mCurPageBitmap = Bitmap.createBitmap(Utilities.screenWidth, Utilities.screenHeight, Bitmap.Config.ARGB_8888);
        mNextPageBitmap = Bitmap
                .createBitmap(Utilities.screenWidth, Utilities.screenHeight, Bitmap.Config.ARGB_8888);

        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);
        pagefactory = new BookPageFactory(Utilities.screenWidth, Utilities.screenHeight);

        pagefactory.setBgBitmap(Utilities.resizeBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.ebook_bg), Utilities.screenWidth,Utilities.screenHeight));

        try {
            File sdcardDir =Environment.getExternalStorageDirectory();

//            FilesUtils.AssetToSD(this,"/ebook_test.txt","/LJAndroidStudy/ebook_test.txt");
            String path = FilesUtils.getAssetsCacheFile(this,"ebook_test.txt");
            pagefactory.openbook(path);
            pagefactory.onDraw(mCurPageCanvas);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            Toast.makeText(this, "电子书不存在,请将《ebook_test.txt》放在assets目录下",
                    Toast.LENGTH_SHORT).show();
        }

        mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);

        mPageWidget.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                // TODO Auto-generated method stub

                boolean ret=false;
                if (v == mPageWidget) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        mPageWidget.abortAnimation();
                        mPageWidget.calcCornerXY(e.getX(), e.getY());

                        pagefactory.onDraw(mCurPageCanvas);
                        if (mPageWidget.DragToRight()) {
                            try {
                                pagefactory.prePage();
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            if(pagefactory.isfirstPage())return false;
                            pagefactory.onDraw(mNextPageCanvas);
                        } else {
                            try {
                                pagefactory.nextPage();
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            if(pagefactory.islastPage())return false;
                            pagefactory.onDraw(mNextPageCanvas);
                        }
                        mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                    }

                    ret = mPageWidget.doTouchEvent(e);
                    return ret;
                }
                return false;
            }

        });
    }
}