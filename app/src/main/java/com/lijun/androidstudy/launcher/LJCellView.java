package com.lijun.androidstudy.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.lijun.androidstudy.util.PhotoUtils;

public class LJCellView extends TextView {

    Context mContext;
    LJCellInfo mCellInfo;
    Intent intent = null;

    public Intent getIntent() {
        return intent;
    }

    public LJCellInfo getmCellInfo() {
        return mCellInfo;
    }

    public void setmCellInfo(LJCellInfo mCellInfo) {
        this.mCellInfo = mCellInfo;
        setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mCellInfo.icon), null, null);
        setText(mCellInfo.name);
        intent = new Intent();
        intent.setComponent(new ComponentName(mCellInfo.packageName, mCellInfo.className));
    }

    public void setForegroundDrawable(Drawable drawable){
        if(drawable == null){
            setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mCellInfo.icon), null, null);
        }else {
            Bitmap compressBitmap = PhotoUtils.mergeLoadingBitmap(mCellInfo.icon);
            setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(compressBitmap), null, null);
        }
    }

    public LJCellView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LJCellView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public LJCellView(Context context) {
        super(context);
        init();
    }

    private void init() {
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
    }

    public String getTitle() {
        if (mCellInfo != null) {
            return mCellInfo.name;
        }
        return null;
    }
}
