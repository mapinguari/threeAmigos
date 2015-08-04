package com.example.mapinguari.workoutclass.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.mapinguari.workoutclass.GregtoString;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by mapinguari on 7/24/15.
 */
public class DatabaseInterface {

    public SQLiteDatabase workoutDatabase;

    public DatabaseInterface(SQLiteDatabase workoutDatabase) {
        this.workoutDatabase = workoutDatabase;
    }

    public Boolean clearData(){
        dropTables();
        makeTables();
        return true;
    }

    private Boolean makeTables(){
        workoutDatabase.execSQL(DatabaseSchema.CREATE_WORKOUT_TABLE);
        workoutDatabase.execSQL(DatabaseSchema.CREATE_INTERVAL_TABLE);
        workoutDatabase.execSQL(DatabaseSchema.CREATE_WORKOUTREL_TABLE);
        return true;
    }

    private Boolean dropTables(){
        workoutDatabase.execSQL(DatabaseSchema.DROP_REL_TABLE);
        workoutDatabase.execSQL(DatabaseSchema.DROP_INTERVALS_TABLE);
        workoutDatabase.execSQL(DatabaseSchema.DROP_WORKOUT_TABLE);
        return true;
    }

    public Boolean insertWorkout(Workout workout) {
        //TODO:HANDLE FAILED INSERTS
        //For efficient updates the transaction will be
        //transaction start
        //int row = insertWithConflict() -- row will be with 0 on the end CONFLICT_IGNORE algo
        //this will need to be a rawSQL exceution update (noOFREF = noOFREF + 1)
        //end transaction
        int workout_ID = (int) workoutDatabase.insert(DatabaseSchema.DataBaseTerms.WORKOUTS_TABLE_NAME, null, DatabaseSchema.workoutContent(workout));
        boolean workoutInserted = workout_ID >= 0;
        int interval_ID;
        int ordin = 0;
        ContentValues cv;
        boolean intervalsInserted = true;
        for (Interval i : workout.getIntervalList()) {
            interval_ID = (int) insertInterval(i);
            intervalsInserted = interval_ID >= 0 && intervalsInserted;
            cv = DatabaseSchema.workOutRelContent(workout_ID, ordin, interval_ID);
            workoutDatabase.insert(DatabaseSchema.DataBaseTerms.WORKOUT_RELATIONS_TABLE_NAME, null, cv);
            ordin++;
        }
        return intervalsInserted && workoutInserted;
    }

    private long insertInterval(Interval interval) {
        //TODO: This will need to be re-written to handle deletes efficiently
        return (workoutDatabase.insertWithOnConflict(DatabaseSchema.DataBaseTerms.INTERVAL_TABLE_NAME, null, DatabaseSchema.intervalContent(interval),SQLiteDatabase.CONFLICT_IGNORE));
    }

    //V0.1
    public int deleteWorkout(Integer Workout_ID) {
        String[] args = {Workout_ID.toString()};
        int nor = workoutDatabase.delete(DatabaseSchema.DataBaseTerms.WORKOUTS_TABLE_NAME, DatabaseSchema.DELETE_WORKOUT_ROW_WHERE_CLAUSE,args);
        if(nor == 0){
            throw new SQLiteException("No workout with specified ID");
        }
        nor = workoutDatabase.delete(DatabaseSchema.DataBaseTerms.WORKOUTS_TABLE_NAME, DatabaseSchema.DELETE_WORKOUT_REL_ROWS_WHERE_CLAUSE,args);
        if(nor == 0){
            throw new SQLiteException("No workout rels with specified ID");
        }
        return nor;
    }
                //TODO: CODE DELETEWORKOUT
                //TODO: This is a nightmare. To do this I am going to have to add a column to the intervals table which tracks the number of references to the row. As far as I can tell this can not be carried out with a paradigmatic call to update and will require execsql() FUCK

    private GregorianCalendar getWorkoutDate(Integer workout_ID) {
        //Change this to use query()
        String[] args = {workout_ID.toString()};

        Cursor firstC = workoutDatabase.rawQuery(DatabaseSchema.QUERY_SINGLE_ID_DATE, args);
        if (firstC.getCount() == 0) {
            throw new SQLiteException("No workout with that ID");
        }
        firstC.moveToFirst();
        String datetime = firstC.getString(firstC.getColumnIndex(DatabaseSchema.DataBaseTerms.getColumnNameCompletedTime()));
        return GregtoString.getGregCal(datetime);
    }

    private List<Interval> getIntervals(Integer workout_ID) {
        String[] args = {workout_ID.toString()};
        Cursor firstC = workoutDatabase.rawQuery(DatabaseSchema.QUERY_WORKOUT_ID_INTERVALS, args);
        int nOI = firstC.getCount();
        if (nOI == 0) {
            throw new SQLiteException("No rels with that ID");
        }
        int ordinal;
        int SPM;
        double time;
        double watts;
        double resttime;
        Interval curInt;
        Interval[] intervals = new Interval[nOI];
        firstC.moveToFirst();
        for(int i = 0;i < nOI;i++){
            ordinal = firstC.getInt(0);
            SPM = firstC.getInt(1);
            time = firstC.getDouble(2);
            watts = firstC.getDouble(3);
            resttime = firstC.getDouble(4);
            curInt = new Interval(time, watts, SPM, resttime);
            intervals[ordinal] = curInt;
            firstC.moveToNext();
        }
        List<Interval> intervalsList = Arrays.asList(intervals);
        return intervalsList;

    }

    public Workout getWorkOut(Integer workout_ID){
        GregorianCalendar date = getWorkoutDate(workout_ID);
        List<Interval> intervals = getIntervals(workout_ID);

        Double watts = Workout.averageWatts(intervals);
        Double totalTime = Workout.totalTime(intervals);
        Integer SPM = Workout.averageSPM(intervals);
        Workout workout = new Workout(intervals,SPM,watts,totalTime,date);
        return workout;

    }

    //This call is SUPER LAZY and needs to be optimized
    public Cursor getAllWorkoutsCursor(){
        return (workoutDatabase.query(DatabaseSchema.DataBaseTerms.WORKOUTS_TABLE_NAME,null,null,null,null,null,null));
    }

}

    //public Workout getWorkoutsByDate()

    //public List<Workout> getWorkoutsByDistance(Integer distance){

    //}

    //options for interval delete problem:::
    //1 do nothing, just try and insert the new interval fail if PK violated and dont delete when delete is called will have to periodically tidy up to keep table optimal
    //2 check rel table for all rels that reference intervals when deleting very inefficient -- NO
    //3 add col that tracks references.

