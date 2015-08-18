package com.example.mapinguari.workoutclass.exerciseObjectsViews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exceptions.IncompleteIntervalException;
import com.example.mapinguari.workoutclass.exceptions.NotHumanStringException;
import com.example.mapinguari.workoutclass.exerciseObjects.ErgoFormatter;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.PowerUnit;

import org.w3c.dom.Text;

import java.util.zip.Inflater;

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
    LinearLayout bottomRow;

    WorkoutView workoutView;

    AlertDialog.Builder splitDialogBuilder;
    AlertDialog.Builder timeDialogBuilder;

    public IntervalView(Context context){
        super(context);
        this.context = context;
        buildIntervalView(context, null);
        newSplitSpinnerDialog();
        newTimeDialog();
    }


    public IntervalView(Context context,Interval interval) {
        super(context);
        this.context = context;
        this.interval = interval;
        buildIntervalView(context, null);
        setIntervalView();
        newSplitSpinnerDialog();
        newTimeDialog();

    }

    public IntervalView(Context context, AttributeSet attrs, Interval interval) {
        super(context, attrs);
        this.interval = interval;
        this.context = context;
        buildIntervalView(context, attrs);
        setIntervalView();
        newTimeDialog();

    }


    public void setInterval(Interval interval){
        this.interval = interval;
        setIntervalView();
    }

    public void makeEditable(Boolean makeEditable){
        makeViewTimeEditable(timeView, R.string.dialog_time_title, makeEditable);
        makeViewEditable(distanceView, makeEditable);
        //TODO: NEED TO WORK OUT WHAT TO DO WHEN CHANGING EDITABLE MODE HERE
        makeViewDialogEditable(variableView, R.string.dialog_split_title, makeEditable);
        //
        makeViewEditable(SPMView, makeEditable);
        makeViewTimeEditable(restView, R.string.dialog_time_title, makeEditable);
    }

    private void makeViewEditable(TextView view, Boolean editable) {
        view.setCursorVisible(editable);
        view.setFocusableInTouchMode(editable);
        view.setInputType(InputType.TYPE_CLASS_NUMBER);
        view.setTextIsSelectable(true);
        view.setOnFocusChangeListener(new KeyboardShow());
        if (editable) {
            //view.setTextColor(getResources().getColor(R.color.sepia));
            //view.setBackgroundColor(getResources().getColor(R.color.sepia));
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.white));
        }

    }

    class TimeEditDialogClick implements OnClickListener{

        int titleId;

        public TimeEditDialogClick(int dialogTitleId){
            titleId = dialogTitleId;
        }

        @Override
        public void onClick(View v) {
            instanciateTimeDialog(titleId, (TextView) v);
        }
    }

    class DialogEditableClick implements OnClickListener{

        int titleId;

        public DialogEditableClick(int dialogTitleId){
         titleId = dialogTitleId;
        }

        @Override
        public void onClick(View v) {
            instanciateSplitSpinnerDialog(titleId,(TextView) v);
        }
    }

    private void makeViewDialogEditable(TextView textView, int titleId,Boolean editable) {
        if (editable) {
            textView.setOnClickListener(new DialogEditableClick(titleId));
        } else {
            textView.setOnClickListener(null);
        }
    }

    private void makeViewTimeEditable(TextView textView, int titleId, Boolean editable) {
            if (editable) {
                textView.setOnClickListener(new TimeEditDialogClick(titleId));
            } else {
                textView.setOnClickListener(null);
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
        this.setPadding(2,2,2,2);

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

        bottomRow = new LinearLayout(context,attributeSet);
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
        int restTimeVal = 0;
        if (interval != null){
            humanTime = interval.showHumanTime();
            distance = Integer.toString(interval.getDistance().intValue());
            variable = interval.showHumanSplit();
            SPM = interval.getSPM().toString();
            restTimeVal = interval.getRestTime().intValue();
            restTime = interval.showHumanRestTime();
        }
        if(restTimeVal == 0 && interval != null){
            bottomRow.setVisibility(GONE);
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

    public Interval getNewInterval() throws IncompleteIntervalException {
        Interval interval;
        Double workSecs;
        Double distance;
        Integer SPM;
        Double restSeconds;
        try {
            workSecs = ErgoFormatter.parseSeconds(timeView.getText().toString());
            distance = Double.parseDouble(distanceView.getText().toString());
            SPM = Integer.parseInt(SPMView.getText().toString());

        }catch(NumberFormatException e){

            throw new IncompleteIntervalException();
        } catch(NotHumanStringException e){
            throw new IncompleteIntervalException();
        }
        try {
            restSeconds = ErgoFormatter.parseSeconds(restView.getText().toString());
        }catch(NotHumanStringException e){
            restSeconds = 0.0;
            interval = new Interval(workSecs,distance,SPM,restSeconds);
            return interval;
        }
        interval = new Interval(workSecs,distance,SPM,restSeconds);
        return interval;
    }

    private void newSplitSpinnerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        splitDialogBuilder = builder;

    }

    private void newTimeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        timeDialogBuilder = builder;
    }

    class DialogButtonResponse implements DialogInterface.OnClickListener{

        private TextView textView;

        public DialogButtonResponse(TextView tv) {
            this.textView = tv;

        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            AlertDialog ad = (AlertDialog) dialog;
            switch (which){
                case(DialogInterface.BUTTON_POSITIVE):
                    Double hours;
                    Double mins;
                    Double secs;
                    Double centi;
                    View hourView = ad.findViewById(R.id.hour_editText);
                    if(hourView == null){
                        hours = 0.0;
                    } else {
                        hours = Double.parseDouble(((TextView) hourView).getText().toString());
                    }
                    mins =  Double.parseDouble(((TextView) ad.findViewById(R.id.min_editText)).getText().toString());
                    secs = Double.parseDouble(((TextView) ad.findViewById(R.id.sec_editText)).getText().toString());
                    centi = Double.parseDouble(((TextView) ad.findViewById(R.id.centi_editText)).getText().toString());
                    Double total = hours*3600 + mins*60 + secs + (centi/10);
                    String out = ErgoFormatter.formatSeconds(total);
                    textView.setText(out);
                    ad.dismiss();
                    break;
                case(DialogInterface.BUTTON_NEGATIVE):
                    ad.dismiss();
                    break;
            }
        }
    }

    private void instanciateTimeDialog(int titleId, TextView textView) {
        timeDialogBuilder.setTitle(getResources().getText(titleId));
        timeDialogBuilder.setView(inflate(context, R.layout.dialog_time_input, null));


        DialogButtonResponse dbr = new DialogButtonResponse(textView);

        timeDialogBuilder.setPositiveButton(R.string.split_dialog_positive, dbr);
        timeDialogBuilder.setNegativeButton(R.string.split_dialog_negative, dbr);

        AlertDialog ad = timeDialogBuilder.show();

        EditText hourSpin = (EditText) ad.findViewById(R.id.hour_editText);
        EditText minSpin = (EditText) ad.findViewById(R.id.min_editText);
        EditText secSpin = (EditText) ad.findViewById(R.id.sec_editText);
        EditText centSpin = (EditText) ad.findViewById(R.id.centi_editText);

        hourSpin.setInputType(InputType.TYPE_CLASS_NUMBER);
        minSpin.setInputType(InputType.TYPE_CLASS_NUMBER);
        secSpin.setInputType(InputType.TYPE_CLASS_NUMBER);
        centSpin.setInputType(InputType.TYPE_CLASS_NUMBER);

        String hoursText;
        String minsText;
        String secsText;
        String centText;

        if(textView.getText() == "") {
            hoursText = "1";
            minsText = "00";
            secsText = "00";
            centText = "0";
        }
        else{
            String current = (String) textView.getText();
            String[] firstS = current.split(":");
            String[] secondS = firstS[2].split("\\.");
            hoursText = firstS[0];
            minsText = firstS[1];
            secsText = secondS[0];
            centText = secondS[1];
        }

        hourSpin.setText(hoursText);
        minSpin.setText(minsText);
        secSpin.setText(secsText);
        centSpin.setText(centText);

        NDigitWatcher singleDigit = new NDigitWatcher(1);
        NDigitWatcher doubleDigit = new NDigitWatcher(2);

        hourSpin.addTextChangedListener(singleDigit);
        minSpin.addTextChangedListener(doubleDigit);
        centSpin.addTextChangedListener(singleDigit);
        secSpin.addTextChangedListener(doubleDigit);

    }

    private void  instanciateSplitSpinnerDialog(int titleId, TextView textView){

        splitDialogBuilder.setTitle(getResources().getText(titleId));
        splitDialogBuilder.setView(inflate(context, R.layout.dialog_split_input, null));


        DialogButtonResponse dbr = new DialogButtonResponse(textView);

        splitDialogBuilder.setPositiveButton(R.string.split_dialog_positive, dbr);
        splitDialogBuilder.setNegativeButton(R.string.split_dialog_negative, dbr);

        AlertDialog ad = splitDialogBuilder.show();

        EditText minSpin = (EditText) ad.findViewById(R.id.min_editText);
        EditText secSpin = (EditText) ad.findViewById(R.id.sec_editText);
        EditText centSpin = (EditText) ad.findViewById(R.id.centi_editText);


        minSpin.setInputType(InputType.TYPE_CLASS_NUMBER);
        secSpin.setInputType(InputType.TYPE_CLASS_NUMBER);
        centSpin.setInputType(InputType.TYPE_CLASS_NUMBER);

        String minsText;
        String secsText;
        String centText;

        if(textView.getText() == "") {
            minsText = "2";
            secsText = "00";
            centText = "0";
        }
        else{
            String current = (String) textView.getText();
            String[] firstS = current.split(":");
            String[] secondS = firstS[1].split("\\.");
            minsText = firstS[0];
            secsText = secondS[0];
            centText = secondS[1];
        }

        minSpin.setText(minsText);
        secSpin.setText(secsText);
        centSpin.setText(centText);

        NDigitWatcher singleDigit = new NDigitWatcher(1);
        NDigitWatcher doubleDigit = new NDigitWatcher(2);

        minSpin.addTextChangedListener(singleDigit);
        centSpin.addTextChangedListener(singleDigit);
        secSpin.addTextChangedListener(doubleDigit);
    }

    class NDigitWatcher implements TextWatcher {

        Integer nOC;

        public NDigitWatcher(Integer integer){
            this.nOC = integer;
        }

        @Override
        public void beforeTextChanged (CharSequence s,int start, int count, int after){

        }

        @Override
        public void onTextChanged (CharSequence s,int start, int before, int count) {

        }

        @Override
        public void afterTextChanged (Editable s){
            if (s.length() > nOC) {
                s.replace(0, s.length() - nOC, "");
            }
        }
    }




}
