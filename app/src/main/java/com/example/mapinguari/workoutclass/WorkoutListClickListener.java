package com.example.mapinguari.workoutclass;

import android.view.View;

/**
 * Created by mapinguari on 7/28/15.
 */
public class WorkoutListClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        WorkoutListItemLayout li = (WorkoutListItemLayout) v;
        //NEED INTENT - pass workoutID call the database from the next activity
        //TODO:implement on click
    }
}
