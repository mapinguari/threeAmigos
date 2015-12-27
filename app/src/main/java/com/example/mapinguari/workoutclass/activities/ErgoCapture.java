package com.example.mapinguari.workoutclass.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ErgoCapture extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private String TAG = "Camera thing";
    private ImageView rectangle;
    private FrameLayout previewHolder;
    private File outFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ergo_capture);

        outFile = new File(this.getFilesDir(),"1.jpg");

        // Create an instance of Camera
        mCamera = getCameraInstance();
        setCameraDisplayOrientation(this,0,mCamera);
        rectangle = (ImageView) findViewById(R.id.view_finder_rect);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        previewHolder = preview;
        preview.addView(mPreview);


        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.autoFocus(null);
                        Toast a = Toast.makeText(ErgoCapture.this,mCamera.getParameters().getFocusMode().toString(),Toast.LENGTH_LONG);
                        a.show();
                        Camera.Parameters p = mCamera.getParameters();

                        p.setPictureSize(p.getPreviewSize().width,p.getPreviewSize().height);
                        mCamera.setParameters(p);
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ergo_capture, menu);
        return true;
    }

    private int area(Camera.Size s){
        return (s.width * s.height);
    }

    private Camera.Size largest(List<Camera.Size> css){
        Camera.Size largest = css.get(0);
        for(Camera.Size s : css){
            if(area(s) > area(largest)){
                largest = s;
            }
        }
        return largest;
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

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            Camera.Parameters p = c.getParameters();
            p.setRotation(90);
            c.setParameters(p);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;
        private double biggest;


        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.android
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        private void bestAsp(Camera camera){
            Camera.Parameters p = mCamera.getParameters();
            List<Camera.Size> lcs = p.getSupportedPreviewSizes();
            double aspect = ((double) this.getHeight()) / (double) this.getWidth();
            double bestAspect = 0;
            Camera.Size bestSize = null;
            double currAspect;
            for(Camera.Size cs : lcs){
                currAspect = ((double) cs.height) / ((double) cs.width);
                if(Math.abs(aspect - currAspect) < (Math.abs(aspect - bestAspect))){
                    bestAspect = currAspect;
                    bestSize = cs;
                }
            }
            p.setPreviewSize(bestSize.width, bestSize.height);
            p.setPictureSize(bestSize.width, bestSize.height);
            mCamera.setParameters(p);
        }

        private void biggest(Camera camera){

            Camera.Parameters p = mCamera.getParameters();
            List<Camera.Size> lcs = p.getSupportedPreviewSizes();

            double biggest = 0;
            Camera.Size biggestSize = null;
            double currSize;
            for(Camera.Size cs : lcs){
                currSize = ((double) cs.height) * ((double) cs.width);
                if(currSize > biggest){
                    biggest = currSize;
                    biggestSize = cs;
                }
            }
            this.biggest = biggest;
            p.setPreviewSize(biggestSize.width, biggestSize.height);
            p.setPictureSize(biggestSize.width, biggestSize.height);
            mCamera.setParameters(p);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                biggest(mCamera);
                Toast a = Toast.makeText(ErgoCapture.this,Double.toString(biggest), Toast.LENGTH_LONG);
                a.show();
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = outFile;
            Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);

            //get the dimensions of the rectangle relative to the view it is contained in
            float Rx = rectangle.getLeft();
            float Ry = rectangle.getTop();
            float Rw = rectangle.getWidth();
            float Rh = rectangle.getHeight();

            //get the dimensions of the parent view of the rectangle
            View vp = previewHolder;
            float vpW = vp.getWidth();
            float vpH = vp.getHeight();

            Camera.Size cS = mCamera.getParameters().getPictureSize();

            float pW = cS.height;
            float pH = cS.width;

            float horLam = pW / vpW;
            float verLam = pH / vpH;

            int trueW = (int) (Rw * horLam);
            int trueH = (int) (Rh * verLam);
            int trueX = (int) (Rx * horLam);
            int trueY = (int) (Ry * verLam);


//            (xmid - (trueW / 2))
//            (ymid - (trueH / 2))


            Bitmap outB = Bitmap.createBitmap(b,trueX,trueY,trueW,trueH);

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                outB.compress(Bitmap.CompressFormat.JPEG,100,fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }


            Intent a = new Intent(ErgoCapture.this,CornerPickerActivity.class);
            a.putExtra(getResources().getString(R.string.EXTRA_ERGO_IMAGE), (Uri.fromFile(outFile)).toString());
            startActivity(a);
        }

    };

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }



}
