package com.example.mapinguari.workoutclass;

import android.util.Log;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.exceptions.CantDecipherWorkoutException;
import com.example.mapinguari.workoutclass.exceptions.NotHumanStringException;
import com.example.mapinguari.workoutclass.exerciseObjects.ErgoFormatter;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.lang.reflect.Array;
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

    int NUMBER_OF_EMPTY_CELLS_ALLOWED_PER_ROW = 2;

    String anyDigRegex = "[0-9]";
    String anyOCRRegex = "[0-9\\.:]";
    String minDecRegex = "[1-5]";
    String secDecRegex = "[0-5]";



    //We can do some stuff with the digits here, for example, the first \d char will be less than 6
    public Pattern prettyDesperateRegex = Pattern.compile("([0-9\\.:]{1,2}:)?([0-9\\.:]?[0-9\\.:])?:[0-9\\.:]{2}\\.[0-9\\.:]");
    public Pattern basicTimeRegex = Pattern.compile("(\\d{1,2}:)?(\\d{2})?:\\d{2}\\.\\d");
    public Pattern betterTimeRegex = Pattern.compile("([0-9]{1,2}:)?([0-5]?[0-9])?:[0-5][0-9]\\.[0-9]");
    public Pattern desperateTimeRegex = Pattern.compile(anyOCRRegex+"{9,10}|"+ anyOCRRegex + "{5,7}");

    public Pattern distanceRegex = Pattern.compile("[0-9]{1,6}");

    public Pattern SPMRegex = Pattern.compile("[0-9]{1,2}");

    public Pattern CalRegex = Pattern.compile("[0-9]{1,4}");
    public Pattern PowerRegex = Pattern.compile("[0-9]{1,4}");


//    private String idealTimeRegex(String string){
//        Matcher timeMatcher = betterTimeRegex.matcher(string);
//        if(timeMatcher.find()){
//
//        }
//    }

    private String length5(String string){
        string = colonPeriodConvert(string);
        String result = ":" + string.substring(1,2) + "." + string.charAt(4);
        return result;
    }

    private String length6(String string){
        String result;
        char min1 = string.charAt(0);
        if(min1 ==  ':' || min1 == '.')
            min1 = fixChar(min1);
        result = min1 + length5(string.substring(1));
        return result;
    }

    private String length7(String string){
        String result;
        char min2 = string.charAt(0);
        if(min2 == ':' || min2 == '.' )
            min2 = fixChar(min2);
        result = min2 + length6(string.substring(1));
        return result;
    }

    private String length9(String string){
        String result;
        char h1 = string.charAt(0);
        if(h1== ':' || h1 == '.' )
            h1 = fixChar(h1);
        result = h1 + ':' + length7(string.substring(2));
        return result;
    }

    private String length10(String string){
        String result;
        char h2 = string.charAt(0);
        if(h2== ':' || h2 == '.' )
            h2 = fixChar(h2);
        result = h2 + length9(string.substring(2));
        return result;
    }

    private char fixChar(char c){
        switch(c){
            case ':' : c = 1;
                break;
            case '.' : c = 1;
                break;
            case '6' : c = 5;
                break;
            case '7' : c = 2;
                break;
            case '8' : c = 3;
                break;
            case '9' : c = 5;
                break;
            case '0' : c = 3;
                break;
        }
        return c;
    }

    public Workout bolderWorkout(Vector<Vector<String>> vvs,String workoutType){

        List<Interval> intervalListTemp = new ArrayList<Interval>(vvs.size() - 1);
        Interval totalsInterval,curInterval;
        Workout result;

        totalsInterval = getInterval(vvs.get(0));

        //Clear inappropriate rows
        vvs = clearInappropriate(vvs,NUMBER_OF_EMPTY_CELLS_ALLOWED_PER_ROW);
        //This currently leaves us with a number of column cells possibly not equal to 4
        //With no cells of all punctuation or white space.
        //At this point probably try to seperate out cells again if you can try.
        //I.e I think we need to write some pattern to check if the data is in a given form.
        // At this point I want to try and take the last two digits of the last cell if it looks
        //like it could be spm



        for(int i = 1; i< vvs.size(); i++){
            curInterval = getInterval(vvs.get(i));
            intervalListTemp.add(curInterval);
        }



        result = new Workout(intervalListTemp,totalsInterval.getSPM(),totalsInterval.getDistance(),totalsInterval.getTime(),new GregorianCalendar());

        result = correctSPM(result);

        boolean isTime = getDoubleProto(workoutType) != null;
        boolean isDistance = getDistance(workoutType) != null;


        //HERE THIS IS GOING TO BE A NIGHTMARE. It is possible I have no information about the interval sizes, so I wont be able to determine if it is
        //JustRow or a time workout. For the moment, I am not going to tackle this.
//        if( isDistance){
//            if(isDistance){
//                result = distanceCorrect(result,getDistance(workoutType));
//            }
//            //This is where I need to decide how to deal with time and justRow
//            if(isTime){
//            }
//
//        } else {

            try {
                result = workoutDecipher(result);
            } catch (CantDecipherWorkoutException e) {
                e.printStackTrace();
            }
//        }
        return result;
    }


    public Workout bolderWorkout(Vector<Vector<String>> vvs){
        return bolderWorkout(vvs,"");
    }


    /**
     * function to clear inappropriate rows from the VVS returned from the OCR
     * @param vvs vector string vector
     * @param numRej the number of cells to require to be empty before the row is rejected
     * @return the new VVS
     */
    private Vector<Vector<String>> clearInappropriate(Vector<Vector<String>> vvs,int numRej){
        int noECells = 0;
        Vector<String> vs;

        for(int i = 0; i<vvs.size();i++) {
            vs = vvs.get(i);
            for (String s : vs) {
                if (!containsSomeData(s)) {
                    noECells += 1;
                }
            }
            if (noECells >= numRej) {
                vvs.remove(i);
            }

        }

        return vvs;
    }


    /**
     * @return true if has some digits in, false otherwise
     */
    private boolean containsSomeData(String s){
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }

    public Interval getInterval(Vector<String> vs){
        Double workTime;
        Integer spm,distance;

        Interval result = new Interval(0.0,0.0,0,0.0);

        //Wrong number of columns.
        //no data in some columns
        //currently resort to null interval

        boolean empty1 = false;
        boolean emptyC;
        for(int i = 0; i < vs.size();i++){
            emptyC = replaceWhiteSpace(vs.get(i)).isEmpty();
            if(emptyC && empty1)
                return null;
            empty1 = empty1 || emptyC;
        }

        if(vs.size() != 4 || empty1){

        } else {
            workTime = getDoubleProto(vs.get(0));
            distance = getDistance(vs.get(1));
            spm = getSPM(vs.get(3));




            spm = spm != null ? spm : 0;
            distance = distance != null ? distance : 0;
            workTime = workTime != null ? workTime : 0.0;

            result = new Interval(workTime, distance.doubleValue(), spm, 0.0);
        }
        return result;
    }


    /*Bolder workout code begins here

     */


    //WE can introduce an integer to denote how aggressively we wish to try and make data appear in
    // the cells. here is a prime example where we could try too aggresively to fit data.

    //WE also need to think about what to do about numbers that singularly seem horrifically out.
    //Should we try and correct that?!

    private Workout correctSPM(Workout workout){
        Integer spm = workout.getSPM();
        List<Interval> intervalList = workout.getIntervalList();
        Interval curInterval;

        Integer curInte;
        int noOfZeros = 0,noOfRows = intervalList.size();
        double intSum = 0;
        double replacementSPM = 0;
        int realreplacementSPM;



        List<Integer> sspm = new ArrayList<Integer>(intervalList.size());
        List<Integer> zeros = new ArrayList();

        for(int i = 0;i <intervalList.size();i++){
            curInte = intervalList.get(i).getSPM();
            sspm.add(curInte);
            if(curInte == 0) {
                zeros.add(i);
                noOfZeros++;
            } else {
                intSum +=curInte;
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
            workout.setSPM(realreplacementSPM);

        for(Integer i : zeros){
            curInterval = intervalList.get(i);
            curInterval.setAverageSPM(realreplacementSPM);
        }
        return workout;
    }




    private List<Interval> justRowCorrect(List<Interval> intervals, Double total){
        int n = intervals.size();
        for(int i = 0; i <n-1;i++){
            intervals.get(i).setWorkTime(300.0);
        }
        intervals.get(n-1).setWorkTime(total - (300*(n-1)));
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

    private Workout distanceCorrect(Workout workout,Integer dist) {

        List<Interval> lI = workout.getIntervalList();
        int numOfIntervals = lI.size();
        double intervalDist = dist / numOfIntervals;
        workout.setIntervalList(distanceCorrect(lI,intervalDist));
        return workout;
    }

    private Workout timeCorrect(Workout workout,Double time) {

        List<Interval> lI = workout.getIntervalList();
        int numOfIntervals = lI.size();
        double intervalTime = time / numOfIntervals;
        workout.setIntervalList(timeCorrect(lI,intervalTime));
        return workout;
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
                if (timeTotal > (300 * (size - 1)) && timeTotal < (300 * size)) {
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
                if(timeTotal > (300 * (size - 1)) && timeTotal < (300 * size)){
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
                if(timeTotal > (300 * (size - 1)) && timeTotal < (300 * size)){
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





    public Double getDoubleProto(String sS){
        Double result = null;
        String timeString = getTimeString(sS);
        try {
            result = ErgoFormatter.parseSeconds(timeString);
        }catch(Exception e){
            e.printStackTrace();
            Log.w("Couldn't get time", sS);
        }
        return result;
    }



    public String getTimeString(String sS){
        String result = null;
        Matcher matcher = betterTimeRegex.matcher(sS);
        if(matcher.find()){
            sS = matcher.group();
            result = sS;
        } else {
            matcher = prettyDesperateRegex.matcher(sS);
            if (matcher.find()) {
                sS = matcher.group();
                switch (sS.length()){
                    case 5 : result = length5(sS);
                        break;
                    case 6 : result = length6(sS);
                        break;
                    case 7 : result = length7(sS);
                        break;
                    case 9 : result = length9(sS);
                        break;
                    case 10 : result = length10(sS);
                        break;
                }
            }
        }
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

    /* Bolder workout code ends here

     */

    //    public Workout correctForJustRow(Workout workout){
//        Interval totals = new Interval(workout.getTime(),workout.getDistance(),workout.getSPM(),0.0);
//        List<Interval> intervalList = workout.getIntervalList();
//        correctForJustRow(totals, intervalList);
//        return new Workout(intervalList,totals.getSPM(),totals.getDistance(),totals.getTime(),workout.getWorkoutTime());
//    }
//
//    private void correctForJustRow(Interval totals,List<Interval> intervals) {
//        int size = intervals.size();
//        Interval last = intervals.get(size - 1);
//        Double totalTime = totals.getTime();
//        Interval current;
//
//        //set all non-final intervals time to 300s
//
//        for (int i = 0; i < size - 1; i++) {
//            current = intervals.get(i);
//            //Just Row time is 5mins for all sub inters
//            current.setWorkTime(300.0);
//        }
//        //get the total time from regular intervals
//        Double regularIntevalsTT = (size - 1) * 300.0;
//
//
//        //Check whether last time or totals time are obviously unreasonable
//        boolean totalTimeReasonable = Math.abs(totalTime - regularIntevalsTT) < 300.0;
//        boolean lastTimeReasonable = Math.abs(last.getTime() - regularIntevalsTT) < 300.0;
//        //Check if both times are equal and non zero
//        boolean timesEqual = totalTime != 0.0 && totalTime == last.getTime();
//        //Set the last time correctly and get out
//        if (timesEqual) {
//            last.setWorkTime(totalTime - regularIntevalsTT);
//            return;
//        }
//
//        /*Going to have to make a call about which to go with:
//        At this point:
//        xy, x is first, y is last
//        TT -> Do nothing, no way of knowing
//        TF -> Assume total is right
//        FT -> Assume final is right
//        FF -> Do nothing, no way of knowing
//         */
//
//        if (totalTimeReasonable) {
//            if (lastTimeReasonable) {
//                //TT
//                return;
//            } else {
//                //TF
//                last.setWorkTime(totalTime - regularIntevalsTT);
//                return;
//            }
//        } else {
//            if (lastTimeReasonable) {
//                //FT
//                totals.setWorkTime(last.getTime());
//                last.setWorkTime(last.getTime() - regularIntevalsTT);
//                return;
//            } else {
//                //FF
//                return;
//            }
//        }
//
//    }
//

}
