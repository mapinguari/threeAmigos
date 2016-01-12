package com.example.mapinguari.workoutclass.ExerciseObjectParsers;

import com.example.mapinguari.workoutclass.ExerciseObjectBuilders.WorkoutChecker;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

/**
 * Created by mapinguari on 1/12/16.
 */
public class WorkoutParser extends PerformanceMeasureParser {

    private GregorianCalendar workoutTime;
    private List<IntervalParser> subIntervalParsers;

    //Not currently dealing with rest time. push in a 0.0 value for the meantime.
    private Double STATIC_REST_TIME_DOUBLE = 0.0;


    //NEED TO INCLUDE CONSTRUCTORS FOR THE TOP LEFT VALUE
    //ALSO NEED TO INCLUDE FIELD VARIABLES

    public WorkoutParser(String timeString, String distanceString, String column3String,
                         String spmString, GregorianCalendar workoutTime,
                         Vector<Vector<String>> subIntervalStrings) {
        super(timeString, distanceString, column3String, spmString);
        this.workoutTime = workoutTime;
        this.subIntervalParsers = intervalParsersFromVVS(subIntervalStrings);
    }

    public WorkoutParser(Vector<String> totalsStrings, GregorianCalendar workoutTime,
                         Vector<Vector<String>> subIntervalStrings) {
        super(totalsStrings);
        this.workoutTime = workoutTime;

        this.subIntervalParsers = intervalParsersFromVVS(subIntervalStrings);
    }

    private List<IntervalParser> intervalParsersFromVVS(Vector<Vector<String>> subIntervalStrings){
        ArrayList<IntervalParser> intervalParsers = new ArrayList<>();
        for(Vector<String> vs : subIntervalStrings){
            intervalParsers.add(new IntervalParser(vs,STATIC_REST_TIME_DOUBLE));
        }
        return intervalParsers;
    }

    //consider top level split for vs that are not of length 4 to prevent each interval doing it
//    public WorkoutChecker getWorkoutChecker(){
//        return new WorkoutChecker(timeDouble,distanceDouble,splitDouble,spmInteger,workoutTime,subIntervalParsers)
//    }


    //Not sure how best to do this. Does just dumping the seconds in the seconds field update others?
    //Worry about this later.
    //PLan to try to set the start time of the workout closer to the actual start time. TofPhoto-SecondsofWorkout
    public void fixGregCal(){

    }
}
