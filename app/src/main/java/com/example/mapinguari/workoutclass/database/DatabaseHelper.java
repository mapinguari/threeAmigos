package com.example.mapinguari.workoutclass.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mapinguari on 7/24/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Workouts.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //TODO:EXCEPTION HANDLING
        db.execSQL(DatabaseSchema.CREATE_WORKOUT_TABLE);
        db.execSQL(DatabaseSchema.CREATE_INTERVAL_TABLE);
        db.execSQL(DatabaseSchema.CREATE_WORKOUTREL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.setVersion(newVersion);
    }
}
