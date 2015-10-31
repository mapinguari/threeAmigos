package com.example.mapinguari.workoutclass.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.mapinguari.workoutclass.R;

import java.io.File;
import java.io.IOException
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;


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
        Intent CaptureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (CaptureImageIntent.resolveActivity(getPackageManager()) != null) {
            File path=new File(this.getApplicationContext().getFilesDir(),CAPTURE_IMAGE_FILE_PROVIDER_DIR);
            File cacheFile=new File(path,CAPTURE_IMAGE_FILE_PROVIDER_NAME);

            Uri imageURI = FileProvider.getUriForFile(this, CAPTURE_IMAGE_FILE_PROVIDER, cacheFile);
            CaptureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
            CameraURI=Uri.fromFile(cacheFile);

            if (!path.exists()) path.mkdirs();
            if (cacheFile.exists()) cacheFile.delete();

            startActivityForResult(CaptureImageIntent, CAPTURE_IMAGE_REQUEST);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent cornerPickerIntent;
        switch(requestCode){
            case GET_IMAGE_REQUEST:
                if (resultCode==RESULT_OK) {
                    cornerPickerIntent = new Intent(this, CornerPickerActivity.class);
                    cornerPickerIntent.putExtra(getResources().getString(R.string.EXTRA_ERGO_IMAGE), data.getData().toString());
                    startActivity(cornerPickerIntent);
                }
                break;
            case CAPTURE_IMAGE_REQUEST:
                if (resultCode==RESULT_OK) {
                    cornerPickerIntent = new Intent(this, CornerPickerActivity.class);
                    cornerPickerIntent.putExtra(getResources().getString(R.string.EXTRA_ERGO_IMAGE), CameraURI.toString());
                    startActivity(cornerPickerIntent);
                }
                break;
            default:
                return;
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
