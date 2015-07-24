package com.example.mapinguari.workoutclass;

/**
 * Created by mapinguari on 7/24/15.
 */
public class Interval {

    // fields
    // active work fields
    private Double workTime;
    private Double averageWatts;
    private Integer averageSPM;

    //rest fields
    private Double restTime;

    //constructors
    public Interval(Double workTime, Double averageWatts, Integer averageSPM){
        this.workTime = workTime;
        this.averageWatts = averageWatts;
        this.averageSPM = averageSPM;
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


}
