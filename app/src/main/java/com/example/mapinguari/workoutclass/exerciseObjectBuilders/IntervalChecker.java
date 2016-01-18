package com.example.mapinguari.workoutclass.exerciseObjectBuilders;

import com.example.mapinguari.workoutclass.exerciseObjects.Interval;

/**
 * Created by mapinguari on 1/11/16.
 */
public class IntervalChecker extends PerfomanceMeasureChecker {


    private Double protoRest;



    public IntervalChecker(Double protoTime, Double protoDistance, Double protoSplit, Integer protoSPM, Double protoRest) {
        super(protoTime,protoDistance,protoSplit,protoSPM);

        this.protoRest = protoRest;

    }

    public Interval standardInternalFix(){
        if(!isConsistent()){
            internalFix();
        }
        return getInterval();
    }

    public boolean fullInterval() {
        return fullPerformanceMeasure();
    }

    public Interval getInterval() {
        return new Interval(protoTime, protoDistance, protoSPM, protoRest);
    }


    public void fixExternalRest(Double protoRest) {
        this.protoRest = protoRest;
    }


    public Double getProtoRest() {
        return protoRest;
    }




}
