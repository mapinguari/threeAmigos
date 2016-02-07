package com.example.mapinguari.workoutclass.exerciseObjectBuilders;

import android.util.Log;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by mapinguari on 1/11/16.
 *
 * STUFF TO DO:
 * Probably need to build some form of sanity logic into the correction
 * Combine deciphered workout and protoworkout types
 * Keep check of consistent rows and columns
 */
public class WorkoutChecker extends PerfomanceMeasureChecker {

    private GregorianCalendar protoGregCal;
    private List<IntervalChecker> protoIntervals;

    private Workout.WorkoutType protoWorkoutType = null;
    private Double workoutTypeValue = null;
    private WorkoutTypeTriple decphieredWorkoutType = null;

    private WorkoutTypeTriple decidedWorkoutType = null;

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

    public Vector<Vector<String>> getVVS(){
        Vector<Vector<String>> vvsWorkout = new Vector<>();
        Vector<String> totals = this.getVS();
        vvsWorkout.add(totals);
        for(IntervalChecker ic : protoIntervals){
            vvsWorkout.add(ic.getVS());
        }
        return vvsWorkout;
    }


    //THIS IS CURRENTLY INCOMPLETE
    //We will be fed information about what type of workout we have from both the top left and
    // the information in the table. Need to try to figure out which workout Type is most likely.
    public void determineWorkout(){
        if(decidedWorkoutType == null){
            workoutDecipher();
        }
        if(decphieredWorkoutType != null){
            decidedWorkoutType = decphieredWorkoutType;
        }
    }

    public void performAllCorrection(int numberOfIterations, boolean horizontalFirst){
        if(decidedWorkoutType ==null)
            determineWorkout();

        correctSPM();

        int iteration = 0;
        int numberOfZerosLast= 4*(protoIntervals.size() +1);
        while(!fullWorkout() && numberOfZerosLast > numberOfZeros() && iteration < numberOfIterations){
            if(horizontalFirst)
                horizontalChecks();
            //verticalCorrection
            correctWorkoutTypeColumn();
            correctNonCumulativeColumn();

            if(!horizontalFirst)
                horizontalChecks();

            if(decidedWorkoutType ==null)
                determineWorkout();

            numberOfZerosLast = numberOfZeros();
            Log.w("iteration Number",Integer.toString(iteration));
            iteration++;
        }


    }

    @Override
    public int numberOfZeros(){
        int sum = super.numberOfZeros();
        for(IntervalChecker i:protoIntervals){
            sum += i.numberOfZeros();
        }
        return sum;
    }

    public boolean fullWorkout(){
        boolean full = fullPerformanceMeasure();
        for(IntervalChecker i: protoIntervals)
            full &= i.fullInterval();
        return full;
    }

    public Workout getWorkout(){
        List<Interval> intervalList = new ArrayList<>(protoIntervals.size());
        for(IntervalChecker iC : protoIntervals){
            intervalList.add(iC.getInterval());
        }
        return new Workout(intervalList,protoSPM,protoDistance,protoTime,protoGregCal);
    }





    //Code to repair the non cumulative Column of a work out.

    public boolean correctNonCumulativeColumn(){
        boolean repairedColumn = false;
        if(decidedWorkoutType == null){
            repairedColumn = false;
        } else if(decidedWorkoutType.workoutType == Workout.WorkoutType.Distance){
            repairedColumn = correctNCumTime();
        } else if(decidedWorkoutType.workoutType == Workout.WorkoutType.JustRow
                || decidedWorkoutType.workoutType == Workout.WorkoutType.Time){
            repairedColumn = correctNCumDistance();
        }
        return repairedColumn;
    }

    private boolean correctNCumTime(){
        boolean allNonZero = true;
        Double x = protoTime;

        Double psiSum = 0.0;
        int numberOfZeros = 0;
        Double cProtoTime;

        for(IntervalChecker intervalChecker:protoIntervals){
            cProtoTime = intervalChecker.protoTime;
            if(cProtoTime == 0){
                numberOfZeros++;
            }
            psiSum += cProtoTime;
        }
        Double psi;
        if(x==0&&numberOfZeros>0){
            allNonZero = false;
        } else if(x==0){
            protoTime = psiSum;
            allNonZero = true;
        } else if(numberOfZeros>0){
            psi = x - psiSum / numberOfZeros;
            for(IntervalChecker intervalChecker : protoIntervals){
                if(intervalChecker.zeroTime){
                    intervalChecker.fixExternalTime(psi);
                }
            }
            allNonZero = true;
        }
        return allNonZero;
    }

    private boolean correctNCumDistance(){
        boolean allNonZero = true;
        Double x = protoDistance;

        Double psiSum = 0.0;
        int numberOfZeros = 0;
        Double cProtoDistance;

        for(IntervalChecker intervalChecker:protoIntervals){
            cProtoDistance = intervalChecker.protoDistance;
            if(cProtoDistance == 0){
                numberOfZeros++;
            }
            psiSum += cProtoDistance;
        }
        Double psi;
        if(x==0&&numberOfZeros>0){
            allNonZero = false;
        } else if(x==0){
            protoDistance = psiSum;
            allNonZero = true;
        } else if(numberOfZeros>0){
            psi = x - psiSum / numberOfZeros;
            for(IntervalChecker intervalChecker : protoIntervals){
                if(intervalChecker.zeroDistance){
                    intervalChecker.fixExternalDistance(psi);
                }
            }
            allNonZero = true;
        }
        return allNonZero;
    }

    // Non cumulative code ends here.


    //WE can introduce an integer to denote how aggressively we wish to try and make data appear in
    // the cells. here is a prime example where we could try too aggresively to fit data.

    //WE also need to think about what to do about numbers that singularly seem horrifically out.
    //Should we try and correct that?!

    private void correctSPM(){
        Integer spm = protoSPM;
        IntervalChecker curInterval;

        Integer curSPM;
        int noOfZeros = 0,noOfRows = protoIntervals.size();
        double intSum = 0;
        double replacementSPM = 0;
        int realreplacementSPM;



        List<Integer> sspm = new ArrayList<Integer>(noOfRows);
        List<Integer> zeros = new ArrayList();

        for(int i = 0;i <noOfRows;i++){
            curSPM = protoIntervals.get(i).protoSPM;
            sspm.add(curSPM);
            if(curSPM == 0) {
                zeros.add(i);
                noOfZeros++;
            } else {
                intSum +=curSPM;
            }

        }


        if(spm == 0){
            if(noOfZeros >0){
                replacementSPM = intSum / (noOfRows - noOfZeros);
            } else {
                replacementSPM = intSum / noOfRows;
            }
        } else {
            if(noOfZeros > 0){
                replacementSPM = ((double) noOfRows / noOfZeros) * (spm - (intSum / noOfRows));
            }
        }

        //HERE I AM NOT AT ALL CONSIDERING THE PROBLEM INVOLVING FLOAT -> INT CONVERSION!
        realreplacementSPM = (int) replacementSPM;

        if(spm == 0)
            this.protoSPM = realreplacementSPM;

        for(Integer i : zeros){
            curInterval = protoIntervals.get(i);
            curInterval.fixExternalSPM(realreplacementSPM);
        }
    }

    //Determine what type of workout we have and correct it, code.
    //Begins
    public void correctWorkoutTypeColumn(){
        switch(decidedWorkoutType.workoutType){
            case JustRow: justRowCorrect(decidedWorkoutType.total);
                break;
            case Time: timeCorrect(decidedWorkoutType.average,decidedWorkoutType.total);
                break;
            case Distance: distanceCorrect(decidedWorkoutType.average,decidedWorkoutType.total);
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
            decphieredWorkoutType = result;
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
            decphieredWorkoutType = result;
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
            decphieredWorkoutType = result;
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
