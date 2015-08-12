package com.example.mapinguari.workoutclass.exceptions;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * Created by mapinguari on 8/12/15.
 */
public class IncompleteWorkoutException extends Exception {

    private boolean totalsComplete = true;
    private List<Integer> incompleteIntervals = new ArrayList<Integer>();
    private boolean touched = false;

    public void setTotalsComplete(){
        totalsComplete = false;
        touched = true;
    }

    public void setIncompleteIntervals(int a){
        incompleteIntervals.add(a);
        touched = true;
    }

    public boolean beenTouched(){
        return touched;
    }

    @Override
    public String getMessage() {
        Formatter formatter = new Formatter();
        formatter.format("Totals are complete: %s. The following intervals are incomplete %s",totalsComplete,incompleteIntervals.toString());
        return formatter.out().toString();
    }
}
