package com.example.mapinguari.workoutclass.exerciseObjects;

/**
 * Created by mapinguari on 8/5/15.
 */
public class PowerUnit {

    public enum CurrentUnit {
        SPLIT, WATTS, JpStr, mpStr
    }

    public CurrentUnit currentUnit;

    public PowerUnit(){
        currentUnit = CurrentUnit.SPLIT;
    }

    public PowerUnit(CurrentUnit cu){
        currentUnit = cu;
    }

    public CurrentUnit update(){
        int currentOrd = currentUnit.ordinal();
        CurrentUnit[] allEnums = currentUnit.values();
        CurrentUnit nextOrd = allEnums[(currentOrd + 1) % allEnums.length];
        currentUnit = nextOrd;
        return nextOrd;
    }
    //s/m
    public static Double pace(Double time,Double distance){
        return (time/distance);
    }

    //s/500m
    public static Double split(Double time, Double distance){
        return pace(time,distance)*500;
    }
    //j/s
    public static Double watts(Double time, Double distance){
        return 2.8 / (Math.pow(pace(time,distance),3));
    }

    //kcal - This formula I took from a website as C2 do not publish thier acutal formula. We will
    // presume it is correct until otherwise.
    public static Double calories(Double time,Double distance){
        return 300 + 1200000000/(Math.pow(pace(time,distance),3));
    }

    public static Double splitFromWatts(Double watts){
        return 500 * Math.cbrt(2.80/watts);
    }

    public static Double splitFromCal(Double cal){
        return 500*(Math.cbrt(1200000000)/(cal - 300));
    }
}
