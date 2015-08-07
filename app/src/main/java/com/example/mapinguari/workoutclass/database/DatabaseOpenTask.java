package com.example.mapinguari.workoutclass.database;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.example.mapinguari.workoutclass.database.DatabaseHelper;

/**
 * Created by mapinguari on 7/29/15.
 */
public class DatabaseOpenTask extends AsyncTask<DatabaseHelper,Integer,SQLiteDatabase> {

    @Override
    protected SQLiteDatabase doInBackground(DatabaseHelper... params) {
        DatabaseHelper dbH = params[0];
        return dbH.getWritableDatabase();
    }

}
