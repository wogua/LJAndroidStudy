package com.lijun.androidstudy.floatmeltitask;

import com.lijun.androidstudy.R;

import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * filename:NoteWindowView.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-8
 * author: laiyang
 * <p>
 * extends to LinearLayout,show note float window
 * <p>
 * Modification History
 * -----------------------------------
 * <p>
 * -----------------------------------
 */
public class NoteWindowView extends LinearLayout implements View.OnClickListener,
        AdapterView.OnItemClickListener, View.OnLongClickListener, View.OnTouchListener {

    private WindowManager.LayoutParams mParams;
    private ImageButton mivBack;
    private ImageButton mivMinimize;
    private RelativeLayout mrlNoteWholeView;
    /**
     * content layout
     */
    private RelativeLayout mrlContentBar;
    /**
     * edit content layout
     */
    private RelativeLayout mrlContent;
    /**
     * control layout(save button, cancle button)
     */
    private RelativeLayout mrlEditControlBar;
    /**
     * bottom layout（delete button, new button�?
     */
    private RelativeLayout mrlBottomBar;
    private RelativeLayout mrlTitle;
    private TextView mtvTitle;
    private TextView mtvDateTime;
    private EditText metNoteContent;
    private Gallery gallery;
    private Button mbtnCancel;
    private Button mbtnSave;
    private ImageButton mibDel;
    private ImageButton mibNew;
    /**
     * empty note layout
     */
    private RelativeLayout mrlEmpty;
    private boolean isPlaying;
    private Context mContext;
    private NoteDatabaseHelper mdbHelper;
    private List<NoteContent> mNotes;
    private GalleryAdapter mAdapter;
    private static String timeFormatPattern;
    private boolean isNewNote = true;
    private WindowManager windowManager;
    // status bar height
    private static int statusBarHeight;

    private static final String TAG = "NoteWindowView";

    private float touchX;

    private float touchY;

    private float xInScreen;

    private float yInScreen;

    public NoteWindowView(Context context) {
        super(context);
        mContext = context;
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        // inflate layout xml
        LayoutInflater.from(context).inflate(R.layout.note_window, this);
        // init views
        mrlNoteWholeView = (RelativeLayout) findViewById(R.id.noteWholeView);
        mrlEditControlBar = (RelativeLayout) findViewById(R.id.notes_pop_layout2);
        mrlEmpty = (RelativeLayout) findViewById(R.id.layout_empty);
        mrlContent = (RelativeLayout) findViewById(R.id.layout_content);
        mrlBottomBar = (RelativeLayout) findViewById(R.id.rl_bottom_bar);
        mrlTitle = (RelativeLayout) findViewById(R.id.rl_title_bar);
        metNoteContent = (EditText) findViewById(R.id.notes_content);
        mtvDateTime = (TextView) findViewById(R.id.date_time);
        mtvTitle = (TextView) findViewById(R.id.note_title);
        mivBack = (ImageButton) findViewById(R.id.iv_back);
        mivMinimize = (ImageButton) findViewById(R.id.iv_minimize);
        gallery = (Gallery) findViewById(R.id.gallery);
        mbtnCancel = (Button) findViewById(R.id.cancel_btn);
        mbtnSave = (Button) findViewById(R.id.save_btn);
        mibDel = (ImageButton) findViewById(R.id.del_btn);
        mibNew = (ImageButton) findViewById(R.id.new_btn);
        mdbHelper = new NoteDatabaseHelper(context);
        // set views click listener and touch listener
        mivBack.setOnClickListener(this);
        mivMinimize.setOnClickListener(this);
        mibNew.setOnClickListener(this);
        mibDel.setOnClickListener(this);
        mbtnCancel.setOnClickListener(this);
        mbtnSave.setOnClickListener(this);
        mrlTitle.setOnLongClickListener(this);
        mrlTitle.setOnTouchListener(this);
        // init time format pattern
        timeFormatPattern = "yyyy" + mContext.getString(R.string.note_time_year) +
                "MM" + mContext.getString(R.string.note_time_month)
                + "dd" + mContext.getString(R.string.note_time_day);
        mNotes = new ArrayList<NoteContent>();
        // read notes
        updateNotes();
        if (mNotes.size() == 0) {
            mrlEmpty.setVisibility(View.VISIBLE);
        } else {
            mrlEmpty.setVisibility(View.GONE);
        }
        mAdapter = new GalleryAdapter();
        gallery.setOnItemClickListener(this);
        gallery.setAdapter(mAdapter);
        // play animation
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_in);
        mrlNoteWholeView.setAnimation(anim);
    }

    /**
     * read db get notes -- query
     */
    private void updateNotes() {
        mNotes.clear();
        SQLiteDatabase db = mdbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from Notes", null);
        NoteContent note = null;
        while (cursor.moveToNext()) {
            note = new NoteContent();
            note.setId(cursor.getInt(0));
            note.setContent(cursor.getString(1));
            note.setTime(cursor.getString(2));
            mNotes.add(note);
        }
        cursor.close();
        db.close();
    }

    /**
     * insert one note
     *
     * @param note
     */
    private void insertNote(NoteContent note) {
        SQLiteDatabase db = mdbHelper.getReadableDatabase();
        db.execSQL("insert into Notes values(?,?,?)", new String[]{null, note.getContent(), note.getTime()});
        db.close();
    }

    /**
     * update note
     *
     * @param note
     */
    private void updateNote(NoteContent note) {
        SQLiteDatabase db = mdbHelper.getReadableDatabase();
        db.execSQL("update Notes set content=?,time=? where id=?", new String[]{note.getContent(), note.getTime(), note.getId() + ""});
        db.close();
    }

    /**
     * delete note
     *
     * @param id
     */
    private void deleteNote(int id) {
        SQLiteDatabase db = mdbHelper.getReadableDatabase();
        db.execSQL("delete from Notes where id=?", new String[]{"" + id});
        db.close();
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mivBack)) {// to main window
            closeSoftInput(mContext, metNoteContent);
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mrlNoteWholeView.setVisibility(View.GONE);
                    MultiTaskManager.removeNoteWindow(getContext());
                    MultiTaskManager.createMainWindow(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mrlNoteWholeView.startAnimation(anim);
        } else if (v.equals(mivMinimize)) {// to float button
            closeSoftInput(mContext, metNoteContent);
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mrlNoteWholeView.setVisibility(View.GONE);
                    MultiTaskManager.removeNoteWindow(getContext());
                    MultiTaskManager.createFLoatButton(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mrlNoteWholeView.startAnimation(anim);

        } else if (v.equals(mbtnSave)) {// save note
            closeSoftInput(mContext, metNoteContent);

            if (!(metNoteContent.getText() == null || "".equals(metNoteContent.getText() + ""))) {
                NoteContent note = null;
                if (isNewNote) {
                    note = new NoteContent(metNoteContent.getText() + "", System.currentTimeMillis() + "");
                    insertNote(note);
                } else {
                    note = new NoteContent((Integer) metNoteContent.getTag(), metNoteContent.getText() + "", System.currentTimeMillis() + "");
                    updateNote(note);
                }
            }
            mrlBottomBar.setVisibility(View.VISIBLE);
            mrlEditControlBar.setVisibility(View.GONE);
            mrlContent.setVisibility(View.GONE);
            mtvTitle.setText(mContext.getString(R.string.note_window_title));
            // save success and update UI
            updateNotes();
            if (0 == mNotes.size()) {
                mrlEmpty.setVisibility(View.VISIBLE);
            } else {
                gallery.setVisibility(View.VISIBLE);
            }
            mAdapter.notifyDataSetChanged();

        } else if (v.equals(mbtnCancel)) {// cancel to edit note,update UI
            closeSoftInput(mContext, metNoteContent);
            mrlBottomBar.setVisibility(View.VISIBLE);
            mrlEditControlBar.setVisibility(View.GONE);
            mrlContent.setVisibility(View.GONE);
            mtvTitle.setText(mContext.getString(R.string.note_window_title));
            updateNotes();
            if (0 == mNotes.size()) {
                mrlEmpty.setVisibility(View.VISIBLE);
            } else {
                gallery.setVisibility(View.VISIBLE);
            }
            mAdapter.notifyDataSetChanged();
        } else if (v.equals(mibNew)) {// to new note layout
            isNewNote = true;
            mrlBottomBar.setVisibility(View.GONE);
            mrlContent.setVisibility(View.VISIBLE);
            mrlEditControlBar.setVisibility(View.VISIBLE);
            metNoteContent.setText("");
            if (0 == mNotes.size()) {
                mrlEmpty.setVisibility(View.GONE);
            } else {
                gallery.setVisibility(View.GONE);
            }
            String time = TimFormatUtils.getTimeinMills(timeFormatPattern, System.currentTimeMillis());
            mtvDateTime.setText(time);
            mtvTitle.setText(mContext.getString(R.string.note_window_title_new));
        } else if (v.equals(mibDel)) {// delete current show note
            if (mNotes.size() == 0) {
                return;
            }
            deleteNote(mNotes.get(gallery.getFirstVisiblePosition()).getId());
            updateNotes();
            if (0 == mNotes.size()) {
                mrlEmpty.setVisibility(View.VISIBLE);
            }
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {// click to edite current note
        isNewNote = false;
        mrlBottomBar.setVisibility(View.GONE);
        mrlContent.setVisibility(View.VISIBLE);
        mrlEditControlBar.setVisibility(View.VISIBLE);
        gallery.setVisibility(View.GONE);
        String time = TimFormatUtils.getTimeinMills(timeFormatPattern, Long.parseLong(mNotes.get(position).getTime()));
        mtvDateTime.setText(time);
        metNoteContent.setText(mNotes.get(position).getContent());
        mtvTitle.setText(mContext.getString(R.string.note_window_title_update));
        metNoteContent.setTag(mNotes.get(position).getId());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (isLongClick) {
            touchX = event.getX();
            touchY = event.getY();
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "action down");
                touchX = event.getX();
                touchY = event.getY();

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "preX:" + mParams.x);
                Log.d(TAG, "preY:" + mParams.y);
                Log.d(TAG, "nowY:" + event.getRawX());
                Log.d(TAG, "nowY:" + event.getRawY());
                Log.d(TAG, "downX:" + touchX);
                Log.d(TAG, "downY:" + touchY);

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();

                mParams.x = (int) (xInScreen - touchX);
                mParams.y = (int) (yInScreen - touchY);
                windowManager.updateViewLayout(this, mParams);
                Log.i(TAG, "action_move");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "action up");
                isLongClick = true;
                mrlTitle.setOnLongClickListener(this);
                break;
        }
        return false;
    }

    /**
     * get status bar height
     *
     * @return
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    /**
     * if true,set long click listener available
     */
    private boolean isLongClick = true;

    @Override
    public boolean onLongClick(View v) {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0, 50}, -1);
        mrlTitle.setOnLongClickListener(null);
        isLongClick = false;
        return false;
    }

    private class GalleryAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public GalleryAdapter() {
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mNotes.size();
        }

        @Override
        public Object getItem(int position) {
            return mNotes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NoteContent note = mNotes.get(position);
            Cache cache = null;
            if (convertView == null) {
                cache = new Cache();
                convertView = inflater.inflate(R.layout.notes_popup_item, null);
                cache.tvContent = (TextView) convertView.findViewById(R.id.notes_content);
                cache.tvPageNum = (TextView) convertView.findViewById(R.id.page_num);
                cache.tvDateTime = (TextView) convertView.findViewById(R.id.date_time);
                convertView.setTag(cache);
            } else {
                cache = (Cache) convertView.getTag();
            }
            cache.tvPageNum.setText(position + 1 + "/" + mNotes.size());
            cache.tvContent.setText(note.getContent());
            cache.tvDateTime.setText(TimFormatUtils.getTimeinMills(timeFormatPattern
                    , Long.parseLong(note.getTime())));
            return convertView;
        }

        private class Cache {
            TextView tvPageNum;
            TextView tvDateTime;
            TextView tvContent;
        }
    }

    /**
     * close soft input
     */
    private void closeSoftInput(Context context, EditText et) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromInputMethod(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
