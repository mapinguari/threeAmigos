package com.example.mapinguari.workoutclass;

import android.content.ContentValues;
import android.provider.BaseColumns;

import java.util.GregorianCalendar;

/**
 * Created by mapinguari on 7/24/15.
 */
public class DatabaseSchema {
    
    public DatabaseSchema(){}

    public static class DataBaseTerms implements BaseColumns {
        //Table names
        //private static final String USER_TABLE_NAME = "users";
        public static final String INTERVAL_TABLE_NAME = "intervals";
        public static final String WORKOUT_RELATIONS_TABLE_NAME = "workOutRelations";
        public static final String WORKOUTS_TABLE_NAME = "workouts";
        //Column names
        private static final String COLUMN_NAME_DISTANCE = "distance";
        private static final String COLUMN_NAME_TIME = "time";
        private static final String COLUMN_NAME_RESTTIME = "restTime";
        private static final String COLUMN_NAME_AVERAGE_WATTS = "averageWatts";
        private static final String COLUMN_NAME_AVERAGE_SPM = "averageSPM";
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
    
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String REAL_TYPE = " REAL";
    public static final String DATE_TIME = " DATETIME";
    public static final String NOT_NULL = " NOT NULL";
    public static final String COMMA_SEP = " ,";
    public static final String OPEN_PAREN = " (";
    public static final String CLOSE_PAREN = ") ";

    private static final String buildNNColumn(String title, String type){
        return (title + type + NOT_NULL + COMMA_SEP);
    }

    private static final String idCol(){
        return (DataBaseTerms._ID + INTEGER_TYPE + NOT_NULL + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP);
    }

    private static final String foreignKey(String col, String table, String ref){
        return "FOREIGN KEY (" + col + ") REFERENCES " + table + " (" + ref + ") ";
    }
    
    public static final String CREATE_INTERVAL_TABLE =
            "CREATE TABLE" + DataBaseTerms.INTERVAL_TABLE_NAME + OPEN_PAREN +
            DataBaseTerms._ID + INTEGER_TYPE + NOT_NULL + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
            DataBaseTerms.COLUMN_NAME_AVERAGE_WATTS + REAL_TYPE + NOT_NULL + COMMA_SEP +
            DataBaseTerms.COLUMN_NAME_TIME + REAL_TYPE + NOT_NULL + COMMA_SEP +
            DataBaseTerms.COLUMN_NAME_AVERAGE_SPM + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
            DataBaseTerms.COLUMN_NAME_RESTTIME + REAL_TYPE + NOT_NULL +
            " UNIQUE (" +
                    DataBaseTerms.COLUMN_NAME_AVERAGE_WATTS +
                    DataBaseTerms.COLUMN_NAME_TIME +
                    DataBaseTerms.COLUMN_NAME_AVERAGE_SPM +
                    DataBaseTerms.COLUMN_NAME_RESTTIME + CLOSE_PAREN + CLOSE_PAREN;

    public static final String CREATE_WORKOUTREL_TABLE =
                    "CREATE TABLE" + DataBaseTerms.WORKOUT_RELATIONS_TABLE_NAME + OPEN_PAREN +
                    idCol() +
                    buildNNColumn(DataBaseTerms.COLUMN_NAME_WORKOUT_ID,INTEGER_TYPE) +
                    buildNNColumn(DataBaseTerms.COLUMN_NAME_INTERVAL_ORDINAL, INTEGER_TYPE) +
                    buildNNColumn(DataBaseTerms.COLUMN_NAME_INTERVAL_ID, INTEGER_TYPE) +
                    foreignKey(DataBaseTerms.COLUMN_NAME_INTERVAL_ID, DataBaseTerms.INTERVAL_TABLE_NAME,DataBaseTerms._ID) + COMMA_SEP +
                    foreignKey(DataBaseTerms.COLUMN_NAME_WORKOUT_ID,DataBaseTerms.WORKOUTS_TABLE_NAME,DataBaseTerms._ID) + CLOSE_PAREN;

    public static final String CREATE_WORKOUT_TABLE =
            "CREATE TABLE" + DataBaseTerms.WORKOUTS_TABLE_NAME + OPEN_PAREN +
            idCol() + DataBaseTerms.COLUMN_NAME_COMPLETED_TIME + DATE_TIME + NOT_NULL + CLOSE_PAREN;

    public static ContentValues intervalContent(Interval interval){
        Double watts = interval.getAverageWatts();
        Double time = interval.getWorkTime();
        Double restTime = interval.getRestTime();
        Integer spm = interval.getAverageSPM();
        ContentValues cv = new ContentValues(4);
        cv.put(DataBaseTerms.COLUMN_NAME_AVERAGE_WATTS,watts);
        cv.put(DataBaseTerms.COLUMN_NAME_TIME,time);
        cv.put(DataBaseTerms.COLUMN_NAME_RESTTIME, restTime);
        cv.put(DataBaseTerms.COLUMN_NAME_AVERAGE_SPM, spm);
        return cv;
    }

    public static ContentValues workoutContent(Workout workout){
        GregorianCalendar cal = workout.getWorkoutTime();
        ContentValues cv = new ContentValues(1);
        cv.put(DataBaseTerms.COLUMN_NAME_COMPLETED_TIME,GregtoString.getDateTime(workout.getWorkoutTime()));
        return cv;
    }

    public static final ContentValues workOutRelContent(int workout_ID,int intervalOrdinal, int interval_ID){
        ContentValues cv = new ContentValues(3);
        cv.put(DataBaseTerms.COLUMN_NAME_WORKOUT_ID,workout_ID);
        cv.put(DataBaseTerms.COLUMN_NAME_INTERVAL_ORDINAL,intervalOrdinal);
        cv.put(DataBaseTerms.COLUMN_NAME_INTERVAL_ID,interval_ID);
        return cv;
    }

    public static final String QUERY_SINGLE_ID_DATE = "SELECT " + DataBaseTerms.COLUMN_NAME_COMPLETED_TIME + " FROM" + DataBaseTerms.WORKOUTS_TABLE_NAME +
            " WHERE" + DataBaseTerms._ID + " = '?' ";


    public static final String QUERY_WORKOUT_ID_INTERVALS = "SELECT" +
            " r." + DataBaseTerms.COLUMN_NAME_INTERVAL_ORDINAL + COMMA_SEP +
            " i." + DataBaseTerms.COLUMN_NAME_AVERAGE_SPM + COMMA_SEP +
            " i." + DataBaseTerms.COLUMN_NAME_TIME + COMMA_SEP +
            " i." + DataBaseTerms.COLUMN_NAME_AVERAGE_WATTS + COMMA_SEP +
            " i." + DataBaseTerms.COLUMN_NAME_RESTTIME +
            " FROM" + DataBaseTerms.WORKOUT_RELATIONS_TABLE_NAME + " AS r" +
            " INNER JOIN" + DataBaseTerms.INTERVAL_TABLE_NAME + " AS i" +
            " ON" + " r." + DataBaseTerms.COLUMN_NAME_INTERVAL_ID + "=" + "i." + DataBaseTerms._ID +
            " WHERE " + " r." + DataBaseTerms.COLUMN_NAME_WORKOUT_ID + "='?' ";

    public static final String DELETE_WORKOUT_ROW_WHERE_CLAUSE = DataBaseTerms._ID + "='?'";
    public static final String DELETE_WORKOUT_REL_ROWS_WHERE_CLAUSE = DataBaseTerms.COLUMN_NAME_WORKOUT_ID + "='?'";


}



