package com.example.mapinguari.workoutclass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.ImgProcess;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;


public class MainMenuActivity extends ActionBarActivity {
    static final int GET_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

    }

    public void GoToInputActivity (View v){
        startActivity(new Intent(getApplicationContext(), WorkoutViewActivity.class));
    }

    public void GoToInspect (View v){
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case GET_IMAGE_REQUEST:
                if (resultCode==RESULT_OK){
                    Uri imageUri=data.getData();
                    Vector<Vector<String>> ocrReturnedValues;

                    ImgProcess.loadLanguage("lan",this.getApplicationContext());

                    try {
                        ocrReturnedValues=ImgProcess.ProcessImage(
                                MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri),
                                this.getCacheDir().getCanonicalPath());
                    } catch (Exception e) {
                        Log.e("LoadImage", e.toString());
                        e.printStackTrace();
                    }
                    //do something with returned values here
                }

            default:
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
