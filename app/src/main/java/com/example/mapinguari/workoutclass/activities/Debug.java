package com.example.mapinguari.workoutclass.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mapinguari.workoutclass.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Debug extends AppCompatActivity {

    Intent startingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        TextView errorView = (TextView) findViewById(R.id.debug_error_msg_tv);

        startingIntent = this.getIntent();
        int errorNo = startingIntent.getIntExtra("CrashNo", 4);
        String errorMSG = startingIntent.getStringExtra("CrashMsg");

        if(errorNo > 0){
            ImageView originalImageView = (ImageView) findViewById(R.id.debug_original_imageview);
            originalImageView.setImageURI(((Uri) startingIntent.getParcelableExtra("originalImage")));

            ImageView blobImageView = (ImageView) findViewById(R.id.debug_blobs_imageview);
            blobImageView.setImageURI(((Uri) startingIntent.getParcelableExtra("linesImage")));

            ImageView ocrImageView = (ImageView) findViewById(R.id.debug_ocr_imageview);
            ocrImageView.setImageURI(((Uri) startingIntent.getParcelableExtra("blobImage")));

            checkSet(R.id.debug_ocr_output_textview, "ocrOutput");
        }

        if(errorNo > 1) {
            checkSet(R.id.debug_parsing_output_textview,"parserOutput");


        }

        if(errorNo > 2) {
            checkSet(R.id.debug_correction_output_textview,"checkerOutput");

        }

        if(errorNo < 4){
            errorView.setText(errorMSG);
        }


    }

    private void checkSet(int viewId,String extraKey){
        TextView tv = (TextView) findViewById(viewId);
        ArrayList<ArrayList<String>> lls = (ArrayList<ArrayList<String>>) startingIntent.getSerializableExtra(extraKey);
        if(lls != null)
            tv.setText((CharSequence) (lls.toString()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug, menu);
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
