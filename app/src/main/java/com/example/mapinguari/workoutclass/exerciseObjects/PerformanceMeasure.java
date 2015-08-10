package com.example.mapinguari.workoutclass.exerciseObjects;

/**
 * Created by mapinguari on 8/6/15.
 */
public abstract class PerformanceMeasure {

    abstract public Double getDistance();
    abstract public Double getTime();
    abstract public Integer getSPM();

    //Default methods
        /*
    public Integer getWorkMins(){
        return (int) Math.floor(workTime / 60);
    }

    public Integer getWorkSecs(){
        double secsRem = workTime % 60;
        int secs = (int) Math.floor(secsRem);
        return secs;
    }

    public Integer getWorkCenti(){
        double secsRem = workTime % 60;
        int milli = (int) Math.round((secsRem % 1) * 10);
    }

    public void setWorkMins(Integer x){
        this.workTime = workTime - getWorkMins()*60 + x;
    }

    public void setWorkSecs(Integer x){
        this.workTime = workTime - getWorkSecs() + x;
    }

    public void setWorkCenti(Integer x){
        this.workTime = workTime - (getWorkCenti() / 10) + x;
    }
        */
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
