package com.example.mapinguari.workoutclass.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.BolderWorkout;
import com.example.mapinguari.workoutclass.ImageTransform;
import com.example.mapinguari.workoutclass.ImgProcess;
import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exceptions.CantDecipherWorkoutException;
import com.example.mapinguari.workoutclass.exceptions.NotHumanStringException;
import com.example.mapinguari.workoutclass.exerciseObjects.ErgoFormatter;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class  CornerPickerActivity extends ActionBarActivity {

    ImageView ergoImageView;
    RelativeLayout relativeLayout;

    Button saveButton;
    Uri imgURI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corner_picker);
        ergoImageView = (ImageView) findViewById(R.id.ergo_image_view);
        relativeLayout = (RelativeLayout) findViewById(R.id.corner_picker_frame);
        saveButton = (Button) findViewById(R.id.corner_picker_saveButton);
        Intent sIntent = getIntent();


        imgURI = Uri.parse(sIntent.getStringExtra(getResources().getString(R.string.EXTRA_ERGO_IMAGE)));


        ergoImageView.setImageURI(imgURI);
    }


    public void saveCorners(View v){
        Workout gleanedWorkout = OCR();
        Intent inspectWorkout = new Intent(getApplicationContext(),WorkoutViewActivity.class);
        inspectWorkout.putExtra(getResources().getString(R.string.EXTRA_WORKOUT),gleanedWorkout);
        inspectWorkout.putExtra(getResources().getString(R.string.EXTRA_WORKOUT_PASSED),true);
        startActivity(inspectWorkout);

    }



    private Workout OCR(){
        Bitmap fullBitmap = null;
        try {
            fullBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgURI);
        } catch(Exception e){
            Toast failed = Toast.makeText(this, "Can't get a bitmap", Toast.LENGTH_SHORT);
            failed.show();
            return null;
        }
        Vector<Vector<String>> ocrReturnedValues = null;

        ImgProcess.loadLanguage("lan", this.getApplicationContext());

        try {
            ocrReturnedValues= ImgProcess.ProcessImage(fullBitmap,
                    this.getCacheDir().getCanonicalPath());
            Bitmap bit = ImgProcess.linesImg;

        } catch (Exception e) {
            Log.e("LoadImage", e.toString());
            e.printStackTrace();
        }
        Workout gleanedWorkout = null;
        if(ocrReturnedValues != null) {
            Log.w("OCR OUTPUT", ocrReturnedValues.toString());

            //trying a bolder parse
            //gleanedWorkout = conservativeWorkout(ocrReturnedValues);

            BolderWorkout bw = new BolderWorkout();

            gleanedWorkout = bw.bolderWorkout(ocrReturnedValues,ImgProcess.workoutType);

            if (gleanedWorkout == null) {
                Toast failed = Toast.makeText(this, "Couldn't get a workout out", Toast.LENGTH_SHORT);
                failed.show();
            }
        } else {
            Toast failed = Toast.makeText(this, "I tried, nothing to find", Toast.LENGTH_SHORT);
            failed.show();
        }
        return gleanedWorkout;
    }



    public Workout conservativeWorkout(Vector<Vector<String>> vvs){
        Log.v("PAR","parsing:"+(vvs.size())+" rows");

        Interval totalsInterval = null;
        Interval currentInterval;
        ArrayList<Interval> intervals = new ArrayList<Interval>();
        Vector<String> currentRow;
        boolean[] missingTime=new boolean[vvs.size()],missingDistance=new boolean[vvs.size()],
                missingStrokes=new boolean[vvs.size()];

        Integer[] powers=new Integer[vvs.size()];

        Arrays.fill(missingTime, false);
        Arrays.fill(missingDistance, false);
        Arrays.fill(missingStrokes, false);

        for(int i = 0; i <vvs.size();i++){
            currentRow = vvs.get(i);
            Double time=null,distance;
            Integer spm;
            try{

                time = getTime(currentRow);
                distance = getDistance(currentRow);
                powers[i]=getPower(currentRow, time, distance);
                spm=getSPM(currentRow);

                currentInterval = new Interval(time!=null?time:0, distance!=null?distance:0,
                        spm!=null?spm:0,0.0);

                Log.v("PAR","row "+i+":"+ currentInterval.toString());

                if(correctHorizontal(currentInterval,time,distance,powers[i])){
                    if(time==null)
                        time=currentInterval.getTime();
                    else if (distance==null)
                        distance=currentInterval.getDistance();
                }

            } catch(Exception exception){
                break;
            }

            if (time==null) {
                missingTime[i]=true;
            }
            if (distance==null) {
                missingDistance[i]=true;
            }
            if (spm==null) {
                missingStrokes[i]=true;
            }

            if (i == 0) {
                totalsInterval = currentInterval;
            } else {
                intervals.add(currentInterval);
            }

        }

        while(correctVertical(totalsInterval,intervals,missingTime,missingDistance,missingStrokes)){

            correctHorizontal(totalsInterval,missingTime[0]?null:totalsInterval.getTime(),
                    missingDistance[0]?null:totalsInterval.getDistance(),powers[0]);

            for (int i = 0; i < intervals.size();i++) {
                correctHorizontal(intervals.get(i),missingTime[i+1]?null:intervals.get(i).getTime(),
                        missingDistance[i+1]?null:intervals.get(i).getDistance(),powers[i+1]);
            }
        }




        Workout result = null;
        if(totalsInterval != null) {
            result = new Workout(intervals, totalsInterval.getSPM(), totalsInterval.getDistance(), totalsInterval.getDistance(), new GregorianCalendar());
        }
        return result;
    }

    private boolean correctVertical(Interval totalsInterval, ArrayList<Interval> intervals,
                                    boolean[] missingTime, boolean[] missingDistance, boolean[] missingStrokes){
        boolean changed=false;

        int numberMissingTime=0,numberMissingDistance=0,numberMissingStrokes=0;
        double totalDistance=0.0,totalTime=0.0;
        int totalStrokes=0;
        int length=intervals.size()+1;

        if (totalsInterval==null||length==1)
            return false;


        for(int i=0;i<length;i++) {
            numberMissingTime+=missingTime[i]?1:0;
            numberMissingDistance+=missingDistance[i]?1:0;
            numberMissingStrokes+=missingStrokes[i]?1:0;

            if (i>1){
                totalTime+=intervals.get(i-1).getTime();
                totalDistance+=intervals.get(i-1).getDistance();
            }
        }

        if (numberMissingTime==1) {
            changed=true;
            if (missingTime[0]) {
                totalsInterval.setWorkTime(totalTime);
                missingTime[0]=false;
            } else {
                double time = totalsInterval.getTime() - totalTime;
                int i = 0;
                do {
                    if (missingTime[++i]) {
                        intervals.get(i - 1).setWorkTime(time);
                        missingTime[i]=false;
                    }
                } while (!missingTime[i]);
            }
        }

        if (numberMissingDistance==1) {
            changed=true;
            if (missingDistance[0]) {
                totalsInterval.setDistance(totalDistance);
                missingDistance[0]=false;
            }else {
                double distance = totalsInterval.getDistance() - totalDistance;
                int i = 0;
                do {
                    if (missingDistance[++i]) {
                        intervals.get(i - 1).setDistance(distance);
                        missingDistance[i]=false;
                    }
                } while (!missingDistance[i]);
            }
        }

        for (int i=1;i<length;i++){
            totalStrokes+=(!missingStrokes[i]&&!missingTime[i])?
                    intervals.get(i-1).getSPM()*intervals.get(i-1).getTime() : 0;
        }

        if (numberMissingStrokes==1&&numberMissingTime<2) {
            if (missingStrokes[0]) {
                if (totalsInterval.getTime()!=0) {
                    changed = true;
                    missingStrokes[0] = false;
                    totalsInterval.setAverageSPM((int) (totalStrokes / totalsInterval.getTime()));
                }
            } else {
                int i = 0;
                double strokes = totalsInterval.getSPM() * totalsInterval.getTime() - totalStrokes;
                do {
                    i++;
                    if (missingStrokes[i] && intervals.get(i - 1).getTime() != 0.0) {
                        changed = true;
                        intervals.get(i - 1).setAverageSPM((int) (strokes / intervals.get(i - 1).getTime()));
                    }
                } while (!missingStrokes[i]);
                missingStrokes[i] = false;
            }
        }

        return changed;
    }

    private boolean correctHorizontal(Interval interval, Double time, Double distance, Integer power){
        boolean changed=false;
        if (time==null&&(distance!=null&&power!=null)) {
            interval.setWorkTime(Math.pow((2.8 / (double)power),1.0/3.0)*distance);
            changed=true;
        }
        else if (distance==null&&(time!=null&&power!=null)){
            interval.setDistance(time/Math.pow((2.8 / (double)power),1.0/3.0));
            changed=true;
        }


        return changed;
    }

    public Double getTime(Vector<String> vs){
        Double secs;
        String time="";
        try{
            time = vs.get(0);
            secs = ErgoFormatter.parseSeconds(time);
        } catch(NotHumanStringException e){
            secs=null;
            e.printStackTrace();
            Log.w("time string parse fail", time);
        } catch (ArrayIndexOutOfBoundsException e){
            secs=null;
            e.printStackTrace();
            Log.w("time string parse fail", time);
        }

        return secs;
    }

    public Double getDistance(Vector<String> vs){
        Double dist;
        String distance="";
        try {
            distance = vs.get(1);
            dist = Double.parseDouble(distance);
        } catch(NumberFormatException e){
            dist=null;
            e.printStackTrace();
            Log.w("dist string parse fail", distance);
        } catch (ArrayIndexOutOfBoundsException e){
            dist=null;
            e.printStackTrace();
            Log.w("dist string parse fail", distance);
        }
        return dist;
    }

    public Integer getPower(Vector<String> vs, Double time, Double distance){
        Integer power;
        String pow="";
        try {
            pow = vs.get(2);
            power = Integer.parseInt(pow);
        } catch(NumberFormatException e){
            if (time != null && distance != null &&distance!=0) {
                power = (int) (2.8 * Math.pow(time / distance, 3.0));
            } else {
                power = null;
            }
            e.printStackTrace();
            Log.w("dist string parse fail", pow);
        } catch (ArrayIndexOutOfBoundsException e){
            power = null;
            e.printStackTrace();
            Log.w("dist string parse fail", pow);
        }
        return power;
    }

    public Integer getSPM(Vector<String> vs){
        Integer spm;
        String spmS="";
        try {
            spmS = vs.get(3);
            spm=Integer.parseInt(spmS);
        } catch (NumberFormatException e) {
            spm=null;
            e.printStackTrace();
            Log.w("SPM string parse fail", spmS);
        } catch (ArrayIndexOutOfBoundsException e){
            spm=null;
            e.printStackTrace();
            Log.w("SPM string parse fail", spmS);
        }
        return spm;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_corner_picker, menu);
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
