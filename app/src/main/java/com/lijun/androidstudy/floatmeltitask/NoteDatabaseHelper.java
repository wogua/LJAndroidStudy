package com.lijun.androidstudy.floatmeltitask;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * filename:NoteDatabaseHelper.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-20
 * author: laiyang
 * <p>
 * extends to SQLiteOpenHelper,to create db and upgreade db
 * <p>
 * Modification History
 * -----------------------------------
 * <p>
 * -----------------------------------
 */
public class NoteDatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    /**
     * db file name
     */
    private static final String DB_NAME = "MultiTask.db";

    public NoteDatabaseHelper(Context context) {
        this(context, DB_NAME);
    }

    public NoteDatabaseHelper(Context context, String name) {
        this(context, name, VERSION);
    }

    public NoteDatabaseHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public NoteDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Notes (id integer PRIMARY KEY, content, time)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table Notes");
        db.execSQL("create table Notes (id PRIMARY KEY, content, time)");
    }

}
