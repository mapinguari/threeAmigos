package com.example.mapinguari.workoutclass.exerciseObjects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mapinguari on 7/24/15.
 */
public class Interval extends PerformanceMeasure implements Parcelable {

    // fields

    // active work fields
    private Double workTime;
    private Double distance;
    private Integer averageSPM;

    //rest fields
    private Double restTime;

    //constructors


    @Override
    public Double getTime() {
        return workTime;
    }

    @Override
    public Integer getSPM() {
        return averageSPM;
    }

    public Double getRest(){
        return restTime;
    }

    public Interval(Double workTime, Double distance, Integer averageSPM, Double restTime) {
        this.workTime = workTime;
        this.distance = distance;
        this.averageSPM = averageSPM;
        this.restTime = restTime;
    }

    public Interval(Parcel parcel){
        readFromParcel(parcel);
    }

    public Double getDistance() {
        return distance;
    }

    //public methods



    public String getUnits(PowerUnit pu){
        String result;
        switch (pu.currentUnit){
            case SPLIT:
                result = getHumanSplit();
                break;
            case WATTS:
                result = getWatts().toString();
                break;
            default: result = "NOT YET DEFINED INTERVAL.GETUNITS RESPOSE";
                break;
        }
        return result;
    }

    public Double getRestTime() {
        return restTime;
    }

    public void setRestTime(Double restTime) {
        this.restTime = restTime;
    }

    public void setWorkTime(Double workTime) {
        this.workTime = workTime;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setAverageSPM(Integer averageSPM) {
        this.averageSPM = averageSPM;
    }


    public double percentageCorrect(Interval correctInterval) {
        double a = workTime.equals(correctInterval.getTime())?1:0;
        double b = distance.equals(correctInterval.getDistance())?1:0;
        double c = averageSPM.equals(correctInterval.getSPM())?1:0;
        double d = restTime.equals(correctInterval.getRestTime())?1:0;
        return (a+b+d+c) / 4;
    }



    //Parcelable code


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(workTime);
        dest.writeDouble(distance);
        dest.writeInt(averageSPM);
        dest.writeDouble(restTime);
    }

    public void readFromParcel(Parcel in){
        workTime = in.readDouble();
        distance = in.readDouble();
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
                ", distance=" + distance +
                ", averageSPM=" + averageSPM +
                ", restTime=" + restTime +
                '}';
    }
    public String[] toStringArr(){
        String[] resp = {Double.toString(workTime),Double.toString(distance),Integer.toString(averageSPM),Double.toString(restTime)};
        return resp;
    }

}
