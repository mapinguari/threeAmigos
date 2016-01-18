package com.example.mapinguari.workoutclass;

import android.util.Log;

import com.example.mapinguari.workoutclass.exceptions.NotHumanStringException;
import com.example.mapinguari.workoutclass.exerciseObjects.ErgoFormatter;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Created by mapinguari on 1/13/16.
 *
 * Code predominantly written by David
 */
public class ConservativeWorkout {

    public Workout conservativeWorkout(Vector<Vector<String>> vvs){
        Log.v("PAR", "parsing:" + (vvs.size()) + " rows");

        Interval totalsInterval = null;
        Interval currentInterval;
        ArrayList<Interval> intervals = new ArrayList<Interval>();
        Vector<String> currentRow;
        boolean[] missingTime=new boolean[vvs.size()],missingDistance=new boolean[vvs.size()],
                missingStrokes=new boolean[vvs.size()];

        Integer[] powers=new Integer[vvs.size()];

        Arrays.fill(missingTime, false);
        Arrays.fill(missingDistance, false);
        Arrays.fill(missingStrokes, false);

        for(int i = 0; i <vvs.size();i++){
            currentRow = vvs.get(i);
            Double time=null,distance;
            Integer spm;
            try{

                time = getTime(currentRow);
                distance = getDistance(currentRow);
                powers[i]=getPower(currentRow, time, distance);
                spm=getSPM(currentRow);

                currentInterval = new Interval(time!=null?time:0, distance!=null?distance:0,
                        spm!=null?spm:0,0.0);

                Log.v("PAR","row "+i+":"+ currentInterval.toString());

                if(correctHorizontal(currentInterval,time,distance,powers[i])){
                    if(time==null)
                        time=currentInterval.getTime();
                    else if (distance==null)
                        distance=currentInterval.getDistance();
                }

            } catch(Exception exception){
                break;
            }

            if (time==null) {
                missingTime[i]=true;
            }
            if (distance==null) {
                missingDistance[i]=true;
            }
            if (spm==null) {
                missingStrokes[i]=true;
            }

            if (i == 0) {
                totalsInterval = currentInterval;
            } else {
                intervals.add(currentInterval);
            }

        }

        while(correctVertical(totalsInterval,intervals,missingTime,missingDistance,missingStrokes)){

            correctHorizontal(totalsInterval,missingTime[0]?null:totalsInterval.getTime(),
                    missingDistance[0]?null:totalsInterval.getDistance(),powers[0]);

            for (int i = 0; i < intervals.size();i++) {
                correctHorizontal(intervals.get(i),missingTime[i+1]?null:intervals.get(i).getTime(),
                        missingDistance[i+1]?null:intervals.get(i).getDistance(),powers[i+1]);
            }
        }




        Workout result = null;
        if(totalsInterval != null) {
            result = new Workout(intervals, totalsInterval.getSPM(), totalsInterval.getDistance(), totalsInterval.getDistance(), new GregorianCalendar());
        }
        return result;
    }

    private boolean correctVertical(Interval totalsInterval, ArrayList<Interval> intervals,
                                    boolean[] missingTime, boolean[] missingDistance, boolean[] missingStrokes){
        boolean changed=false;

        int numberMissingTime=0,numberMissingDistance=0,numberMissingStrokes=0;
        double totalDistance=0.0,totalTime=0.0;
        int totalStrokes=0;
        int length=intervals.size()+1;

        if (totalsInterval==null||length==1)
            return false;


        for(int i=0;i<length;i++) {
            numberMissingTime+=missingTime[i]?1:0;
            numberMissingDistance+=missingDistance[i]?1:0;
            numberMissingStrokes+=missingStrokes[i]?1:0;

            if (i>1){
                totalTime+=intervals.get(i-1).getTime();
                totalDistance+=intervals.get(i-1).getDistance();
            }
        }

        if (numberMissingTime==1) {
            changed=true;
            if (missingTime[0]) {
                totalsInterval.setWorkTime(totalTime);
                missingTime[0]=false;
            } else {
                double time = totalsInterval.getTime() - totalTime;
                int i = 0;
                do {
                    if (missingTime[++i]) {
                        intervals.get(i - 1).setWorkTime(time);
                        missingTime[i]=false;
                    }
                } while (!missingTime[i]);
            }
        }

        if (numberMissingDistance==1) {
            changed=true;
            if (missingDistance[0]) {
                totalsInterval.setDistance(totalDistance);
                missingDistance[0]=false;
            }else {
                double distance = totalsInterval.getDistance() - totalDistance;
                int i = 0;
                do {
                    if (missingDistance[++i]) {
                        intervals.get(i - 1).setDistance(distance);
                        missingDistance[i]=false;
                    }
                } while (!missingDistance[i]);
            }
        }

        for (int i=1;i<length;i++){
            totalStrokes+=(!missingStrokes[i]&&!missingTime[i])?
                    intervals.get(i-1).getSPM()*intervals.get(i-1).getTime() : 0;
        }

        if (numberMissingStrokes==1&&numberMissingTime<2) {
            if (missingStrokes[0]) {
                if (totalsInterval.getTime()!=0) {
                    changed = true;
                    missingStrokes[0] = false;
                    totalsInterval.setAverageSPM((int) (totalStrokes / totalsInterval.getTime()));
                }
            } else {
                int i = 0;
                double strokes = totalsInterval.getSPM() * totalsInterval.getTime() - totalStrokes;
                do {
                    i++;
                    if (missingStrokes[i] && intervals.get(i - 1).getTime() != 0.0) {
                        changed = true;
                        intervals.get(i - 1).setAverageSPM((int) (strokes / intervals.get(i - 1).getTime()));
                    }
                } while (!missingStrokes[i]);
                missingStrokes[i] = false;
            }
        }

        return changed;
    }

    private boolean correctHorizontal(Interval interval, Double time, Double distance, Integer power){
        boolean changed=false;
        if (time==null&&(distance!=null&&power!=null)) {
            interval.setWorkTime(Math.pow((2.8 / (double)power),1.0/3.0)*distance);
            changed=true;
        }
        else if (distance==null&&(time!=null&&power!=null)){
            interval.setDistance(time/Math.pow((2.8 / (double)power),1.0/3.0));
            changed=true;
        }


        return changed;
    }

    public Double getTime(Vector<String> vs){
        Double secs;
        String time="";
        try{
            time = vs.get(0);
            secs = ErgoFormatter.parseSeconds(time);
        } catch(NotHumanStringException e){
            secs=null;
            e.printStackTrace();
            Log.w("time string parse fail", time);
        } catch (ArrayIndexOutOfBoundsException e){
            secs=null;
            e.printStackTrace();
            Log.w("time string parse fail", time);
        }

        return secs;
    }

    public Double getDistance(Vector<String> vs){
        Double dist;
        String distance="";
        try {
            distance = vs.get(1);
            dist = Double.parseDouble(distance);
        } catch(NumberFormatException e){
            dist=null;
            e.printStackTrace();
            Log.w("dist string parse fail", distance);
        } catch (ArrayIndexOutOfBoundsException e){
            dist=null;
            e.printStackTrace();
            Log.w("dist string parse fail", distance);
        }
        return dist;
    }

    public Integer getPower(Vector<String> vs, Double time, Double distance){
        Integer power;
        String pow="";
        try {
            pow = vs.get(2);
            power = Integer.parseInt(pow);
        } catch(NumberFormatException e){
            if (time != null && distance != null &&distance!=0) {
                power = (int) (2.8 * Math.pow(time / distance, 3.0));
            } else {
                power = null;
            }
            e.printStackTrace();
            Log.w("dist string parse fail", pow);
        } catch (ArrayIndexOutOfBoundsException e){
            power = null;
            e.printStackTrace();
            Log.w("dist string parse fail", pow);
        }
        return power;
    }

    public Integer getSPM(Vector<String> vs){
        Integer spm;
        String spmS="";
        try {
            spmS = vs.get(3);
            spm=Integer.parseInt(spmS);
        } catch (NumberFormatException e) {
            spm=null;
            e.printStackTrace();
            Log.w("SPM string parse fail", spmS);
        } catch (ArrayIndexOutOfBoundsException e){
            spm=null;
            e.printStackTrace();
            Log.w("SPM string parse fail", spmS);
        }
        return spm;
    }

}
