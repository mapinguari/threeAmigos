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
}
