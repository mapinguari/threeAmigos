package com.example.mapinguari.workoutclass;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by mapinguari on 7/24/15.
 */
public class Workout {

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

    //methods
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
}
