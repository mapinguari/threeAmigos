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
        int width = ergoImageView.getWidth();
        int height = ergoImageView.getHeight();
        Intent sIntent = getIntent();

        imgURI = Uri.parse(sIntent.getStringExtra(getResources().getString(R.string.EXTRA_ERGO_IMAGE)));

        ergoImageView.setImageURI(imgURI);
        ergoImageView.setOnTouchListener(new CornerAddTouch());


        //mGestureDetector = new GestureDetector(this,new TouchInteractions());
        cornerPoints = new ArrayList<View>(4);
    }

    class CornerAddTouch implements View.OnTouchListener {
        float xentry;
        float yentry;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case (MotionEvent.ACTION_UP):{
                    if (!lastWasMove) {
                        float x = event.getX();
                        float y = event.getY();
                        addCornerView(x, y);

                    }
                    if (movingView != null) {
                        GradientDrawable circle = (GradientDrawable) movingView.getBackground();
                        circle.setColor(getResources().getColor(R.color.blue));
                        movingView = null;
                    }
                    movingView = null;
                    lastWasMove = false;
                    break;
                }
                case (MotionEvent.ACTION_DOWN): {
                    View currentView = null;
                    xentry = event.getX();
                    yentry = event.getY();
                    double dist;
                    double shortestdist = 10000000000.0;
                    for (View tV : cornerPoints) {
                        dist = Math.sqrt(Math.pow((double) getcenterX(tV) - xentry, 2.0) + Math.pow((double) getcenterY(tV) - yentry, 2));

                        if (dist < shortestdist) {
                            shortestdist = dist;
                            currentView = tV;
                            Log.d("currentView ", currentView.toString());
                        }
                    }
                    movingView = currentView;
                    if (movingView != null) {
                        GradientDrawable circle = (GradientDrawable) movingView.getBackground();
                        circle.setColor(getResources().getColor(R.color.red));
                    }
                    lastWasMove = false;
                    break;
                }
                case (MotionEvent.ACTION_MOVE): {
                        float x = event.getX();
                        float y = event.getY();
                    if(xentry != x || yentry != y) {
                        movingView.setX(x);
                        movingView.setY(y);
                        xentry = x;
                        yentry = y;
                        lastWasMove = true;
                    }
                    break;
                }
            }
            return true;

        }
    }

    public void addCornerView(float x, float y){
        if(cornerPointsCount < 4) {
            FrameLayout view = new FrameLayout(this);
            //TODO: SIZING HERE NEEDS TO BE SORTED OUT
            view.setLayoutParams(new ViewGroup.LayoutParams(40, 40));
            view.setBackgroundResource(R.drawable.circle);
            relativeLayout.addView(view);
            view.setX(x);
            view.setY(y);
            cornerPointsCount++;
            cornerPoints.add(view);
            GradientDrawable circle = (GradientDrawable) view.getBackground();
            circle.setColor(getResources().getColor(R.color.blue));
            if(cornerPointsCount == 4){
                saveButton.setVisibility(View.VISIBLE);
            }
        }

    }

    public void saveCorners(View v){
        View closestView = null;
        double dist;
        double shortestdist = 10000000000.0;
        for(View tV: cornerPoints){
            dist = Math.sqrt(Math.pow((double) getcenterX(tV),2.0) + Math.pow((double) getcenterY(tV),2));
            if(dist < shortestdist) {
                shortestdist = dist;
                closestView = tV;
            }

        }
        x1 = (int) getcenterX(closestView);
        y1 = (int) getcenterY(closestView);
        cornerPoints.remove(closestView);
        View bl = null;
        int blx = 1000000;
        View tr = null;
        int trY = 1000000;
        for(View tV: cornerPoints){
            if(getcenterX(tV) < blx){
                bl = tV;
            }
            if(getcenterY(tV) < trY){
                tr = tV;
            }
        }
        x2 = (int) getcenterX(tr);
        y2 = (int) getcenterY(tr);
        x4 = (int) getcenterX(bl);
        y4 = (int) getcenterY(bl);
        cornerPoints.remove(tr);
        cornerPoints.remove(bl);
        x3 = (int) getcenterX(cornerPoints.get(0));
        y3 = (int) getcenterY(cornerPoints.get(0));

        Log.d("About to rotate and ocr","");

        Workout gleanedWorkout = rotateANDOCR();
        Intent inspectWorkout = new Intent(getApplicationContext(),WorkoutViewActivity.class);
        inspectWorkout.putExtra(getResources().getString(R.string.EXTRA_WORKOUT),gleanedWorkout);
        inspectWorkout.putExtra(getResources().getString(R.string.EXTRA_WORKOUT_PASSED),true);
        startActivity(inspectWorkout);

    }

    private Workout rotateANDOCR(){
        Bitmap fullBitmap = null;
        try {
            fullBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgURI);
        } catch(Exception e){
            Toast failed = Toast.makeText(this, "Can't get a bitmap", Toast.LENGTH_SHORT);
            failed.show();
            return null;
        }
        Bitmap transformedBitmap;
        transformedBitmap = ImageTransform.TransformAreaToSquare(fullBitmap,x1,x2,x4,x3,y1,y2,y4,y3);
        Vector<Vector<String>> ocrReturnedValues = null;

        ImgProcess.loadLanguage("lan", this.getApplicationContext());

        try {
            ocrReturnedValues= ImgProcess.ProcessImage(transformedBitmap,
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



    private Bitmap correctlySizedImage(String filePath,int reqWidth, int reqHeight ){

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
