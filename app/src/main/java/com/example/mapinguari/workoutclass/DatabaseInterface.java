package com.example.mapinguari.workoutclass;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by mapinguari on 7/24/15.
 */
public class DatabaseInterface {

    private SQLiteDatabase workoutDatabase;

    public DatabaseInterface(SQLiteDatabase workoutDatabase) {
        this.workoutDatabase = workoutDatabase;
    }


    public Boolean insertWorkout(Workout workout) {
        //TODO:HANDLE FAILED INSERTS
        int workout_ID = (int) workoutDatabase.insert(DatabaseSchema.DataBaseTerms.WORKOUTS_TABLE_NAME, null, DatabaseSchema.workoutContent(workout));
        int interval_ID;
        int ordin = 0;
        ContentValues cv;
        for (Interval i : workout.getIntervalList()) {
            interval_ID = (int) insertInterval(i);
            cv = DatabaseSchema.workOutRelContent(workout_ID, ordin, interval_ID);
            workoutDatabase.insert(DatabaseSchema.DataBaseTerms.WORKOUT_RELATIONS_TABLE_NAME, null, cv);
            ordin++;
        }
        //Obviously this is not safe
        return true;
    }

    private long insertInterval(Interval interval) {
        //TODO: This will need to be re-written to handle deletes efficiently
        return (workoutDatabase.insert(DatabaseSchema.DataBaseTerms.INTERVAL_TABLE_NAME, null, DatabaseSchema.intervalContent(interval)));
    }

    public void deleteWorkout(Integer Workout_ID) {
        //TODO: CODE DELETEWORKOUT
        //TODO: This is a nightmare. To do this I am going to have to add a column to the intervals table which tracks the number of references to the row. As far as I can tell this can not be carried out with a paradigmatic call to update and will require execsql() FUCK.
    }

    private GregorianCalendar getWorkoutDate(Integer workout_ID) {
        //Change this to use query()
        String[] args = {workout_ID.toString()};
        Cursor firstC = workoutDatabase.rawQuery(DatabaseSchema.QUERY_SINGLE_ID_DATE, args);
        if (firstC.getCount() == 0) {
            throw new SQLiteException("No workout with that ID");
        }
        String datetime = firstC.getString(0);
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
        do {
            ordinal = firstC.getInt(0);
            SPM = firstC.getInt(1);
            time = firstC.getDouble(2);
            watts = firstC.getDouble(3);
            resttime = firstC.getDouble(4);
            curInt = new Interval(time, watts, SPM, resttime);
            intervals[ordinal] = curInt;

        } while (!(firstC.isLast()));
        List<Interval> intervalsList = Arrays.asList(intervals);
        return intervalsList;

    }

    public Workout getWorkOut(Integer workout_ID){
        GregorianCalendar date = getWorkoutDate(workout_ID);
        List<Interval> intervals = getIntervals(workout_ID);
        Double watts = getAvgWatts(intervals);
        Double totalTime = getTotalTime(intervals);
        Integer SPM = getSPM(intervals);
        Workout workout = new Workout(intervals,SPM,watts,totalTime,date);
        return workout;

    }

    private Double getAvgWatts(List<Interval> intervals){
        double sum = 0;
        for(Interval i : intervals){
            sum = sum + i.getAverageWatts();
        }
        return (sum / intervals.size());
    }

    private Double getTotalTime(List<Interval> intervals) {
        double sum = 0;
        for (Interval i : intervals) {
            sum = sum + i.getWorkTime();
        }
        return sum;
    }

    private Integer getSPM(List<Interval> intervals){
        double sum = 0;
        for(Interval i : intervals){
            sum = sum + i.getAverageWatts();
        }
        return (int) Math.floor(sum / intervals.size());
    }

}

    //public Workout getWorkoutsByDate()

    //public List<Workout> getWorkoutsByDistance(Integer distance){

    //}

    //options for interval delete problem:::
    //1 do nothing, just try and insert the new interval fail if PK violated and dont delete when delete is called will have to periodically tidy up to keep table optimal
    //2 check rel table for all rels that reference intervals when deleting very inefficient -- NO
    //3 add col that tracks references.

