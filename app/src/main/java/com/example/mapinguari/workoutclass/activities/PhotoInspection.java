package com.example.mapinguari.workoutclass.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.example.mapinguari.workoutclass.OCRProcess;
import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exerciseObjectBuilders.WorkoutChecker;
import com.example.mapinguari.workoutclass.exerciseObjectParsers.WorkoutParser;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.Vector;

public class PhotoInspection extends ActionBarActivity {

    ImageView ergoImageView;
    RelativeLayout relativeLayout;

    Button saveButton;
    Uri imgURI;

    Intent debugIntent;

    Workout workout = null;

    String crashNoString = "CrashNo";
    String crashErrorMsg = "CrashMsg";



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
        if(workout == null) {
            workout = OCR();
        }
        Intent inspectWorkout = new Intent(getApplicationContext(),WorkoutViewActivity.class);
        inspectWorkout.putExtra(getResources().getString(R.string.EXTRA_WORKOUT),workout);
        inspectWorkout.putExtra(getResources().getString(R.string.EXTRA_WORKOUT_PASSED),true);
        startActivity(inspectWorkout);

    }

    public void debug(View v){
        workout = OCR();
        startActivity(debugIntent);
    }

    private void intentDrop(String intentTag, Bitmap bitmap){
        File outFile = new File(this.getFilesDir(),intentTag + ".jpg");
        Uri uri = Uri.fromFile(outFile);
        try {
            OutputStream os = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,os);
            debugIntent.putExtra(intentTag,uri);
        }catch(Exception e){

        }

        }

    private Workout OCR(){

        int MAX_CORRECTIVE_ITERATIONS = 3;

        debugIntent = new Intent(getApplicationContext(),Debug.class);

        Bitmap fullBitmap = null;
        try {
            fullBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgURI);
        } catch(Exception e){
            Toast failed = Toast.makeText(this, "Can't get a bitmap", Toast.LENGTH_SHORT);
            failed.show();
            return null;
        }

        intentDrop("originalImage", fullBitmap);

        Vector<Vector<String>> ocrReturnedValues = null;

        OCRProcess.loadLanguage("lan", this.getApplicationContext());

        try {
            ImgProcess imgProcess = new ImgProcess(fullBitmap);
            OCRProcess OCR=new OCRProcess(this.getCacheDir().getCanonicalPath(),imgProcess);
            ocrReturnedValues=OCR.getStrings();

            Bitmap linesBit = imgProcess.getLinesImg();
            Bitmap ocrBit = imgProcess.getOCRImg();

            intentDrop("linesImage", linesBit);
            intentDrop("ocrImage", ocrBit);

            Vector<Vector<String>> tmp = new Vector<Vector<String>>(ocrReturnedValues);

            debugIntent.putExtra("ocrOutput", tmp);
        } catch (Exception e) {
            Log.e("LoadImage", e.toString());
            e.printStackTrace();
            debugIntent.putExtra(crashNoString, 0);
            debugIntent.putExtra(crashErrorMsg,e.getMessage());
        }
        Workout gleanedWorkout = null;
        if(ocrReturnedValues != null && ocrReturnedValues.size() > 0) {
            Log.w("OCR OUTPUT", ocrReturnedValues.toString());

            Vector<String> totals = ocrReturnedValues.firstElement();
            ocrReturnedValues.remove(0);
            Vector<Vector<String>> subIntervals = ocrReturnedValues;
            GregorianCalendar workoutTime = new GregorianCalendar();

            WorkoutParser workoutParser = null;
            WorkoutChecker workoutChecker = null;

            try {
                workoutParser = new WorkoutParser(totals, workoutTime, subIntervals);
                workoutChecker = workoutParser.runForWorkoutChecker();
                debugIntent.putExtra("parserOutput",workoutParser.getVVS());
            }catch(Exception e){
                debugIntent.putExtra(crashNoString,1);
                debugIntent.putExtra(crashErrorMsg,e.getMessage());
            }

            try {
                if(workoutParser !=null) {

                    workoutChecker.performAllCorrection(MAX_CORRECTIVE_ITERATIONS, true);

                    gleanedWorkout = workoutChecker.getWorkout();
                    debugIntent.putExtra("checkerOutput", workoutChecker.getVVS());

                }
            }catch(Exception e){
                debugIntent.putExtra(crashNoString,2);
                debugIntent.putExtra(crashErrorMsg,e.getMessage());
            }
//            BolderWorkout bw = new BolderWorkout();
//
//            gleanedWorkout = bw.bolderWorkout(ocrReturnedValues,ImgProcess.workoutType);

            debugIntent.putExtra(crashNoString,4);

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
