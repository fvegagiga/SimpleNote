package com.blacksparrowgames.simplenote.provider;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class NoteContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.blacksparrowgames.simplenote";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/simplenoteitems");

    public static final String COLUMN_ID = MySQLiteHelper.COLUMN_ID;
    public static final String COLUMN_TITLE = MySQLiteHelper.COLUMN_TITLE;
    public static final String COLUMN_LAST_TIME = MySQLiteHelper.COLUMN_LAST_TIME;
    public static final String COLUMN_COMMENT = MySQLiteHelper.COLUMN_COMMENT;
    public static final String COLUMN_IMAGE_PATH = MySQLiteHelper.COLUMN_IMAGE_PATH;

    public static final String[] PROJECTION = MySQLiteHelper.PROJECTION;
    public static final String DEFAULT_SORT_ORDER = MySQLiteHelper.DEFAULT_SORT_ORDER;
    public static final String TITLES_SORT_ORDER = MySQLiteHelper.TITLES_SORT_ORDER;
    public static final String TIME_SORT_ORDER = MySQLiteHelper.TIME_SORT_ORDER;

    private MySQLiteHelper myOpenHelper;

    private static final int ALLROWS = 1;
    private static final int SINGLE_ROW = 2;

    private static final UriMatcher uriMatcher;



    @Override
    public boolean onCreate() {

        myOpenHelper = new MySQLiteHelper(getContext());
        return true;
    }

    // Populate the UriMatcher object, where a URI ending in 'simplenoteitems' will
    // correspond to a request for all items, and 'simplenoteitems/[rowID]'
    // represents a single row.
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "simplenoteitems",   ALLROWS);
        uriMatcher.addURI(AUTHORITY, "simplenoteitems/#", SINGLE_ROW);
    }


    @Override
    public String getType(Uri uri) {
        // Return a string that identifies the MIME typefor a Content Provider URI
        switch (uriMatcher.match(uri)) {
            case ALLROWS:// directory location
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.blacksparrowgames.note";
            case SINGLE_ROW: // item location
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.blacksparrowgames.note";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = myOpenHelper.getWritableDatabase();

        // Replace these with valid SQL statements if necessary.
        String groupBy = null;
        String having = null;

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MySQLiteHelper.DATABASE_TABLE);

        // If this is a row query, limit the result set to the passed in row.
        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW:
                String rowID = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(COLUMN_ID + "=" + rowID);
            default:
                break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, groupBy, having, sortOrder);

        return cursor;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Open a read / write database to support the transaction.
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();

        // If this is a row URI, limit the deletion to the specified row.
        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW:
                String rowID = uri.getPathSegments().get(1);
                selection = COLUMN_ID + "=" + rowID
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
            default:
                break;
        }

        // To return the number of deleted items, you must specify a where
        // clause. To delete all rows and return a value, pass in "1".
        if (selection == null)
            selection = "1";

        // Execute the deletion.
        int deleteCount = db.delete(MySQLiteHelper.DATABASE_TABLE, selection,
                selectionArgs);

        // Notify any observers of the change in the data set.
        getContext().getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Open a read / write database to support the transaction.
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        String nullColumnHack = null;
        values.put(COLUMN_LAST_TIME, System.currentTimeMillis());

        // Insert the values into the table
        long id = db.insert(MySQLiteHelper.DATABASE_TABLE, nullColumnHack,values);

        if (id > -1) {
            // Construct and return the URI of the newly inserted row.
            Uri insertedId = ContentUris.withAppendedId(CONTENT_URI, id);

            // Notify any observers of the change in the data set.
            getContext().getContentResolver().notifyChange(insertedId, null);

            return insertedId;
        }

        return null;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        // Open a read / write database to support the transaction.
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW:
                String rowID = uri.getPathSegments().get(1);
                selection = COLUMN_ID + "=" + rowID
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
            default:
                break;
        }

        values.put(COLUMN_LAST_TIME, System.currentTimeMillis());
        // Perform the update.
        int updateCount = db.update(MySQLiteHelper.DATABASE_TABLE, values,
                selection, selectionArgs);

        // Notify any observers of the change in the data set.
        getContext().getContentResolver().notifyChange(uri, null);

        return updateCount;
    }

}
