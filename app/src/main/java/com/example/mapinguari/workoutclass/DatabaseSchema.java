package com.example.mapinguari.workoutclass;

import android.provider.BaseColumns;

/**
 * Created by mapinguari on 7/24/15.
 */
public class DatabaseSchema {

    public static class DataBaseTerms implements BaseColumns {
        //Table names
        //private static final String USER_TABLE_NAME = "users";
        private static final String INTERVAL_TABLE_NAME = "intervals";
        private static final String WORKOUT_RELATIONS_TABLE_NAME = "workOutRelations";
        private static final String WORKOUTS_TABLE_NAME = "workouts";
        //Column names
        private static final String COLUMN_NAME_DISTANCE = "distance";
        private static final String COLUMN_NAME_TIME = "time";
        private static final String COLUMN_NAME_RESTTIME = "restTime";
        //private static final String COLUMN_NAME_NAME = "name";
        //private static final String COLUMN_NAME_WEIGHT = "weight";
        //private static final String COLUMN_NAME_HEIGHT = "height";
        //private static final String COLUMN_NAME_AGE = "age";
        private static final String COLUMN_NAME_WORKOUT_ID = "workout_ID";
        private static final String COLUMN_NAME_INTERVAL_ORDINAL = "IntervalOrdinal";
        private static final String COLUMN_NAME_INTERVAL_ID = "interval_ID";
        private static final String COLUMN_NAME_COMPLETED_TIME = "completed";
        //private static final String COLUMN_NAME_USER_ID = "user_ID";

    }

    public static final String CREATE_INTERVAL_TABLE = "CREATE TABLE Interval (_ID integer NOT NULL PRIMARY KEY AUTOINCREMENT, averageWatts double NOT NULL,time double NOT NULL, averageSPM integer NOT NULL,restTime double NOT NULL)";
    /*        "CREATE TABLE " + DataBaseTerms.USER_TABLE_NAME +
            + " (" + DataBaseTerms._ID +
    */
    //public static final String CREATE_USER_TABLE = "CREATE TABLE User (_ID integer NOT NULL PRIMARY KEY AUTOINCREMENT,name varchar(20) NOT NULL,weight double NOT NULL,height double NOT NULL,age integer NOT NULL)";
    public static final String CREATE_WORKOUTREL_TABLE = "CREATE TABLE WorkOutRel (_ID integer NOT NULL  PRIMARY KEY AUTOINCREMENT,workout_ID integer NOT NULL,intervalOrdinal integer NOT NULL,interval_ID integer NOT NULL,FOREIGN KEY (Interval_ID) REFERENCES Interval (_ID),FOREIGN KEY (Workout_ID) REFERENCES Workout (_ID))";
    public static final String CREATE_WORKOUT_TABLE = "CREATE TABLE Workout (_ID integer NOT NULL  PRIMARY KEY AUTOINCREMENT,completed datetime NOT NULL, user_ID integer NOT NULL,FOREIGN KEY (User_ID) REFERENCES User (_ID))";

}
