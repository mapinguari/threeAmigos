package com.example.mapinguari.workoutclass;

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
        double watts = Workout.averageWatts(iL);
        double time = Workout.totalTime(iL);
        int spm = Workout.averageSPM(iL);
        GregorianCalendar g = new GregorianCalendar(r.nextInt(3000),r.nextInt(11),r.nextInt(28),r.nextInt(23),r.nextInt(59),r.nextInt(59));
        return new Workout(iL,spm,watts,time,g);
    }

    public List<Workout> workoutsGen(int numberofWorkouts,int maxNumberIntervals){
        ArrayList<Workout> workouts = new ArrayList<Workout>(numberofWorkouts);
        Workout temp;
        int noOfInt;
        for(int i = 0; i < numberofWorkouts; i++){
            noOfInt = r.nextInt(maxNumberIntervals);
            temp = consistentWorkoutGen(noOfInt);
            workouts.add(i,temp);
        }
        return workouts;
    }
}
