package com.example.mapinguari.workoutclass.exerciseObjects;

import android.util.Log;

/**
 * Created by mapinguari on 8/6/15.
 */
public abstract class PerformanceMeasure {

    abstract public Double getDistance();
    abstract public Double getTime();
    abstract public Integer getSPM();
    abstract public Double getRest();


    public String showHumanTime(){
        return ErgoFormatter.formatSeconds(getTime());
    }

    public String showHumanRestTime(){
        return ErgoFormatter.formatSeconds(getRest());
    }

    public String showHumanSplit(){
        Log.w("coming out ", getSplit().toString());
        return ErgoFormatter.formatSeconds(getSplit());
    }

    public String showHumanDistance(){
        return Integer.toString(getDistance().intValue());
    }

    public String showHumanSPM(){
        return Integer.toString(getSPM());
    }
    //Default methods

    //POWER MEASURES

    //s/500m
    public Double getSplit() {
        return ((500 * getTime()) / getDistance());
    }

    //m/s
    public Double getSpeed(){
        return getDistance() / getTime();
    }

    //J/s
    public Double getWatts(){
        return (2.8*(Math.pow(getDistance()/500*getTime(),3)));
    }

    //J/str
    public Double energyPerStroke(){
        return (getWatts()*60)/getSPM();
    }

    public Double getDistancePerStroke(){
        return (60*getDistance())/(getSPM()*getTime());
    }

    //HUMAN INTERFACE STUFF

    public String getHumanSplit(){
        return PerformanceMeasure.splitToString(getSplit());
    }

    public String getHumanTime(){
        return PerformanceMeasure.timeToString(getTime());
    }

    public static String timeToString(Double time){
        double hours = time / 3600;
        double hoursRem = time % 3600;
        double mins = hoursRem / 60;
        double minsRem = hoursRem % 60;
        double secs = Math.floor(minsRem);
        double a,b,c;
        if (hours > 0) {
            a = hours;
            b = mins;
            c = secs;
        }
        else {
            a = mins;
            b = secs;
            c = Math.round((secs % 1) * 10);
        }
        return ((int) a + ":" + (int) b + "." + (int) c);
    }


    public static String splitToString(Double split){
        int mins = (int) Math.floor(split / 60);
        double secsRem = split % 60;
        int secs = (int) Math.floor(secsRem);
        int milli = (int) Math.round((secsRem % 1) * 10);
        return (Integer.toString(mins) + ":" + Integer.toString(secs) + "." + Integer.toString(milli));
    }



}
