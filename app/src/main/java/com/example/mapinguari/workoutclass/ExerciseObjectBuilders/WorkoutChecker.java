package com.example.mapinguari.workoutclass.ExerciseObjectBuilders;

import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mapinguari on 1/11/16.
 */
public class WorkoutChecker extends PerfomanceMeasureChecker {

    private GregorianCalendar protoGregCal;
    private List<IntervalChecker> protoIntervals;
    private Workout.WorkoutType protoWorkoutType = null;
    private Double workoutTypeValue = null;
    private WorkoutTypeTriple decipheredWorkoutType;

    private class WorkoutTypeTriple {
        public Workout.WorkoutType workoutType;
        public Double total;
        public Double average;

        public WorkoutTypeTriple(Workout.WorkoutType workoutType, Double total, Double average) {
            this.workoutType = workoutType;
            this.total = total;
            this.average = average;
        }
    }


    public WorkoutChecker(Double protoTime, Double protoDistance, Double protoSplit,
                          Integer protoSPM, GregorianCalendar protoGregCal, List<IntervalChecker> protoIntervals) {
        super(protoTime, protoDistance, protoSplit, protoSPM);
        this.protoGregCal = protoGregCal;
        this.protoIntervals = protoIntervals;
    }

    public WorkoutChecker(Double protoTime, Double protoDistance, Double protoSplit,
                          Integer protoSPM, GregorianCalendar protoGregCal,
                          List<IntervalChecker> protoIntervals, Workout.WorkoutType protoWorkoutType, Double workoutTypeValue) {
        super(protoTime, protoDistance, protoSplit, protoSPM);
        this.protoGregCal = protoGregCal;
        this.protoIntervals = protoIntervals;
        this.protoWorkoutType = protoWorkoutType;
        this.workoutTypeValue = workoutTypeValue;
    }



    public Workout getWorkout(){
        List<Interval> intervalList = new ArrayList<>(protoIntervals.size());
        for(IntervalChecker iC : protoIntervals){
            intervalList.add(iC.getInterval());
        }
        return new Workout(intervalList,protoSPM,protoDistance,protoTime,protoGregCal);
    }


    //Vertical time
    //Vertical Distance
    //Vertical SPM
    //Combine deciphered workout and protoworkout types
    //Keep check of consistent rows and columns


    //Determine what type of workout we have and correct it, code.
    //Begins
    public void CorrectForWorkoutType(WorkoutTypeTriple wtt){
        switch(wtt.workoutType){
            case JustRow: justRowCorrect(wtt.total);
                break;
            case Time: timeCorrect(wtt.average,wtt.total);
                break;
            case Distance: distanceCorrect(wtt.average,wtt.total);
                break;
        }
    }

    public Double mostFrequentTimeInterval(){

        Double curInterval,t1,t2;
        Integer lastFreq;

        Double mfInterval = 0.0;
        Integer noOfpulls = 0;

        Map<Double,Integer> tracker = new HashMap<>();
        for(int i = 0;i<protoIntervals.size()-2;i++) {
            t1 = protoIntervals.get(i).protoTime;
            t2 = protoIntervals.get(i + 1).protoTime;

            if (t1 != 0.0 && t2 != 0.0) {
                curInterval = t2 - t1;
                lastFreq = tracker.get(curInterval);
                tracker.put(curInterval, lastFreq == null ? 1 : lastFreq + 1);
            }
        }
        for(Map.Entry<Double,Integer> e : tracker.entrySet()){
            if(e.getValue() > noOfpulls){
                noOfpulls = e.getValue();
                mfInterval = e.getKey();
            }
        }
        return mfInterval;
    }

    public Double mostFrequentDistanceInterval(){

        Double curInterval,t1,t2;
        Integer lastFreq;

        Double mfInterval = 0.0;
        Integer noOfpulls = 0;

        Map<Double,Integer> tracker = new HashMap<>();
        for(int i = 0;i<protoIntervals.size()-2;i++) {
            t1 = protoIntervals.get(i).protoDistance;
            t2 = protoIntervals.get(i + 1).protoDistance;

            if (t1 != 0.0 && t2 != 0.0) {
                curInterval = t2 - t1;
                lastFreq = tracker.get(curInterval);
                tracker.put(curInterval, lastFreq == null ? 1 : lastFreq + 1);
            }
        }
        for(Map.Entry<Double,Integer> e : tracker.entrySet()){
            if(e.getValue() > noOfpulls){
                noOfpulls = e.getValue();
                mfInterval = e.getKey();
            }
        }
        return mfInterval;
    }

    private void justRowCorrect(Double total){
        int n = protoIntervals.size();
        protoTime = total;
        for(int i = 0; i <n-1;i++){
            protoIntervals.get(i).fixExternalTime(300.0);
        }
        protoIntervals.get(n-1).fixExternalTime(total - (300*(n-1)));
    }

    private void timeCorrect(Double averageTime,Double totalTime){
        protoTime = totalTime;
        for(IntervalChecker i : protoIntervals){
            i.fixExternalTime(averageTime);
        }
    }

    private void distanceCorrect(Double averageDistance,Double totalDistance){
        protoDistance = totalDistance;
        for(IntervalChecker i : protoIntervals){
            i.fixExternalDistance(averageDistance);
        }
    }

    private boolean workoutIsJustRow(Double time){
        int size = protoIntervals.size();
        return time > (300 * (size - 1)) && time < (300 * size);
    }

    //Figure out whether time, distance or JR
    public WorkoutTypeTriple workoutDecipher(){


        //Easy Wins first this only requires 2 correct ocrs

        int size = protoIntervals.size();
        IntervalChecker lastIntervalChecker = protoIntervals.get(size-1);

        //xAVGI = x average interval
        Double timeTotal = (protoTime!=0.0?protoTime:lastIntervalChecker.protoTime);
        Double timeAVGI =  timeTotal /size;

        Double distanceTotal = (protoDistance!=0.0?protoDistance:lastIntervalChecker.getProtoDistance());
        Double distanceAVGI =  distanceTotal / size;

        //xTEL = x Total equals last
        boolean timeTEL = protoTime.equals(lastIntervalChecker.protoTime)
                && timeTotal != 0.0;
        boolean distanceTEL = protoDistance.equals(lastIntervalChecker.protoDistance)
                && distanceTotal != 0.0;

        //We can use one of the two booleans to determine the workout type.

        WorkoutTypeTriple result = null;

        if(timeTEL || distanceTEL) {
            if (timeTEL) {
                if (workoutIsJustRow(timeTotal)) {
                    result = new WorkoutTypeTriple(Workout.WorkoutType.JustRow,timeTotal,timeAVGI);
                } else {
                    result = new WorkoutTypeTriple(Workout.WorkoutType.Time,timeTotal, timeAVGI);
                }
            } else {
                result = new WorkoutTypeTriple(Workout.WorkoutType.Distance,distanceTotal, distanceAVGI);
            }
            return result;
        }

        //Ok, so we don't have it confirmed that the total and the last added up. Lets see if the
        //other column is cumulative. This requires n+2 correct ocrs
        //Check if the other adds up to the total

        Double timeSum = 0.0,distanceSum = 0.0;
        for(IntervalChecker i : protoIntervals){
            timeSum += i.getProtoTime();
            distanceSum += i.getProtoDistance();
        }

        //xTEC = x total equals cumulative
        boolean timeTEC = protoTime.equals(timeSum);
        boolean distanceTEC = protoDistance.equals(distanceSum);

        if(timeTEC && (distanceAVGI !=0.0) || (distanceTEC && (timeAVGI != 0.0))){
            if(timeTEC){
                result = new WorkoutTypeTriple(Workout.WorkoutType.Distance,distanceTotal, distanceAVGI);
            } else {
                if(workoutIsJustRow(timeTotal)){
                    result = new WorkoutTypeTriple(Workout.WorkoutType.JustRow,timeTotal,timeAVGI);
                } else {
                    result = new WorkoutTypeTriple(Workout.WorkoutType.Time,timeTotal, timeAVGI);
                }
            }
            return result;
        }

        //Right, now we are getting desperate. Lets see what the most common interval difference
        //exists. Hopefully it will match one of the averages
        //requires at least 3 correct ocrs... but it is ropey as hell.



        //xFREI = x most frequent interval
        Double timeFREI = mostFrequentTimeInterval();
        Double distanceFREI = mostFrequentDistanceInterval();

        //xIE = x intervals equal
        boolean timeII = timeAVGI.equals(timeFREI);
        boolean distanceII = distanceAVGI.equals(distanceFREI);

        if(timeII && (timeAVGI !=0.0) || (distanceTEC && (distanceAVGI != 0.0))){
            if(distanceII){
                result = new WorkoutTypeTriple(Workout.WorkoutType.Distance,distanceTotal, distanceAVGI);
            } else {
                if(workoutIsJustRow(timeTotal)){
                    result = new WorkoutTypeTriple(Workout.WorkoutType.JustRow,timeTotal,timeAVGI);
                } else {
                    result = new WorkoutTypeTriple(Workout.WorkoutType.Time,timeTotal, timeAVGI);
                }
            }
            return result;
        }

        //OK, none of that worked. We can do even more rope ass stuff.... but... probably not wise.
        //at this point throw up and give up

        return result;

    }

    //Workout Type determining code ends here

    public void horizontalChecks(){
        internalFix();
        for(IntervalChecker pI : protoIntervals){
            pI.internalFix();
        }
    }



}
