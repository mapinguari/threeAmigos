package com.example.mapinguari.workoutclass;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by mapinguari on 7/24/15.
 */
public class Interval implements Parcelable {

    // fields
    // active work fields
    private Double workTime;
    private Double averageWatts;
    private Integer averageSPM;

    //rest fields
    private Double restTime;

    //constructors

    public Interval(Double workTime, Double averageWatts, Integer averageSPM, Double restTime) {
        this.workTime = workTime;
        this.averageWatts = averageWatts;
        this.averageSPM = averageSPM;
        this.restTime = restTime;
    }

    public Interval(Parcel parcel){
        readFromParcel(parcel);
    }

    //this gets the current interval's split
    public String getSplit(){
        Double a = getExtSplit(this.averageWatts);
        String str = secondsToSplit(a);
        return str;
    }

    //this returns the human representation of the split given watts
    public static String calcSplit(Double watts){
        Double a = getExtSplit(watts);
        String str = secondsToSplit(a);
        return str;
    }

    //Given watts this returns the speed (m/s).
    public static Double getExtSplit(double watts){
        double a = 2.8/watts;
        double speed = 500 / Math.cbrt(a);
        return speed;
    }

    private static String secondsToSplit(double secondsT){
        //TODO: Change these doubles to ints
        int mins = (int) Math.floor(secondsT / 60);
        double secsRem = secondsT % 60;
        int secs = (int) Math.floor(secsRem);
        int milli = (int) Math.round((secsRem % 1) * 10);
        return (Integer.toString(mins) + ":" + Integer.toString(secs) + "." + Integer.toString(milli));
    }
    //public methods

    public Double getWorkTime() {
        return workTime;
    }

    public void setWorkTime(Double workTime) {
        this.workTime = workTime;
    }

    public Integer getAverageSPM() {
        return averageSPM;
    }

    public void setAverageSPM(Integer averageSPM) {
        this.averageSPM = averageSPM;
    }

    public Double getAverageWatts() {
        return averageWatts;
    }

    public void setAverageWatts(Double averageWatts) {
        this.averageWatts = averageWatts;
    }

    public Double getRestTime() {
        return restTime;
    }

    public void setRestTime(Double restTime) {
        this.restTime = restTime;
    }


    //Parcelable code


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(workTime);
        dest.writeDouble(averageWatts);
        dest.writeInt(averageSPM);
        dest.writeDouble(restTime);
    }

    public void readFromParcel(Parcel in){
        workTime = in.readDouble();
        averageWatts = in.readDouble();
        averageSPM = in.readInt();
        restTime = in.readDouble();
    }

    public static final Creator<Interval> CREATOR = new Parcelable.Creator<Interval>(){
        @Override
        public Interval createFromParcel(Parcel source) {
            return new Interval(source);
        }

        @Override
        public Interval[] newArray(int size) {
            return new Interval[size];
        }
    };

    @Override
    public String toString() {
        return "Interval{" +
                "workTime=" + workTime +
                ", averageWatts=" + averageWatts +
                ", averageSPM=" + averageSPM +
                ", restTime=" + restTime +
                '}';
    }
}
