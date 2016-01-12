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
import java.util.Vector;

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

    public enum thirdColumnType {
        SPLIT,WATTS,CALORIES
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
        return intervalList.get(intervalList.size() - 1);
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

    private Vector<String> valuesToVS(double time, double distance, thirdColumnType type, int SPM){
        Vector<String> returned=new Vector<String>();
        
        returned.add(ErgoFormatter.formatSeconds(time));
        returned.add(String.valueOf(distance));

        double split=500*time/distance;
        double watts=2.8/Math.pow(time/distance,3);
        double calories=((4*(watts*time)/1000+0.35*time)/4.2)*3600/time;

        switch (type) {
            case SPLIT:
                returned.add(ErgoFormatter.formatSeconds(split));
                break;
            case WATTS:
                returned.add(Integer.toString((int)Math.round(watts)));
                break;
            case CALORIES:
                returned.add(Integer.toString((int)Math.round(calories)));
                break;
        }
        returned.add(String.valueOf(SPM));
        return returned;

    }

    public Vector<Vector<String>> toVVS(thirdColumnType type) {
        Vector<Vector<String>> returned=new Vector<Vector<String>>();
        returned.add(valuesToVS(this.getTime(),this.getDistance(),type,this.getSPM()));

        List<Interval> intervals=this.getIntervalList();

        boolean timeIncremental, distanceIncremental;

        switch(this.getWorkoutType()){
            case Time:
                timeIncremental=true;
                distanceIncremental=false;
                break;
            case Distance:
                timeIncremental=false;
                distanceIncremental=true;
                break;
            case JustRow:
            default:
                timeIncremental=false;
                distanceIncremental=false;
                break;
        }

        double thisTime=0;
        double thisDistance=0;

        for(Interval i : intervals) {
            thisTime=i.getTime()+(timeIncremental?thisTime:0);
            thisDistance=i.getDistance()+(distanceIncremental?thisDistance:0);
            returned.add(valuesToVS(thisTime,thisDistance,type,i.getSPM()));
        }

        return returned;
    }


    public double compToVVS(Vector<Vector<String>> comp1,thirdColumnType type){
        double total=0.0;
        double correct=0.0;

        Vector<Vector<String>> comp2=this.toVVS(type);
        for (int row=0;row<comp1.size();row++){
            Vector<String> compRow1=comp1.get(row);
            for (int column=0;column<compRow1.size();column++){
                total+=1;
                if (row<comp2.size()&&column<comp2.get(row).size()){
                    correct+=(comp2.get(row).get(column).equals(compRow1.get(column)))?1:0;
                }
            }
        }
        return correct/total;
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
