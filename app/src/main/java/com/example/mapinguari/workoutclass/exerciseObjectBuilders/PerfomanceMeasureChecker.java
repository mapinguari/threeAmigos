package com.example.mapinguari.workoutclass.exerciseObjectBuilders;

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

        this.zeroTime = protoTime == 0.0;
        this.zeroDistance = protoDistance == 0.0;
        this.zeroSplit = protoSplit == 0.0;
        this.zeroSPM = protoSPM == 0;
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

    protected int numberOfZeros(){
        int sum = 0;
        if(zeroTime)
            sum++;
        if(zeroDistance)
            sum++;
        if(zeroSplit)
            sum++;
        if(zeroSPM)
            sum++;
        return sum;
    }

    public boolean fullPerformanceMeasure(){
        return !zeroTime && !zeroDistance && !zeroSPM;
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

    public void fixExternalTime(Double protoTime) {
        this.protoTime = protoTime;
    }

    public void fixExternalDistance(Double protoDistance) {
        this.protoDistance = protoDistance;
    }

    public void fixExternalSPM(Integer protoSPM) {
        this.protoSPM = protoSPM;
    }

    public void fixExternalSplit(Double protoSplit){
        this.protoSplit = protoSplit;
    }



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
