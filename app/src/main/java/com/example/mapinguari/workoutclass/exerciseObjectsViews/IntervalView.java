package com.example.mapinguari.workoutclass.exerciseObjectsViews;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.PowerUnit;

/**
 * Created by mapinguari on 8/4/15.
 */
public final class IntervalView extends LinearLayout {

    private Interval interval = null;
    public TextView timeView;
    public TextView distanceView;
    public TextView variableView;
    public TextView SPMView;
    public TextView restView;
    Context context;

    public IntervalView(Context context) {
        super(context);
        this.context = context;
        buildIntervalView(context,null);
    }

    public IntervalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        buildIntervalView(context, attrs);
    }

    public IntervalView(Context context,Interval interval) {
        super(context);
        this.context = context;
        this.interval = interval;
        buildIntervalView(context, null);
        setIntervalView();
    }

    public IntervalView(Context context, AttributeSet attrs, Interval interval) {
        super(context, attrs);
        this.interval = interval;
        this.context = context;
        buildIntervalView(context, attrs);
        setIntervalView();
    }


    public void setInterval(Interval interval){
        this.interval = interval;
        setIntervalView();
    }

    public void makeEditable(Boolean makeEditable){
        makeViewEditable(timeView, makeEditable);
        makeViewEditable(distanceView, makeEditable);
        makeViewEditable(variableView, makeEditable);
        makeViewEditable(SPMView, makeEditable);
        makeViewEditable(restView, makeEditable);
    }

    private void makeViewEditable(TextView view, Boolean editable) {
        view.setCursorVisible(editable);
        view.setFocusableInTouchMode(editable);
        view.setInputType(InputType.TYPE_CLASS_NUMBER);
        view.setTextIsSelectable(true);
        view.setOnFocusChangeListener(new KeyboardShow());
        if (editable) {
            view.setBackgroundColor(getResources().getColor(R.color.sepia));
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.white));
        }

    }

    class KeyboardShow implements View.OnFocusChangeListener{
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }


    private void buildIntervalView(Context context,AttributeSet attributeSet) {

        LayoutParams textLayoutParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        LayoutParams LLayout = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        this.setOrientation(VERTICAL);
        this.setLayoutParams(LLayout);

        LinearLayout toprow = new LinearLayout(context,attributeSet);
        toprow.setLayoutParams(LLayout);
        toprow.setOrientation(HORIZONTAL);

        this.addView(toprow);

        timeView = new TextView(context, attributeSet);
        distanceView = new TextView(context, attributeSet);
        variableView = new TextView(context, attributeSet);
        SPMView = new TextView(context, attributeSet);

        TextView[] tvA = {timeView,distanceView,variableView,SPMView};
        for(TextView tv : tvA){
            tv.setLayoutParams(textLayoutParams);
            toprow.addView(tv);
        }

        LinearLayout bottomRow = new LinearLayout(context,attributeSet);
        bottomRow.setOrientation(HORIZONTAL);
        bottomRow.setLayoutParams(LLayout);
        this.addView(bottomRow);

        TextView restViewTitle = new TextView(context,attributeSet);
        restViewTitle.setText(getResources().getString(R.string.interval_view_rest_time_title));
        restViewTitle.setLayoutParams(textLayoutParams);
        bottomRow.addView(restViewTitle);

        restView = new TextView(context,attributeSet);
        restView.setLayoutParams(textLayoutParams);
        bottomRow.addView(restView);

    }

    private void setIntervalView(){
        String humanTime = "";
        String distance = "";
        String variable = "";
        String SPM = "";
        String restTime = "";
        if (interval != null){
            humanTime = interval.getHumanTime();
            distance = Integer.toString(interval.getDistance().intValue());
            variable = interval.getHumanSplit();
            SPM = interval.getSPM().toString();
            restTime = Integer.toString(interval.getRestTime().intValue());
        }
        timeView.setText(humanTime);
        distanceView.setText(distance);
        variableView.setText(variable);
        SPMView.setText(SPM);
        restView.setText(restTime);
    }


    public void changeUnit(PowerUnit.CurrentUnit cu){
        switch(cu){
            case WATTS:
                variableView.setText(interval.getWatts().toString());
                break;
            case SPLIT:
                variableView.setText(interval.getHumanSplit());
                break;
            case JpStr:
                variableView.setText(interval.energyPerStroke().toString());
                break;
            case mpStr:
                variableView.setText(interval.getDistancePerStroke().toString());
                break;
        }
    }

    public Interval getNewInterval(){
        return new Interval(Double.parseDouble(timeView.getText().toString()),
                            Double.parseDouble(distanceView.getText().toString()),
                            Integer.parseInt(SPMView.getText().toString()),
                            Double.parseDouble(restView.getText().toString()));
    }

}
