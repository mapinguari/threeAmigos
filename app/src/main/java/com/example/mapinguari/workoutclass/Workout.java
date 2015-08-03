package com.example.mapinguari.workoutclass;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by mapinguari on 7/24/15.
 */
public class Workout implements Parcelable {

    //fields
    private GregorianCalendar workoutTime;
    private Double averageWatts;
    private Double totalTime;
    private Integer averageSPM;
    private List<Interval> intervalList;

    //constructors
    public Workout(List<Interval> intervalList, Integer averageSPM, Double averageWatts, Double totalTime, GregorianCalendar workoutTime) {
        this.intervalList = intervalList;
        this.averageSPM = averageSPM;
        this.averageWatts = averageWatts;
        this.totalTime = totalTime;
        this.workoutTime = workoutTime;
    }
    public Workout(Parcel workout){
        this.readFromParcel(workout);
    }

    //methods

    public Integer getDistance(){
        double split = Interval.getExtSplit(this.averageWatts);
        double speed = 500/split;
        //TODO: NOT SURE WHAT THE ERGO DOES HERE FOR THE ACTUAL METERS (ie Math.Floor, Math.Round etc etc)
        return (int) (totalTime*speed);
    }


    public GregorianCalendar getWorkoutTime() {
        return workoutTime;
    }

    public void setWorkoutTime(GregorianCalendar workoutTime) {
        this.workoutTime = workoutTime;
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

    public Double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Double totalTime) {
        this.totalTime = totalTime;
    }

    public List<Interval> getIntervalList() {
        return intervalList;
    }

    //TODO: write verification function. i.e one that checks that the header workout data matches the intervals workout data
    static public double totalTime(List<Interval> iL){

        double sum = 0;

        for(Interval i : iL){
            sum += i.getWorkTime();
        }
        return sum;
    }

    static public double averageWatts(List<Interval> iL){
        double sum = 0;

        for(Interval i : iL){
            sum += i.getAverageWatts();
        }
        return (sum / iL.size());
    }

    static public int averageSPM(List<Interval> iL){
        int sum = 0;

        for(Interval i : iL){
            sum += i.getAverageSPM();
        }
        return (int) Math.floor(sum / iL.size());
    }

    static public double totalRestTime(List<Interval> iL){

        double sum = 0;

        for(Interval i : iL){
            sum += i.getRestTime();
        }
        return sum;
    }



    // Parcelable code here


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(workoutTime);
        dest.writeDouble(averageWatts);
        dest.writeDouble(totalTime);
        dest.writeInt(averageSPM);
        dest.writeList(intervalList);
    }

    public void readFromParcel(Parcel in){
        workoutTime = (GregorianCalendar) in.readSerializable();
        averageWatts = in.readDouble();
        totalTime = in.readDouble();
        averageSPM = in.readInt();
        //TODO: Not sure here if this class loader (null) is acceptable
        in.readList(this.intervalList,null );
    }

    public static final Creator<Workout> CREATOR = new Parcelable.Creator<Workout>(){
        @Override
        public Workout createFromParcel(Parcel source) {
            return new Workout(source);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };

    @Override
    public String toString() {
        return "Workout{" +
                "workoutTime=" + workoutTime +
                ", averageWatts=" + averageWatts +
                ", totalTime=" + totalTime +
                ", averageSPM=" + averageSPM +
                ", intervalList=" + intervalList +
                '}';
    }
}
