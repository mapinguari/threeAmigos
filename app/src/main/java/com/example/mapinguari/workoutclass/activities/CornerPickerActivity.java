package com.example.mapinguari.workoutclass.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
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

import com.example.mapinguari.workoutclass.ImageTransform;
import com.example.mapinguari.workoutclass.ImgProcess;
import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exceptions.NotHumanStringException;
import com.example.mapinguari.workoutclass.exerciseObjects.ErgoFormatter;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Vector;

public class CornerPickerActivity extends ActionBarActivity {

    int x1,y1,x2,y2,x3,y3,x4,y4;
    View movingView;
    ImageView ergoImageView;
    RelativeLayout relativeLayout;
    ArrayList<View> cornerPoints;
    int cornerPointsCount;
    Button saveButton;
    Uri imgURI;
    boolean lastWasMove = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corner_picker);
        ergoImageView = (ImageView) findViewById(R.id.ergo_image_view);
        relativeLayout = (RelativeLayout) findViewById(R.id.corner_picker_frame);
        saveButton = (Button) findViewById(R.id.corner_picker_saveButton);
        Intent sIntent = getIntent();

        String farSide = sIntent.getStringExtra(getResources().getString(R.string.EXTRA_ERGO_IMAGE));

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
        } catch (Exception e) {
            Log.e("LoadImage", e.toString());
            e.printStackTrace();
        }
        Workout gleanedWorkout = null;
        if(ocrReturnedValues != null) {
            Log.w("OCR OUTPUT", ocrReturnedValues.toString());

            gleanedWorkout = conservativeWorkout(ocrReturnedValues);
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
        Interval totalsInterval = null;
        Interval currentInterval;
        ArrayList<Interval> intervals = new ArrayList<Interval>();
        Vector<String> currentRow;
        for(int i = vvs.size() - 1; i >= 0;i--){
            currentRow = vvs.get(i);
            try{
                currentInterval = new Interval(getTime(currentRow),getDistance(currentRow),getSPM(currentRow),0.0);
            } catch(Exception exception){
                break;
            }

            if (currentInterval!=null) {
                if (i == 0) {
                    totalsInterval = currentInterval;
                } else {
                    intervals.add(currentInterval);
                }
            }
        }
        Workout result = null;
        if(totalsInterval != null) {
            result = new Workout(intervals, totalsInterval.getSPM(), totalsInterval.getDistance(), totalsInterval.getDistance(), new GregorianCalendar());
        }
        return result;
    }

    public Double getTime(Vector<String> vs){
        String time = vs.get(0);
        Double secs = 0.0;
        try{
            secs = ErgoFormatter.parseSeconds(time);
        } catch(NotHumanStringException e){
            e.printStackTrace();
            Log.w("time string parse fail", time);
        }

        return secs;
    }

    public Double getDistance(Vector<String> vs){
        String distance = vs.get(1);
        Double dist = 0.0;
        dist = Double.parseDouble(distance);
        return dist;
    }

    public Integer getSPM(Vector<String> vs){
        String spmS = vs.get(3);
        Integer spm = Integer.parseInt(spmS);
        return spm;
    }

    private double getcenterX(View v){
        return v.getX() + (v.getWidth() / 2);
    }
    private double getcenterY(View v){
        return v.getY() + (v.getHeight() / 2);
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
