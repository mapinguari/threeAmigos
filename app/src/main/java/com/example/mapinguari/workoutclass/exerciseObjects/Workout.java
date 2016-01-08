package com.example.mapinguari.workoutclass.exerciseObjects;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by mapinguari on 7/24/15.
 */
public class Workout extends PerformanceMeasure implements Parcelable {

    //fields
    private GregorianCalendar workoutTime;
    private Double distance;
    private Double totalTime;
    private Integer averageSPM;
    private List<Interval> intervalList;

    private enum WorkoutType {
        JustRow,Time,Distance;
    }

    //constructors
    public Workout(List<Interval> intervalList, Integer averageSPM, Double distance, Double totalTime, GregorianCalendar workoutTime) {
        this.intervalList = intervalList;
        this.averageSPM = averageSPM;
        this.distance = distance;
        this.totalTime = totalTime;
        this.workoutTime = workoutTime;
    }
    public Workout(Parcel workout){
        this.readFromParcel(workout);
    }

    //methods
    public String getHumanDate(Context context){
        java.text.DateFormat df = DateFormat.getMediumDateFormat(context);
        String out = df.format(workoutTime.getTime());
        return out;
    }

    public String getHumanTime(Context context){
        java.text.DateFormat tf = DateFormat.getTimeFormat(context);
        String out = tf.format(workoutTime.getTime());
        return  out;
    }

    @Override
    public Double getTime() {
        return totalTime;
    }

    @Override
    public Integer getSPM() {
        return averageSPM;
    }

    public Double getDistance(){
        return distance;
    }

    public Double getRest(){ return getTotalRest();}

    public GregorianCalendar getWorkoutTime() {
        return workoutTime;
    }

    public Interval getLastInterval(){
        return intervalList.get(intervalList.size()-1);
    }

    public void setWorkoutTime(GregorianCalendar workoutTime) {
        this.workoutTime = workoutTime;
    }


    public void setSPM(Integer averageSPM) {
        this.averageSPM = averageSPM;
    }


    public void setdistance(Double distance) {
        this.distance = distance;
    }

    public void setTotalTime(Double totalTime) {
        this.totalTime = totalTime;
    }

    public List<Interval> getIntervalList() {
        return intervalList;
    }

    public Double getTotalRest(){
        return totalRestTime(this.intervalList);
    }

    //TODO: write verification function. i.e one that checks that the header workout data matches the intervals workout data
    static public double totalTime(List<Interval> iL){

        double sum = 0;

        for(Interval i : iL){
            sum += i.getTime();
        }
        return sum;
    }

    static public double totalDistance(List<Interval> iL){
        double sum = 0;
        for(Interval i : iL){
            sum += i.getDistance();
        }
        return sum;
    }

    static public int averageSPM(List<Interval> iL){
        int sum = 0;

        for(Interval i : iL){
            sum += i.getSPM();
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


    public WorkoutType getWorkoutType(){
        WorkoutType result;
        if(isDistance()) {
            result = WorkoutType.Distance;
        } else {
            if(isJustRow()){
                result = WorkoutType.JustRow;
            } else{
                result = WorkoutType.Time;
            }
        }
        return result;
    }

    private boolean isTime(){
        return totalTime.equals(Workout.totalTime(intervalList));
    }

    private boolean isDistance(){
        return distance.equals(Workout.totalDistance(intervalList));
    }

    private boolean isJustRow(){
        return !intervalList.get(0).getTime().equals(this.getLastInterval().getTime()) && isTime();
    }

    public double percentCorrect(Workout correctWorkout){
        double a = totalTime.equals(correctWorkout.getTime())?1:0;
        double b = distance.equals(correctWorkout.getDistance())?1:0;
        double c = averageSPM.equals(correctWorkout.getSPM())?1:0;
        List<Interval> correctIntervalList = correctWorkout.getIntervalList();

        double sum=0;
        int noOfInts;



        for (noOfInts = 0; noOfInts < intervalList.size() && noOfInts < correctIntervalList.size(); noOfInts++) {
            sum += intervalList.get(noOfInts).percentageCorrect(correctIntervalList.get(noOfInts));
        }

        return (sum*4 + a + b + c) / ((correctIntervalList.size()*4) + 3);
    }

    // Parcelable code here


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(workoutTime);
        dest.writeDouble(distance);
        dest.writeDouble(totalTime);
        dest.writeInt(averageSPM);
        dest.writeList(intervalList);
    }

    public void readFromParcel(Parcel in){
        workoutTime = (GregorianCalendar) in.readSerializable();
        distance = in.readDouble();
        totalTime = in.readDouble();
        averageSPM = in.readInt();
        this.intervalList = new ArrayList<Interval>();
        in.readList(this.intervalList, Interval.class.getClassLoader());

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
                ", distance=" + distance +
                ", totalTime=" + totalTime +
                ", averageSPM=" + averageSPM +
                ", intervalList=" + intervalList +
                '}';
    }
}
