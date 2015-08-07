package com.example.mapinguari.workoutclass.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapinguari.workoutclass.database.DatabaseInterface;
import com.example.mapinguari.workoutclass.exerciseObjects.Interval;
import com.example.mapinguari.workoutclass.R;
import com.example.mapinguari.workoutclass.exerciseObjects.Workout;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;


public class ManualInputActivity extends ActionBarActivity {

    TableLayout tableLayout;
    Workout newworkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView sv = new ScrollView(this);
        String[] column = {"Interval  ", "  Time  ", "  Power  ", "  SPM  ", "  Rest  "};
        int rl= 18;
        int cl=column.length;

        tableLayout = createTableLayout( column,rl, cl);
        HorizontalScrollView hsv = new HorizontalScrollView(this);

        hsv.addView(tableLayout);
        sv.addView(hsv);
        setContentView(sv);

    }

    public void setHeaderTitle(TableLayout tableLayout, int rowIndex, int columnIndex, String text){

        // get row from table with rowIndex
        TableRow tableRow = (TableRow) tableLayout.getChildAt(rowIndex);

        // get cell from row with columnIndex
        TextView textView = (TextView)tableRow.getChildAt(columnIndex);

        textView.setText("" + text);
    }

    private TableLayout createTableLayout(String [] cv,int rowCount, int columnCount) {
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
        TableLayout tableLayout = new TableLayout(this);
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
        final int InputID = 1;
        for (int i = 0; i <= rowCount; i++) {
            TableRow tableRow = new TableRow(this);
            for (int j = 0; j < columnCount; j++) {

                TextView textView = new TextView(this);
                EditText textEdit = new EditText(this);

                if(i==0) {
                    textView.setText(cv[j]);
                    tableRow.addView(textView, tableRowParams);
                }else {
                    if (j == 0) {
                        if (i == rowCount) {
                            Button button = new Button(this);
                            button.setText("Save");
                            button.setOnClickListener(buttonListener);
                            tableRow.addView(button, tableRowParams);
                        } else if (i == 1) {
                            textView.setText("Average");
                            tableRow.addView(textView, tableRowParams);
                        } else {
                            int IntNumber = i - 1;
                            textView.setText("" + IntNumber);
                            tableRow.addView(textView, tableRowParams);
                        }
                    } else if (i != rowCount) {
                        tableRow.addView(textEdit, tableRowParams);
                        textEdit.setText( "" + 10);
                    }
                }

            }

            tableLayout.addView(tableRow, tableLayoutParams);
        }
        return tableLayout;
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            List<Interval> inputintervals = new ArrayList<Interval>();

            for (int i = 2; i < 8; i++) {
                TableRow row = (TableRow) tableLayout.getChildAt(i);
                EditText Time = (EditText) row.getChildAt(1);
                String Timetext = Time.getText().toString();
                Double DTime = Double.parseDouble(Timetext);

                EditText Power = (EditText) row.getChildAt(2);
                String Powertext = Power.getText().toString();
                Double DPower = Double.parseDouble(Powertext);


                EditText SPM = (EditText) row.getChildAt(3);
                String SPMtext = SPM.getText().toString();
                int DSPM = Integer.parseInt(SPMtext);

                EditText Rest = (EditText) row.getChildAt(4);
                String Resttext = Rest.getText().toString();
                Double DRest = Double.parseDouble(Resttext);

                //Double DTime = 1.222;
                //Double DPower = 1.222;
                //int DSPM = 1;
                //Double DRest = 1.222;

                Interval RowInterval = new Interval(DTime, DPower, DSPM, DRest);
                inputintervals.add(i - 2, RowInterval);

            }

            TableRow row = (TableRow) tableLayout.getChildAt(1);
            EditText Time = (EditText) row.getChildAt(1);
            String Timetext = Time.getText().toString();
            Double DTime = Double.parseDouble(Timetext);

            EditText Power = (EditText) row.getChildAt(1);
            String Powertext = Power.getText().toString();
            Double DPower = Double.parseDouble(Powertext);

            EditText SPM = (EditText) row.getChildAt(1);
            String SPMtext = SPM.getText().toString();
            int DSPM = Integer.parseInt(SPMtext);

            //Double DTime = 1.222;
            //  Double DPower = 1.222;
            //int DSPM = 1;
            //Double DRest = 1.222;

            GregorianCalendar date = new GregorianCalendar();

            newworkout = new Workout(inputintervals, DSPM, DPower, DTime, date);
            DatabaseInterface db = new DatabaseInterface(ManualInputActivity.this);

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inout_test, menu);
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

    public Workout getinputdata(){
        return newworkout;
    }
}
