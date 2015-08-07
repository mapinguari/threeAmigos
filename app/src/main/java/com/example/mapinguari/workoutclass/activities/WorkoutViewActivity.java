package com.example.mapinguari.workoutclass.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.mapinguari.workoutclass.exerciseObjectsViews.IntervalView;
import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;
import com.example.mapinguari.workoutclass.exerciseObjectsViews.WorkoutView;

import java.util.List;

public class WorkoutViewActivity extends ActionBarActivity {

    Workout workout;
    TextView dateView;

    ScrollView intervalsScroll;
    List<IntervalView> makeEditableList;
    LinearLayout headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WorkoutView workoutView = null;
        if( getIntent().getBooleanExtra(getResources().getString(R.string.EXTRA_WORKOUT_PASSED),false)){
            workout = getIntent().getParcelableExtra(getResources().getString(R.string.EXTRA_WORKOUT));
        }
        workoutView = new WorkoutView(this,workout);
        //workoutView.setOnClickListener(this);
        setContentView(workoutView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workout_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
