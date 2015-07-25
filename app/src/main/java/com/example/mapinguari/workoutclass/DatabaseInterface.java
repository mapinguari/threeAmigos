package com.example.mapinguari.workoutclass;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

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



    public Boolean insertWorkout(Workout workout){
        //TODO:HANDLE FAILED INSERTS
        int workout_ID = (int) workoutDatabase.insert(DatabaseSchema.DataBaseTerms.WORKOUTS_TABLE_NAME,null,DatabaseSchema.workoutContent(workout));
        int interval_ID;
        int ordin = 0;
        ContentValues cv;
        for (Interval i : workout.getIntervalList()){
            interval_ID = (int) insertInterval(i);
            cv = DatabaseSchema.workOutRelContent(workout_ID,ordin,interval_ID);
            workoutDatabase.insert(DatabaseSchema.DataBaseTerms.WORKOUT_RELATIONS_TABLE_NAME,null,cv);
        }
        //Obviously this is not safe
        return true;
    }

    private long insertInterval(Interval interval){
        //TODO: This will need to be re-written to handle deletes efficiently
        return (workoutDatabase.insert(DatabaseSchema.DataBaseTerms.INTERVAL_TABLE_NAME,null,DatabaseSchema.intervalContent(interval)));
    }

    public void deleteWorkout(Integer Workout_ID){
        //TODO: CODE DELETEWORKOUT
        //TODO: This is a nightmare. To do this I am going to have to add a column to the intervals table which tracks the number of references to the row. As far as I can tell this can not be carried out with a paradigmatic call to update and will require execsql() FUCK.
    }
    //ACTUALLY RETURN WORKOUT
    public void getWorkoutByID(Integer Workout_ID){
        //TODO: CODE GETWORKOUTBYID
    }

    //public Workout getWorkoutsByDate()

    //public List<Workout> getWorkoutsByDistance(Integer distance){

    //}

    //options for interval delete problem:::
    //1 do nothing, just try and insert the new interval fail if PK violated and dont delete when delete is called will have to periodically tidy up to keep table optimal
    //2 check rel table for all rels that reference intervals when deleting very inefficient -- NO
    //3 add col that tracks references.
}
