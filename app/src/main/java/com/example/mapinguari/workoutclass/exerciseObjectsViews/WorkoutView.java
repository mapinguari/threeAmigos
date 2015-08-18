package com.example.mapinguari.workoutclass.exerciseObjectsViews;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.activities.MainMenuActivity;
import com.example.mapinguari.workoutclass.database.DatabaseInterface;
import com.example.mapinguari.workoutclass.exceptions.IncompleteIntervalException;
import com.example.mapinguari.workoutclass.exceptions.IncompleteWorkoutException;
import com.example.mapinguari.workoutclass.exerciseObjects.GregtoString;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.PowerUnit;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by mapinguari on 8/5/15.
 */
public final class WorkoutView extends LinearLayout {

    Workout workout = null;
    Context context;
    GregorianCalendar currentCal;


    Button datePickerButton;
    Button timePickerButton;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;


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

        LinearLayout datebuttons = new LinearLayout(context);
        datebuttons.setOrientation(HORIZONTAL);

        datePickerButton = new Button(context);
        timePickerButton = new Button(context);

        datebuttons.addView(datePickerButton);
        datebuttons.addView(timePickerButton);

        if(workout == null){
            currentCal = new GregorianCalendar();
        }
        else{
            currentCal = workout.getWorkoutTime();
        }

        datePickerButton.setText(getNewDate());
        timePickerButton.setText(getNewTime());


        datePickerDialog = new DatePickerDialog(context,new dateGet(),currentCal.get(Calendar.YEAR),currentCal.get(Calendar.MONTH),currentCal.get(Calendar.DAY_OF_MONTH));
        timePickerDialog = new TimePickerDialog(context,new getTime(),currentCal.get(Calendar.HOUR_OF_DAY), currentCal.get(Calendar.MINUTE),true);


        headerView = new HeaderView(context);
        totalsView = new IntervalView(context);
        totalsView.setBackgroundResource(R.drawable.top_bottom_border);
        intervalsViewCont = new ScrollView(context);
        intervalsView = new LinearLayout(context);
        intervalsView.setOrientation(VERTICAL);
        intervalsViewCont.addView(intervalsView);



        powerUnitDisplayed = new PowerUnit(PowerUnit.CurrentUnit.SPLIT);

        this.addView(datebuttons);
        this.addView(headerView);
        headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.addView(totalsView);
        this.addView(intervalsViewCont);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    class dateGet implements DatePickerDialog.OnDateSetListener{
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            currentCal.set(Calendar.YEAR,year);
            currentCal.set(Calendar.MONTH,monthOfYear);
            currentCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            datePickerButton.setText(getNewDate());
        }
    }


    private String getNewDate(){
        DateFormat df = android.text.format.DateFormat.getDateFormat(context);
        return df.format(currentCal.getTime());
    }

    class getTime implements TimePickerDialog.OnTimeSetListener{
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            currentCal.set(Calendar.HOUR_OF_DAY,hourOfDay);
            currentCal.set(Calendar.MINUTE,minute);
            timePickerButton.setText(getNewTime());
        }
    }

    private String getNewTime(){
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        return df.format(currentCal.getTime());
    }

    private IntervalView intervalViewToAdd(@Nullable Interval interval){
        IntervalView intervalView = new IntervalView(context,interval);
        if(interval == null){
            intervalView.makeEditable(true);
        }
        return intervalView;
    }

    void addInterval(Interval interval){
        IntervalView intervalView = intervalViewToAdd(interval);
        int pos = intervalsView.getChildCount() - 2;
        if(pos % 2 ==0){
            intervalView.setBackgroundColor(getResources().getColor(R.color.even_list_item));
        }
        else{
            intervalView.setBackgroundColor(getResources().getColor(R.color.odd_list_item));
        }
        intervalsView.addView(intervalView,pos);
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

    private Workout getNewWorkout() throws IncompleteWorkoutException {
        Workout result;
        IncompleteWorkoutException  exception = new IncompleteWorkoutException();
        Interval totalsInterval = null;
        try {
             totalsInterval = totalsView.getNewInterval();
        }catch(IncompleteIntervalException e){
            exception.setTotalsComplete();
        }
        List<Interval> intervals = new ArrayList<Interval>(intervalsView.getChildCount()-2);
        for(int i = 0; i < intervalsView.getChildCount() - 2; i++){
            try {
                intervals.add(i, ((IntervalView) intervalsView.getChildAt(i)).getNewInterval());
            }catch(IncompleteIntervalException e){
                exception.setIncompleteIntervals(i+1);
            }
        }
        if(exception.beenTouched()){
            throw exception;
        }
        else {
            result = new Workout(intervals, totalsInterval.getSPM(), totalsInterval.getDistance(), totalsInterval.getTime(), currentCal);
            return result;
        }
    }

    private void addSaveButton(){
        saveButton = new Button(context);
        saveButton.setText(getResources().getString(R.string.save_button_text));
        intervalsView.addView(saveButton);
        SaveClick sc = new SaveClick();
        saveButton.setOnClickListener(sc);
    }

    private void addAddIntervalButton(){
        final Button addIntervalButton = new Button(context);
        addIntervalButton.setText(getResources().getText(R.string.add_interval_button_text));
        intervalsView.addView(addIntervalButton);
        addIntervalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addInterval(null);
            }
        });
    }

    private class SaveClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Workout newWorkout = null;
            try {
                newWorkout = getNewWorkout();
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
                } else {
                    Toast sameWorkoutToast = Toast.makeText(context, "This workout has not altered", Toast.LENGTH_SHORT);
                    sameWorkoutToast.show();
                }
            } catch (IncompleteWorkoutException e){
                Toast tellIncomp = Toast.makeText(context,"Please complete all Intervals before saving", Toast.LENGTH_SHORT);
                tellIncomp.show();
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

        totalsView.setInterval(totalsInterval);
        addIntervals(intervals);
        if(workout == null){
            addAddIntervalButton();
            addSaveButton();
            totalsView.makeEditable(true);
        }

        if(workout == null){
            datePickerButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    datePickerDialog.show();
                }
            });
            timePickerButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    timePickerDialog.show();
                }
            });
        }
        headerView.setOnClickListener(new updateUnitsClick());
        totalsView.setOnClickListener(new updateUnitsClick());
        View tempInt;
        for(int i = 0; i < intervalsView.getChildCount(); i++){
            tempInt = intervalsView.getChildAt(i);
            if(tempInt instanceof IntervalView){
                tempInt.setOnClickListener(new updateUnitsClick());
            }
        }

    }

    class updateUnitsClick implements OnClickListener{
        @Override
        public void onClick(View v) {
            changeUnits();

        }
    }

    public void changeUnits(){
        headerView.changeUnits(powerUnitDisplayed.currentUnit);
        totalsView.changeUnit(powerUnitDisplayed.currentUnit);
        View tempInt;
        for(int i = 0; i < intervalsView.getChildCount(); i++){
            tempInt = intervalsView.getChildAt(i);
            if(tempInt instanceof IntervalView){
                ((IntervalView) tempInt).changeUnit(powerUnitDisplayed.currentUnit);
            }
        }
        powerUnitDisplayed.update();
    }



}
