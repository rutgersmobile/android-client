package edu.rutgers.css.Rutgers.items;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import edu.rutgers.css.Rutgers.api.Analytics;

/**
 * Created by jamchamb on 8/6/14.
 */
public class AnalyticsOpenHelper extends SQLiteOpenHelper {

    public static final String TYPE_FIELD = "TYPE";
    public static final String DATE_FIELD = "DATE";
    public static final String EXTRA_FIELD = "EXTRA";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AnalyticsDB";
    public static final String TABLE_NAME = "events";
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                TYPE_FIELD + " TEXT NOT NULL DEFAULT '" + Analytics.DEFAULT_TYPE + "'," +
                DATE_FIELD + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                EXTRA_FIELD + " TEXT" +
            ");";

    public AnalyticsOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
