package com.example.mapinguari.workoutclass.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mapinguari.workoutclass.exerciseObjects.GregtoString;
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

    public DatabaseInterface(Context context){
        DatabaseHelper dbH = new DatabaseHelper(context);
        AsyncTask dbT = new DatabaseOpenTask().execute(dbH);
        //TODO: Exception handling
        //databaseBacker
        SQLiteDatabase dbB;
        try{
            dbB = (SQLiteDatabase) dbT.get();
            workoutDatabase = dbB;
        } catch(Throwable tr){
            Log.w("DAtabase", "did not initialise");
        }
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
        //TODO: THE RETURN VALUE IS NOT CORRECT FIX THIS
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
            Boolean intervalinsertedTest = interval_ID >= 0;
            intervalsInserted = (intervalinsertedTest) && intervalsInserted;
            cv = DatabaseSchema.workOutRelContent(workout_ID, ordin, interval_ID);
            workoutDatabase.insert(DatabaseSchema.DataBaseTerms.WORKOUT_RELATIONS_TABLE_NAME, null, cv);
            ordin++;
        }
        Log.w("result",Boolean.toString(intervalsInserted && workoutInserted));
        return intervalsInserted && workoutInserted;
    }

    private long insertInterval(Interval interval) {
        //TODO: This will need to be re-written to handle deletes efficiently
        long rowId;
        try {
            rowId = workoutDatabase.insertOrThrow(DatabaseSchema.DataBaseTerms.INTERVAL_TABLE_NAME, null, DatabaseSchema.intervalContent(interval));
        } catch(SQLException e){
            Cursor resp;
            String[] cols = {DatabaseSchema.DataBaseTerms._ID};
            StringBuilder where = new StringBuilder();
            where.append(DatabaseSchema.DataBaseTerms.getColumnNameTime() + " =? AND ");
            where.append(DatabaseSchema.DataBaseTerms.getColumnNameDistance() + " =? AND ");
            where.append(DatabaseSchema.DataBaseTerms.getColumnNameAverageSpm() + " =? AND ");
            where.append(DatabaseSchema.DataBaseTerms.getColumnNameResttime() + " =?");
            String[] test = {Integer.toString(1)};
            resp = workoutDatabase.query(DatabaseSchema.DataBaseTerms.getIntervalTableName(),cols,where.toString(),interval.toStringArr(),null,null,null);
            resp.moveToFirst();
            rowId = resp.getLong(0);
        }
        return rowId;
    }

    //V0.1
    public int deleteWorkout(Integer Workout_ID) {
        String[] args = {Workout_ID.toString()};
        int nor = workoutDatabase.delete(DatabaseSchema.DataBaseTerms.WORKOUTS_TABLE_NAME, DatabaseSchema.DELETE_WORKOUT_ROW_WHERE_CLAUSE,args);
        if(nor == 0){
            throw new SQLiteException("No workout with specified ID");
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
        double distance;
        double resttime;
        Interval curInt;
        Interval[] intervals = new Interval[nOI];
        firstC.moveToFirst();
        for(int i = 0;i < nOI;i++){
            ordinal = firstC.getInt(0);
            SPM = firstC.getInt(1);
            time = firstC.getDouble(2);
            distance = firstC.getDouble(3);
            resttime = firstC.getDouble(4);
            curInt = new Interval(time, distance, SPM, resttime);
            intervals[ordinal] = curInt;
            firstC.moveToNext();
        }
        List<Interval> intervalsList = Arrays.asList(intervals);
        return intervalsList;

    }

    public Workout getWorkOut(Integer workout_ID){
        GregorianCalendar date = getWorkoutDate(workout_ID);
        List<Interval> intervals = getIntervals(workout_ID);

        Double distance = Workout.totalDistance(intervals);
        Double totalTime = Workout.totalTime(intervals);
        Integer SPM = Workout.averageSPM(intervals);
        Workout workout = new Workout(intervals,SPM,distance,totalTime,date);
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

