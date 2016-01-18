package com.example.mapinguari.workoutclass.exerciseObjects.Generators;

import com.example.mapinguari.workoutclass.exerciseObjects.Generators.IntervalGen;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

/**
 * Created by mapinguari on 8/2/15.
 */
public class WorkoutGen {

    IntervalGen iG;
    Random r;

    public WorkoutGen(){
        r = new Random();
        iG = new IntervalGen(r);
    }

    public Workout consistentWorkoutGen(int numberOfInt){
        List<Interval> iL = iG.generateIntevals(numberOfInt);
        double distance = Workout.totalDistance(iL);
        double time = Workout.totalTime(iL);
        int spm = Workout.averageSPM(iL);
        GregorianCalendar g = new GregorianCalendar(r.nextInt(3000),r.nextInt(11),r.nextInt(28),r.nextInt(23),r.nextInt(59),r.nextInt(59));
        return new Workout(iL,spm,distance,time,g);
    }

    public List<Workout> workoutsGen(int numberofWorkouts,int maxNumberIntervals){
        ArrayList<Workout> workouts = new ArrayList<Workout>(numberofWorkouts);
        Workout temp;
        int noOfInt;
        for(int i = 0; i < numberofWorkouts; i++){
            noOfInt = r.nextInt(maxNumberIntervals - 1) + 1;
            temp = consistentWorkoutGen(noOfInt);
            workouts.add(i,temp);
        }
        return workouts;
    }
}
