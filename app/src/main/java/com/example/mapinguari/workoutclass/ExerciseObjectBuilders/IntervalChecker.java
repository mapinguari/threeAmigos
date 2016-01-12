package com.example.mapinguari.workoutclass.ExerciseObjectBuilders;

import com.example.mapinguari.workoutclass.exerciseObjects.Interval;

/**
 * Created by mapinguari on 1/11/16.
 */
public class IntervalChecker extends PerfomanceMeasureChecker {


    private Double protoRest;
    private boolean zeroRest;


    public IntervalChecker(Double protoTime, Double protoDistance, Double protoSplit, Integer protoSPM, Double protoRest) {
        super(protoTime,protoDistance,protoSplit,protoSPM);
        this.zeroTime = protoTime == 0.0;
        this.zeroDistance = protoDistance == 0.0;
        this.zeroSplit = protoSplit == 0.0;
        this.zeroSPM = protoSPM == 0;
        this.protoRest = protoRest;
        this.zeroRest = protoRest == 0.0;

    }

    public Interval standardInternalFix(){
        if(!isConsistent()){
            internalFix();
        }
        return getInterval();
    }




    public Interval getInterval() {
        return new Interval(protoTime, protoDistance, protoSPM, protoRest);
    }


    public void fixExternalTime(Double protoTime) {
        this.protoTime = protoTime;
    }

    public void fixExternalDistance(Double protoDistance) {
        this.protoDistance = protoDistance;
    }

    public void fixExternalSPM(Integer protoSPM) {
        this.protoSPM = protoSPM;
    }

    public void fixExternalRest(Double protoRest) {
        this.protoRest = protoRest;
    }


    public Double getProtoRest() {
        return protoRest;
    }

    public boolean isZeroRest() {
        return zeroRest;
    }




}
