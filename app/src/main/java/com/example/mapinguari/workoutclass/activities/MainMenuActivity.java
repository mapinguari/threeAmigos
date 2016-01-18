package com.example.mapinguari.workoutclass.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.mapinguari.workoutclass.R;


public class MainMenuActivity extends ActionBarActivity {
    static final int GET_IMAGE_REQUEST = 1;
    static final int CAPTURE_IMAGE_REQUEST = 2;
    static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.example.mapinguari.fileprovider";
    static final String CAPTURE_IMAGE_FILE_PROVIDER_DIR="captured_images";
    static final String CAPTURE_IMAGE_FILE_PROVIDER_NAME="image.jpg";

    Uri CameraURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void GoToInputActivity (View v){
        startActivity(new Intent(getApplicationContext(), WorkoutViewActivity.class));
    }

    public void GoToInspect (View v) {
        startActivity(new Intent(getApplicationContext(), WorkoutListActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    public void LoadImage (View v){
        Intent LoadImageIntent = new Intent(Intent.ACTION_GET_CONTENT );
        LoadImageIntent.addCategory(Intent.CATEGORY_OPENABLE);
        LoadImageIntent.setType("image/*");
        startActivityForResult(LoadImageIntent, GET_IMAGE_REQUEST);
    }

    public void CaptureImage (View v){
        Intent captureIntent = new Intent(this, ErgoCapture.class);
        startActivity(captureIntent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Intent cornerPickerIntent;
        Uri bitmapURI = null;
        String ergoScreenName = null;
            if(resultCode == RESULT_OK){
                switch (requestCode) {
                    case GET_IMAGE_REQUEST:
                        bitmapURI = data.getData();
                        break;
                    case CAPTURE_IMAGE_REQUEST:
                        bitmapURI = CameraURI;
                }
                try {

                    cornerPickerIntent = new Intent(this, PhotoInspection.class);
                    cornerPickerIntent.putExtra(getResources().getString(R.string.EXTRA_ERGO_IMAGE), bitmapURI.toString());
                    startActivity(cornerPickerIntent);
                }catch(Exception e){
                    e.printStackTrace();
                    Log.w("FileURI", bitmapURI.toString());
                }

            }

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
