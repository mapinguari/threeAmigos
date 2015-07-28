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

    public Interval(Double workTime, Double averageWatts, Integer averageSPM, Double restTime) {
        this.workTime = workTime;
        this.averageWatts = averageWatts;
        this.averageSPM = averageSPM;
        this.restTime = restTime;
    }

    public String getSplit(){
        Double a = calculateSplit(this.averageWatts);
        String str = secondsToSplit(a);
        return str;
    }

    public static String calcSplit(Double watts){
        Double a = calculateSplit(watts);
        String str = secondsToSplit(a);
        return str;
    }

    private static Double calculateSplit(double watts){
        double a = 2.8/watts;
        double split = Math.cbrt(a);
        return split;
    }

    private static String secondsToSplit(double secondsT){
        double mins = Math.floor(secondsT / 60);
        double secs = Math.floor(secondsT % 60);
        double milli = Math.round((secs % 1) * 10);
        return (Double.toString(mins) + ":" + Double.toString(secs) + "." + Double.toString(milli));
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
