package com.example.mapinguari.workoutclass;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by mapinguari on 8/2/15.
 */
public class IntervalGen {

    Random r;

    public IntervalGen(Random a){
        r = a;
    }

    public IntervalGen(){
        r = new Random();
    }

    public Interval generateInterval(){
        double time = r.nextDouble()*600;
        double watts = r.nextDouble()*10000;
        double rTime = r.nextDouble()*600;
        int spm = r.nextInt(100);
        return new Interval(time,watts,spm,rTime);
    }

    public List<Interval> generateIntevals(int n){
        List<Interval> intervals = new ArrayList<Interval>(n);
        for(int i = 0; i < n;i++){
            intervals.add(i,generateInterval());
        }
        return intervals;
    }
}
