package com.example.mapinguari.workoutclass;

import android.util.Log;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.exceptions.CantDecipherWorkoutException;
import com.example.mapinguari.workoutclass.exceptions.NotHumanStringException;
import com.example.mapinguari.workoutclass.exerciseObjects.ErgoFormatter;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mapinguari on 1/1/16.
 */
public class BolderWorkout {

    public Workout bolderWorkout(Vector<Vector<String>> vvs){

        List<Interval> intervalListTemp = new ArrayList<Interval>(vvs.size() - 1);
        Interval totalsInterval,curInterval = null;
        Workout result;

        totalsInterval = getInterval(vvs.get(0));

        for(int i = 1; i< vvs.size(); i++){
            curInterval = getInterval(vvs.get(i));
            intervalListTemp.add(curInterval);
        }



        result = new Workout(intervalListTemp,totalsInterval.getSPM(),totalsInterval.getDistance(),totalsInterval.getTime(),new GregorianCalendar());
        try {
            result = workoutDecipher(result);
        }catch (CantDecipherWorkoutException e){

        }
        return result;
    }

    /*Bolder workout code begins here

     */

    private List<Interval> justRowCorrect(List<Interval> intervals, Double total){
        int n = intervals.size();
        for(int i = 0; i <n-1;i++){
            intervals.get(i).setWorkTime(300.0);
        }
        intervals.get(n-1).setWorkTime(total - (300*n));
        return intervals;
    }

    private List<Interval> timeCorrect(List<Interval> intervals, Double time){
        for(Interval i : intervals){
            i.setWorkTime(time);
        }
        return intervals;

    }

    private List<Interval> distanceCorrect(List<Interval> intervals, Double distance){
        for(Interval i : intervals){
            i.setDistance(distance);
        }
        return intervals;
    }

    public Double mostFrequentTimeInterval(List<Interval> intervalList){

        Double curInterval,t1,t2;
        Integer lastFreq;

        Double mfInterval = 0.0;
        Integer noOfpulls = 0;

        Map<Double,Integer> tracker = new HashMap<>();
        for(int i = 0;i<intervalList.size()-2;i++) {
            t1 = intervalList.get(i).getTime();
            t2 = intervalList.get(i + 1).getTime();

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

    public Double mostFrequentDistanceInterval(List<Interval> intervalList){

        Double curInterval,t1,t2;
        Integer lastFreq;

        Double mfInterval = 0.0;
        Integer noOfpulls = 0;

        Map<Double,Integer> tracker = new HashMap<>();
        for(int i = 0;i<intervalList.size()-2;i++) {
            t1 = intervalList.get(i).getDistance();
            t2 = intervalList.get(i + 1).getDistance();

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



    /*Workout's comming straight out the back of OCR have one main problem
    Depending on whether the workout was set up as a Distance, time or just row session
    respectively the distance,time,or time columns could be cumulative in
    (regular,regular,regular except for the last) the interval size in the respective columns.
    (total / noOfIntervals , total/ noOfIntervals, 300s except for the last)
    This function will attempt to decipher which is the most likely.

    *****NOTE It is possible to extract this information from the top left of the ergo screen. However this is
    * not something we are currently doing!!!!
     */

    public Workout workoutDecipher(Workout workout) throws CantDecipherWorkoutException {
        //Figure out whether time, distance or JR

        List<Interval> intervalList = workout.getIntervalList();

        //Easy Wins first this only requires 2 correct ocrs

        int size = intervalList.size();

        //xAVGI = x average interval
        Double timeTotal = (workout.getTime()!=0.0?workout.getTime():intervalList.get(size-1).getTime());
        Double timeAVGI =  timeTotal / intervalList.size();

        Double distanceTotal = (workout.getDistance()!=0.0?workout.getDistance():intervalList.get(size-1).getDistance());
        Double distanceAVGI =  distanceTotal / intervalList.size();

        //xTEL = x Total equals last
        boolean timeTEL = workout.getTime().equals(intervalList.get(intervalList.size() - 1).getTime())
                && workout.getTime() != 0.0;
        boolean distanceTEL = workout.getDistance().equals(intervalList.get(intervalList.size() - 1).getDistance())
                && workout.getDistance() != 0.0;

        //We can use one of the two booleans to determine the workout type.

        if(timeTEL || distanceTEL) {
            if (timeTEL) {
                if (timeTotal > (300 * size - 1) && timeTotal < (300 * size)) {
                    intervalList = justRowCorrect(intervalList, timeTotal);
                } else {
                    intervalList = timeCorrect(intervalList, timeAVGI);
                }
            } else {
                intervalList = distanceCorrect(intervalList, distanceAVGI);
            }
            return new Workout(intervalList,workout.getSPM(),workout.getDistance(),workout.getTime(),workout.getWorkoutTime());
        }

        //Ok, so we don't have it confirmed that the total and the last added up. Lets see if the
        //other column is cumulative. This requires n+2 correct ocrs
        //Check if the other adds up to the total

        Double timeSum = 0.0,distanceSum = 0.0;
        for(Interval i : intervalList){
            timeSum += i.getTime();
            distanceSum += i.getDistance();
        }

        //xTEC = x total equals cumulative
        boolean timeTEC = workout.getTime().equals(timeSum);
        boolean distanceTEC = workout.getDistance().equals(distanceSum);

        if(timeTEC && (distanceAVGI !=0.0) || (distanceTEC && (timeAVGI != 0.0))){
            if(timeTEC){
                distanceCorrect(intervalList,distanceAVGI);
            } else {
                if(timeTotal > (300 * size - 1) && timeTotal < (300 * size)){
                    justRowCorrect(intervalList,timeTotal);
                } else {
                    intervalList = timeCorrect(intervalList, timeAVGI);
                }
            }
            return new Workout(intervalList,workout.getSPM(),workout.getDistance(),workout.getTime(),workout.getWorkoutTime());
        }

        //Right, now we are getting desperate. Lets see what the most common interval difference
        //exists. Hopefully it will match one of the averages
        //requires at least 3 correct ocrs... but it is ropey as hell.



        //xFREI = x most frequent interval
        Double timeFREI = mostFrequentTimeInterval(intervalList);
        Double distanceFREI = mostFrequentDistanceInterval(intervalList);

        //xIE = x intervals equal
        boolean timeII = timeAVGI.equals(timeFREI);
        boolean distanceII = distanceAVGI.equals(distanceFREI);

        if(timeII && (timeAVGI !=0.0) || (distanceTEC && (distanceAVGI != 0.0))){
            if(distanceII){
                distanceCorrect(intervalList,distanceAVGI);
            } else {
                if(timeTotal > (300 * size - 1) && timeTotal < (300 * size)){
                    justRowCorrect(intervalList,timeTotal);
                } else {
                    intervalList = timeCorrect(intervalList, timeAVGI);
                }
            }
            return new Workout(intervalList,workout.getSPM(),workout.getDistance(),workout.getTime(),workout.getWorkoutTime());
        }

        //OK, none of that worked. We can do even more rope ass stuff.... but... probably not wise.
        //at this point throw up and give up

        throw new CantDecipherWorkoutException();

    }

    public Workout correctForJustRow(Workout workout){
        Interval totals = new Interval(workout.getTime(),workout.getDistance(),workout.getSPM(),0.0);
        List<Interval> intervalList = workout.getIntervalList();
        correctForJustRow(totals, intervalList);
        return new Workout(intervalList,totals.getSPM(),totals.getDistance(),totals.getTime(),workout.getWorkoutTime());
    }

    private void correctForJustRow(Interval totals,List<Interval> intervals) {
        int size = intervals.size();
        Interval last = intervals.get(size - 1);
        Double totalTime = totals.getTime();
        Interval current;

        //set all non-final intervals time to 300s

        for (int i = 0; i < size - 1; i++) {
            current = intervals.get(i);
            //Just Row time is 5mins for all sub inters
            current.setWorkTime(300.0);
        }
        //get the total time from regular intervals
        Double regularIntevalsTT = (size - 1) * 300.0;


        //Check whether last time or totals time are obviously unreasonable
        boolean totalTimeReasonable = Math.abs(totalTime - regularIntevalsTT) < 300.0;
        boolean lastTimeReasonable = Math.abs(last.getTime() - regularIntevalsTT) < 300.0;
        //Check if both times are equal and non zero
        boolean timesEqual = totalTime != 0.0 && totalTime == last.getTime();
        //Set the last time correctly and get out
        if (timesEqual) {
            last.setWorkTime(totalTime - regularIntevalsTT);
            return;
        }

        /*Going to have to make a call about which to go with:
        At this point:
        xy, x is first, y is last
        TT -> Do nothing, no way of knowing
        TF -> Assume total is right
        FT -> Assume final is right
        FF -> Do nothing, no way of knowing
         */

        if (totalTimeReasonable) {
            if (lastTimeReasonable) {
                //TT
                return;
            } else {
                //TF
                last.setWorkTime(totalTime - regularIntevalsTT);
                return;
            }
        } else {
            if (lastTimeReasonable) {
                //FT
                totals.setWorkTime(last.getTime());
                last.setWorkTime(last.getTime() - regularIntevalsTT);
                return;
            } else {
                //FF
                return;
            }
        }

    }


    public Interval getInterval(Vector<String> vs){
        Double workTime;
        Integer spm,distance;

        boolean empty1 = false;
        boolean emptyC;
        for(int i = 0; i < vs.size();i++){
            emptyC = replaceWhiteSpace(vs.get(i)).isEmpty();
            if(emptyC && empty1)
                return null;
            empty1 = empty1 || emptyC;
        }


        distance =  getDistance(vs.get(1));
        if(vs.size() > 3)
            spm = getSPM(vs.get(3));
        else
            spm = null;
        workTime = getDoubleProto(vs.get(0));

        spm = spm != null ? spm: 0;
        distance = distance != null?distance:0;

        Interval result = new Interval(workTime,distance.doubleValue(),spm,0.0);
        return result;
    }

    public Double getDoubleProto(String sS){
        sS = replaceWhiteSpace(sS);
        Double result;
        try {
            result = ErgoFormatter.parseSeconds(sS);
        }catch(NotHumanStringException e){
            result = 0.0;
        }
        return result;
        //[\d:][\d{1,2}:]\d{1,2}.\d
    }

    public Double getTime(String sS){
        Double result = null;
        try{
            sS = removeConcurrentWhiteSpace(sS);
            result = ErgoFormatter.parseSeconds(sS);
        }catch(NotHumanStringException e){
            Log.w("Cannot Parse Time", "Attempting error correction");
        }
//
//        String[] spaceSplit = sS.split("[\\.:]");
//        String[] periodSplit = sS.split("\\.");
//        String milliS,hmsS,lt2S;
//        Integer hI,mI,sI,milI;
//        if(periodSplit.length == 2){
//            milliS = periodSplit[1];
//            hmsS = periodSplit[0];
//        } else if(periodSplit.length < 2){
//            lt2S = sS;
//        } else {
//
//        }
        //2 parse on . if >2, problem, if <2 problem
        return result;
    }

    public Integer getIntProto(String sS,int n,int m){
        sS = replaceWhiteSpace(sS);
        sS = colonPeriodConvert(sS);
        //catch digits
        String regex;
        if(m < n){
            regex = "[\\d:\\.]"+"{"+n+",}";
        } else {
            regex = "[\\d:\\.]"+"{"+n+","+m+"}";

        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sS);

        Integer result = null;
        if(matcher.find()) {
            sS = matcher.group();
            result = Integer.valueOf(sS);
            return result;
        }
        return result;
    }

    public Integer getSPM(String spmS){
        return getIntProto(spmS,1,2);
    }

    public Integer getDistance(String distS){
        return getIntProto(distS,1,0);
    }

    public Integer getCalories(String calS){
        return getIntProto(calS,1,0);
    }

    private String colonPeriodConvert(String s){
        s = s.replaceAll(":","1");
        s = s.replaceAll("\\.","0");
        return s;
    }

    private String replaceWhiteSpace(String sS){
        sS = sS.replaceAll("\\s", "");
        return sS;
    }

    private String removeConcurrentWhiteSpace(String sS){
        sS = sS.replaceAll("\\s{2,}","");
        return sS;
    }

    /* Bolder workout code ends here

     */


}
