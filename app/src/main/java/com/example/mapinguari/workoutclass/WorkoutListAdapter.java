package com.example.mapinguari.workoutclass;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.mapinguari.workoutclass.database.DatabaseSchema;
import com.example.mapinguari.workoutclass.exerciseObjects.GregtoString;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.Arrays;
import java.util.GregorianCalendar;

/**
 * Created by mapinguari on 7/28/15.
 */
public class WorkoutListAdapter extends CursorAdapter {


    private LayoutInflater mInflater;

    public WorkoutListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.workout_list_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        WorkoutListItemLayout iv = (WorkoutListItemLayout) view;
        String[] columnNames = cursor.getColumnNames();
        int idC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getID());
        int dateC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getColumnNameCompletedTime());
        int distanceC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getColumnNameDistance());
        int timeC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getColumnNameTime());
        int SPMC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getColumnNameAverageSpm());

        int id = cursor.getInt(idC);
        Double dist = cursor.getDouble(distanceC);
        String date = cursor.getString(dateC);
        Double time = cursor.getDouble(timeC);
        Integer SPM = cursor.getInt(SPMC);
        Workout pretend = new Workout(null,SPM,dist,time,GregtoString.getGregCal(date));

        TextView dateField = (TextView) iv.findViewById(R.id.List_date);
        TextView distanceField = (TextView) iv.findViewById(R.id.List_distance);
        TextView splitField = (TextView) iv.findViewById(R.id.List_split);

        iv.workout_ID = id;
        dateField.setText(pretend.getHumanDate(context));
        distanceField.setText(Integer.toString(pretend.getDistance().intValue()));
        splitField.setText(pretend.getHumanSplit());
        
        if(cursor.getPosition()%2==1) {
            view.setBackgroundColor(context.getResources().getColor(R.color.odd_list_item));
        }
        else {
            view.setBackgroundColor(context.getResources().getColor(R.color.even_list_item));
        }
        
    }

    private Integer getColumn(String[] columnNames, String name) {
        return Arrays.asList(columnNames).indexOf(name);
    }

    //h:m.s
    //m:s.ms

}
