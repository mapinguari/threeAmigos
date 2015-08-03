package com.example.mapinguari.workoutclass;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;


public class InspectActivity extends ActionBarActivity {

    Workout TrialW = trialworkout() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] column = {"Interval  ", "  Time  ", "  Power  ", "  SPM  ", "  Rest  "};
        int rl= 1+ TrialW.getIntervalList().size();
        int cl=column.length;

        ScrollView sv = new ScrollView(this);
        TableLayout tableLayout = createTableLayout( column,rl, cl);
        HorizontalScrollView hsv = new HorizontalScrollView(this);

        hsv.addView(tableLayout);
        sv.addView(hsv);
        setContentView(sv);

    }

    private TableLayout createTableLayout(String [] cv,int rowCount, int columnCount) {
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
        TableLayout tableLayout = new TableLayout(this);
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();

        List<Interval> IntervalList;
        IntervalList = TrialW.getIntervalList();

        for (int i = 0; i <= rowCount; i++) {
            TableRow tableRow = new TableRow(this);
            for (int j = 0; j < columnCount; j++) {
                TextView textView = new TextView(this);
                textView.setBackgroundColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);

                if(i==0) {
                    textView.setText(cv[j]);
                }
                else if(i==1){
                    if(j==0){
                        textView.setText("Average");
                    }
                    else if(j==1){
                        textView.setText("" + TrialW.getTotalTime());
                    }
                    else if(j==2){
                        textView.setText("" + TrialW.getAverageWatts());
                    }
                    else if(j==3) {
                        textView.setText("" + TrialW.getAverageSPM());
                    }
                } else if(i > 1){
                    Interval RowInterval = IntervalList.get(i-2);
                    if (j == 0) {
                        int IntNumber = i-1;
                        textView.setText("" + IntNumber);
                    } else if (j == 1) {
                        textView.setText("" + RowInterval.getWorkTime());
                    } else if (j == 2) {
                        textView.setText("" + RowInterval.getAverageWatts());
                    } else if (j == 3) {
                        textView.setText("" + RowInterval.getAverageSPM());
                    } else if (j == 4) {
                        textView.setText("" + RowInterval.getRestTime());
                    }
                }

                tableRow.addView(textView, tableRowParams);
            }

            tableLayout.addView(tableRow, tableLayoutParams);
        }

        return tableLayout;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_testinput, menu);
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

    public Workout trialworkout(){
        Workout Trial;
        GregorianCalendar date = new GregorianCalendar();
        Interval one;
        one = new Interval(100.2,220.0,20,2.31);
        Interval two;
        two =  new Interval(100.2,220.0,21,2.32);
        Interval three;
        three =  new Interval(100.2,220.0,22,2.33);
        Interval four;
        four =  new Interval(100.2,220.0,23,2.34);
        List<Interval> Trialint;
        Trialint = Arrays.asList(one, two, three, four);
        Trial = new Workout(Trialint,22,220.0,400.8,date);

        return Trial;
    }
}
