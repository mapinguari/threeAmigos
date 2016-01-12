package com.example.mapinguari.workoutclass.ExerciseObjectBuilders;

/**
 * Created by mapinguari on 1/11/16.
 */
public abstract class PerfomanceMeasureChecker {
    protected Double protoTime;
    protected boolean zeroTime;
    protected Double protoDistance;
    protected boolean zeroDistance;
    protected Double protoSplit;
    protected boolean zeroSplit;
    protected Integer protoSPM;
    protected boolean zeroSPM;
    protected Double epsilon = 0.1;

    public PerfomanceMeasureChecker(Double protoTime, Double protoDistance, Double protoSplit, Integer protoSPM) {
        this.protoTime = protoTime;
        this.protoDistance = protoDistance;
        this.protoSplit = protoSplit;
        this.protoSPM = protoSPM;
    }

    //Ergo numbers are very inaccurate, need epsilon equality
    public boolean isConsistent(){
        return Math.abs(protoTime * getSpeed() - protoDistance ) < epsilon && !zeroTime;
    }

    public void internalFix(){
        if(zeroTime && canFixTime()){
            fixTime();
        } else if(zeroDistance && canFixDistance()){
            fixDistance();
        }
    }


    protected Double getSpeed(){
        return (1/(protoSplit / 500));
    }

    protected void fixTime(){
        if(canFixTime()){
            protoTime = protoDistance * getSpeed();
        }
    }

    protected void fixDistance(){
        if(canFixDistance()){
            protoDistance = protoTime * getSpeed();
        }
    }

    //WHY CAN'T I CANT THINK OF THIS?!?
//    public void fixSplit(){
//        if(!zeroTime && !zeroDistance){
//            protoSplit = (protoTime / protoDistance) * 500;
//        }
//    }


    protected boolean canFixTime(){
        return !zeroSplit && !zeroDistance;
    }

    protected boolean canFixDistance(){
        return !zeroSplit && !zeroTime;
    }

    public Double getProtoTime() {
        return protoTime;
    }

    public boolean isZeroTime() {
        return zeroTime;
    }

    public Double getProtoDistance() {
        return protoDistance;
    }

    public boolean isZeroDistance() {
        return zeroDistance;
    }

    public Double getProtoSplit() {
        return protoSplit;
    }

    public boolean isZeroSplit() {
        return zeroSplit;
    }

    public Integer getProtoSPM() {
        return protoSPM;
    }

    public boolean isZeroSPM() {
        return zeroSPM;
    }


}
