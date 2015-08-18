package com.example.mapinguari.workoutclass.exerciseObjects;

import android.util.Log;

import java.util.Formatter;

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
        return ErgoFormatter.formatSeconds(getSplit());
    }

    public String showHumanDistance(){
        return Integer.toString(getDistance().intValue());
    }

    public String showHumanSPM(){
        return Integer.toString(getSPM());
    }

    public String showHumanDistancePerStroke(){
        String out = String.format("%.1f",getDistancePerStroke());
        return out;
    }

    public String showHumanEnergyPerStroke() {
        String out = String.format("%.1f", energyPerStroke());
        return out;
    }

    public String showHumanWatts(){
        String out = String.format("%.1f", getWatts());
        return out;
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
        return ErgoFormatter.formatSeconds(getSplit());
    }

    /*
    static public Double splitToWatts(Double split){

    }

    static public Double wattsToEpStr(Double watts){

    }

    static public Double EpStrToDps(Double epStr){

    }

    static public Double dpsTosplit(Double dps){

    }
*/
}
