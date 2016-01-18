package com.example.mapinguari.workoutclass.exerciseObjectParsers;

import com.example.mapinguari.workoutclass.exerciseObjectBuilders.IntervalChecker;
import com.example.mapinguari.workoutclass.exerciseObjectBuilders.WorkoutChecker;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mapinguari on 1/12/16.
 *
 * Things to do
 * 1. deal with the top left corner data coming in.
 */
public class WorkoutParser extends PerformanceMeasureParser {

    private Double workoutTypeString = null;

    private Workout.WorkoutType workoutType = null;
    private Double workoutTypeValue;

    private GregorianCalendar workoutTime;
    private List<IntervalParser> subIntervalParsers;

    //Not currently dealing with rest time. push in a 0.0 value for the meantime.
    private Double STATIC_REST_TIME_DOUBLE = 0.0;

    public WorkoutParser(String timeString, String distanceString, String column3String,
                         String spmString, GregorianCalendar workoutTime,
                         Vector<Vector<String>> subIntervalStrings,Double workoutTypeString) {
        super(timeString, distanceString, column3String, spmString);
        this.workoutTypeString = workoutTypeString;
        this.workoutTime = workoutTime;
        this.subIntervalParsers = intervalParsersFromVVS(subIntervalStrings);
    }

    public WorkoutParser(Vector<String> stringRow, GregorianCalendar workoutTime,
                         Vector<Vector<String>> subIntervalStrings, Double workoutTypeString) {
        super(stringRow);
        this.workoutTypeString = workoutTypeString;
        this.workoutTime = workoutTime;
        this.subIntervalParsers = intervalParsersFromVVS(subIntervalStrings);
    }

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



    //If a row contains any data, we take it.
    private List<IntervalParser> intervalParsersFromVVS(Vector<Vector<String>> subIntervalStrings){
        ArrayList<IntervalParser> intervalParsers = new ArrayList<>();
        for(Vector<String> vs : subIntervalStrings){
            if(containsSomeData(vs))
                intervalParsers.add(new IntervalParser(vs,STATIC_REST_TIME_DOUBLE));
        }
        return intervalParsers;
    }

    /**
     * @return true if has some digits in, false otherwise
     */
    private boolean containsSomeData(String s){
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }

    private boolean containsSomeData(Vector<String> vs){
        boolean containsData = false;
        for(String s: vs){
            if(containsSomeData(s))
                containsData = true;
                break;
        }
        return containsData;
    }

    //consider top level split for vs that are not of length 4 to prevent each interval doing it
    public WorkoutChecker runForWorkoutChecker(){
        getAllValues();
        ArrayList<IntervalChecker> intervalCheckers = new ArrayList<>(subIntervalParsers.size());
        for(IntervalParser intervalParser:subIntervalParsers){
            intervalCheckers.add(intervalParser.runForIntervalChecker());
        }
        if(workoutType ==  null)
            return new WorkoutChecker(timeDouble,distanceDouble,splitDouble,spmInteger,workoutTime,intervalCheckers);
        else
            return new WorkoutChecker(timeDouble,distanceDouble,splitDouble,spmInteger,workoutTime,intervalCheckers,workoutType,workoutTypeValue);
    }




    //Not sure how best to do this. Does just dumping the seconds in the seconds field update others?
    //Worry about this later.
    //PLan to try to set the start time of the workout closer to the actual start time. TofPhoto-SecondsofWorkout
    public void fixGregCal(){

    }
}
