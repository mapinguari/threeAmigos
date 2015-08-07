package com.example.mapinguari.workoutclass.exerciseObjectsViews;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exerciseObjects.PowerUnit;

import org.w3c.dom.Text;

/**
 * Created by mapinguari on 8/6/15.
 */
public final class HeaderView extends LinearLayout{

    TextView variableUnits;

    public HeaderView(Context context) {
        super(context);
        buildView(context);
    }

    private void buildView(Context context){
        this.setOrientation(HORIZONTAL);

        LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);

        TextView timeView = new TextView(context);
        TextView distanceView = new TextView(context);
        variableUnits = new TextView(context);
        TextView SPMView = new TextView(context);

        timeView.setLayoutParams(layoutParams);
        distanceView.setLayoutParams(layoutParams);
        variableUnits.setLayoutParams(layoutParams);
        SPMView.setLayoutParams(layoutParams);


        timeView.setText(getResources().getString(R.string.workout_view_header_time));
        distanceView.setText(getResources().getString(R.string.workout_view_header_distance));
        variableUnits.setText(getResources().getString(R.string.workout_view_header_split));
        SPMView.setText(getResources().getString(R.string.workout_view_header_spm));

        this.addView(timeView);
        this.addView(distanceView);
        this.addView(variableUnits);
        this.addView(SPMView);

        Log.w("HeaderSet", "");
    }

    public void changeUnits(PowerUnit.CurrentUnit currentUnit){
        switch (currentUnit){
            case SPLIT: variableUnits.setText(getResources().getString(R.string.workout_view_header_split));
                break;
            case WATTS: variableUnits.setText(getResources().getString(R.string.workout_view_header_watts));
                break;
            case JpStr: variableUnits.setText(getResources().getString(R.string.workout_view_header_energy_per_stroke));
                break;
            case mpStr: variableUnits.setText(getResources().getString(R.string.workout_view_header_distance_per_stroke));
                break;
        }

    }
}
