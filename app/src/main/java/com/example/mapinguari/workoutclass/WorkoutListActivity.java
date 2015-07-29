package com.example.mapinguari.workoutclass;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import java.util.List;


public class WorkoutListActivity extends ActionBarActivity {

    DatabaseInterface db;
    String INVESTIGATE_WORKOUT_ACTION_NAME = "/*NAME OF ACTION CLASS GOES HERE*/";
    String EXTRA_WORKOUT = getPackageName() + ".";
    ComponentName investigateWorkoutAction = new ComponentName(getPackageName(),INVESTIGATE_WORKOUT_ACTION_NAME);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);
        DatabaseHelper dbH = new DatabaseHelper(this);
        AsyncTask dbT = new DatabaseOpenTask().execute(dbH);
        //TODO: Exception handling
        //databaseBacker
        SQLiteDatabase dbB = null;
        try{
            dbB = (SQLiteDatabase) dbT.get();
        } catch(Throwable tr){}
        db = new DatabaseInterface(dbB);
        Cursor cursor = db.getAllWorkoutsCursor();
        //TODO: danger here, not sure what the last parameter does, 0 doesnt seem to be a flag. null is not acceptable apparently
        WorkoutListAdapter adapter = new WorkoutListAdapter(this,cursor,0);
        ListView listView = (ListView) findViewById(R.id.databaseListView);
        listView.setAdapter(adapter);
        WorkoutClick wc = new WorkoutClick();
        listView.setOnItemClickListener(wc);

    }

    class WorkoutClick implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            WorkoutListItemLayout itemView = (WorkoutListItemLayout) view;
            int workout_ID = itemView.workout_ID;
            Workout workout = db.getWorkOut(workout_ID);
            Intent inspectIntent = new Intent();
            inspectIntent.putExtra(EXTRA_WORKOUT,workout);
            inspectIntent.setComponent(investigateWorkoutAction);
            startActivity(inspectIntent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workout_list, menu);
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
