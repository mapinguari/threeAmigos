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
import android.widget.Toast;

import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.ImgProcess;
import com.example.mapinguari.workoutclass.database.DatabaseInterface;
import com.example.mapinguari.workoutclass.exceptions.NotHumanStringException;
import com.example.mapinguari.workoutclass.exerciseObjects.ErgoFormatter;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
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
                Intent cornerPickerIntent = new Intent(this,CornerPickerActivity.class);
                cornerPickerIntent.putExtra(getResources().getString(R.string.EXTRA_ERGO_IMAGE),data.getData().toString());
                startActivity(cornerPickerIntent);
                /*
                if (resultCode==RESULT_OK){
                    Uri imageUri=data.getData();

*/
            default:
        }


    }

    //This function is conservative. It assumes that intervals are the last elements in the array
    //It will attempt to take intervals until it gets a human string parse error on a row. it will
    // then take the last successful parse as the totals row and the rest of the intervals as intervals.



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
