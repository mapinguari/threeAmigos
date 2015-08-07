package com.example.mapinguari.workoutclass.exerciseObjectsViews;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.activities.MainMenuActivity;
import com.example.mapinguari.workoutclass.activities.ManualInputActivity;
import com.example.mapinguari.workoutclass.database.DatabaseInterface;
import com.example.mapinguari.workoutclass.exerciseObjects.GregtoString;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.PowerUnit;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by mapinguari on 8/5/15.
 */
public final class WorkoutView extends LinearLayout {

    Workout workout = null;
    Context context;
    GregorianCalendar currentCal;

    TextView dateView;

    Button datePickerButton;
    Button timePickerButton;


    HeaderView headerView;
    IntervalView totalsView;
    ScrollView intervalsViewCont;
    LinearLayout intervalsView;
    Button saveButton;

    PowerUnit powerUnitDisplayed;


    public WorkoutView(Context context, @Nullable Workout workout) {
        super(context);
        this.context = context;
        this.workout = workout;
        buildView(context);
        setWorkoutView();
    }

    private void buildView(Context context){
        this.setOrientation(VERTICAL);
        dateView = new TextView(context);

        datePickerButton = new Button(context);
        timePickerButton = new Button(context);

        if(workout == null){
            currentCal = new GregorianCalendar();
        }
        else{
            currentCal = workout.getWorkoutTime();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog();
        TimePickerDialog timePickerDialog = new TimePickerDialog();


        headerView = new HeaderView(context);
        totalsView = new IntervalView(context);
        totalsView.setBackgroundResource(R.drawable.top_bottom_border);
        intervalsViewCont = new ScrollView(context);
        intervalsView = new LinearLayout(context);
        intervalsView.setOrientation(VERTICAL);
        intervalsViewCont.addView(intervalsView);


        powerUnitDisplayed = new PowerUnit(PowerUnit.CurrentUnit.SPLIT);

        this.addView(dateView);
        this.addView(headerView);
        headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.addView(totalsView);
        this.addView(intervalsViewCont);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    private IntervalView intervalViewToAdd(@Nullable Interval interval){
        IntervalView intervalView = new IntervalView(context,interval);
        if(interval == null){
            intervalView.makeEditable(true);
        }
        return intervalView;
    }

    private void addIntervals(List<Interval> intervals){
        IntervalView currInt;
        int j =0;
        for(Interval i : intervals){
            currInt = intervalViewToAdd(i);
            if(j % 2 ==0){
                currInt.setBackgroundColor(getResources().getColor(R.color.even_list_item));
            }
            else{
                currInt.setBackgroundColor(getResources().getColor(R.color.odd_list_item));
            }
            intervalsView.addView(currInt);
            j++;
        }
    }

    private Workout getNewWorkout(){
        Workout result;
        String date = dateView.getText().toString();
        Interval totalsInterval = totalsView.getNewInterval();
        List<Interval> intervals = new ArrayList<Interval>(intervalsView.getChildCount()-1);
        for(int i = 0; i < intervalsView.getChildCount() - 1; i++){
            intervals.add(i,((IntervalView) intervalsView.getChildAt(i)).getNewInterval());
        }
        result = new Workout(intervals,totalsInterval.getSPM(),totalsInterval.getDistance(),totalsInterval.getTime(),GregtoString.getGregCal(date));
        return result;
    }

    private void addSaveButton(){
        saveButton = new Button(context);
        saveButton.setText(getResources().getString(R.string.save_button_text));
        intervalsView.addView(saveButton);
        SaveClick sc = new SaveClick();
        saveButton.setOnClickListener(sc);
    }

    private class SaveClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Workout newWorkout = getNewWorkout();
            if(newWorkout != workout){
                DatabaseInterface db = new DatabaseInterface(context);
                boolean successdb = db.insertWorkout(newWorkout);
                String dbMessage = successdb ? "The workout was added" : "The workout was not added";
                Toast tellsuccess = Toast.makeText(context, dbMessage, Toast.LENGTH_SHORT);
                tellsuccess.show();
                if (successdb) {
                    Intent intent = new Intent(context, MainMenuActivity.class);
                    context.startActivity(intent);
                }
            }
        }
    }



    public void setWorkoutView(){
        String dateString = "";
        Interval totalsInterval = null;
        List<Interval> intervals= new ArrayList<Interval>();
        intervals.add(null);
        if(workout != null){
            dateString = GregtoString.getDateTime(workout.getWorkoutTime());
            totalsInterval = new Interval(workout.getTime(),workout.getDistance(),workout.getSPM(),workout.getTotalRest());
            intervals = workout.getIntervalList();
            for(Interval i : intervals){
            }
        }
        dateView.setText(dateString);
        totalsView.setInterval(totalsInterval);
        addIntervals(intervals);
        if(workout == null){
            addSaveButton();
            totalsView.makeEditable(true);
        }
    }



}
