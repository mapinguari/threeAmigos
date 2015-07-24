package com.example.mapinguari.workoutclass;

import android.database.sqlite.SQLiteDatabase;

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
        //TODO: CODE INSERTWORKOUT
    }

    public Boolean deleteWorkout(Integer Workout_ID){
        //TODO: CODE DELETEWORKOUT
    }

    public Workout getWorkoutByID(Integer Workout_ID){
        //TODO: CODE GETWORKOUTBYID
    }

    //public Workout getWorkoutsByDate()

    public List<Workout> getWorkoutsByDistance(Integer distance){

    }


}
