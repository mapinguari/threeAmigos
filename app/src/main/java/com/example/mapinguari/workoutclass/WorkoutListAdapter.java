package com.example.mapinguari.workoutclass;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Arrays;

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
        int wattsC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getColumnNameAverageWatts());
        int timeC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getColumnNameTime());
        int SPMC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getColumnNameAverageSpm());
        int distC = getColumn(columnNames, DatabaseSchema.DataBaseTerms.getColumnNameDistance());

        int id = cursor.getInt(idC);
        String split = Interval.calcSplit(cursor.getDouble(wattsC));
        String date = cursor.getString(dateC);
        String time = secsToErgoString(cursor.getDouble(timeC));
        String SPM = Integer.toString(cursor.getInt(SPMC));
        String dist = Integer.toString(cursor.getInt(distC));

        TextView dateField = (TextView) iv.findViewById(R.id.List_date);
        TextView distanceField = (TextView) iv.findViewById(R.id.List_distance);
        TextView splitField = (TextView) iv.findViewById(R.id.List_split);

        iv.workout_ID = id;
        dateField.setText(date);
        distanceField.setText(dist);
        splitField.setText(split);
        
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
    String secsToErgoString(Double time) {
        double hours = time / 3600;
        double hoursRem = time % 3600;
        double mins = hoursRem / 60;
        double minsRem = hoursRem % 60;
        double secs = Math.floor(minsRem);
        double a,b,c;
        if (hours > 0) {
            a = hours;
            b = mins;
            c = secs;
        }
        else {
            a = mins;
            b = secs;
            c = Math.round((secs % 1) * 10);
        }
        return ( a + ":" + b + "." + c);
    }
}
