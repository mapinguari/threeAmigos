package com.example.mapinguari.workoutclass.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.ImgProcess;
import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exerciseObjectBuilders.WorkoutChecker;
import com.example.mapinguari.workoutclass.exerciseObjectParsers.WorkoutParser;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

public class PhotoInspection extends ActionBarActivity {

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

        int MAX_CORRECTIVE_ITERATIONS = 3;

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
            ImgProcess imgProcess = new ImgProcess(fullBitmap,this.getCacheDir().getCanonicalPath());
            ocrReturnedValues= imgProcess.ProcessImage();
            Bitmap bit = imgProcess.linesImg;

        } catch (Exception e) {
            Log.e("LoadImage", e.toString());
            e.printStackTrace();
        }
        Workout gleanedWorkout = null;
        if(ocrReturnedValues != null) {
            Log.w("OCR OUTPUT", ocrReturnedValues.toString());

            //trying a bolder parse
            //gleanedWorkout = conservativeWorkout(ocrReturnedValues);
            Vector<String> totals = ocrReturnedValues.firstElement();
            ocrReturnedValues.remove(0);
            Vector<Vector<String>> subIntervals = ocrReturnedValues;
            GregorianCalendar workoutTime = new GregorianCalendar();

            WorkoutParser workoutParser = new WorkoutParser(totals, workoutTime, subIntervals);

            WorkoutChecker workoutChecker = workoutParser.runForWorkoutChecker();
            workoutChecker.performAllCorrection(MAX_CORRECTIVE_ITERATIONS, true);

            Toast.makeText(this, "Yes I did the new stuff", Toast.LENGTH_LONG).show();


            gleanedWorkout = workoutChecker.getWorkout();

//            BolderWorkout bw = new BolderWorkout();
//
//            gleanedWorkout = bw.bolderWorkout(ocrReturnedValues,ImgProcess.workoutType);

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
