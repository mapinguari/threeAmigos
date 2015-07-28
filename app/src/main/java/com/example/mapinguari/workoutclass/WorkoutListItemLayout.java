package com.example.mapinguari.workoutclass;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by mapinguari on 7/28/15.
 */
public class WorkoutListItemLayout extends LinearLayout {

    public Integer workout_ID;

    public WorkoutListItemLayout(Context context) {
        super(context);
    }

    public WorkoutListItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WorkoutListItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //public WorkoutListItemLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    //    super(context, attrs, defStyleAttr, defStyleRes);
    //}

}
