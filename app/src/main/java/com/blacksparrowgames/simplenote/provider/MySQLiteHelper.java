package com.blacksparrowgames.simplenote.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_TABLE = "simplenote";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_LAST_TIME = "lastTime";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_IMAGE_PATH = "imagePath";

    public static final String DATABASE_NAME = "simplenote.db";
    public static final int DATABASE_VERSION = 1;

    public static final String DEFAULT_SORT_ORDER = COLUMN_ID + " ASC";
    public static final String TITLES_SORT_ORDER = COLUMN_TITLE + " ASC";
    public static final String TIME_SORT_ORDER = COLUMN_LAST_TIME + " ASC";

    public static final String[] PROJECTION = {COLUMN_ID, COLUMN_LAST_TIME, COLUMN_TITLE, COLUMN_COMMENT, COLUMN_IMAGE_PATH};

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_LAST_TIME + " integer, " +
            COLUMN_TITLE + " text not null, " +
            COLUMN_COMMENT + " text, " +
            COLUMN_IMAGE_PATH + " text);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}
