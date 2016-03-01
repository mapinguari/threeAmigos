package com.example.mapinguari.workoutclass.ergoGridView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.mapinguari.workoutclass.ergoGrids.PM3;
import com.example.mapinguari.workoutclass.ergoGrids.PM3plus;

import java.util.ArrayList;

/**
 * Created by mapinguari on 2/29/16.
 */
public class PM3PlusView extends View{

    private Paint overViewPaint;
    private Paint datePaint;
    private Paint headersPaint;
    private Paint summaryPaint;
    private Paint intervalsPaint;
    private Paint edgePaint;

    PM3plus grid;

    public PM3PlusView(Context context) {
        super(context);
        init();
    }

    public PM3PlusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public PM3PlusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        grid = new PM3(w,h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(grid.getBoundingRect(), edgePaint);
        canvas.drawRect(grid.getOverView(),overViewPaint);
        //canvas.drawRect(grid.getTimeDate(),datePaint);
        for(Rect r: grid.getHeaderRects()){
            canvas.drawRect(r,headersPaint);
        }
        for(Rect r: grid.getSummaryRects()){
            canvas.drawRect(r,summaryPaint);
        }
        for(ArrayList<Rect> ar : grid.getIntervalRects()){
            for(Rect r : ar){
                canvas.drawRect(r,intervalsPaint);

            }

        }


    }

    private void init(){
        edgePaint = new Paint();
        edgePaint.setColor(Color.RED);
        edgePaint.setStrokeWidth(20);
        edgePaint.setStyle(Paint.Style.STROKE);
        overViewPaint = new Paint();
        overViewPaint.setColor(Color.BLUE);
        overViewPaint.setStrokeWidth(2);
        overViewPaint.setStyle(Paint.Style.STROKE);
        datePaint = overViewPaint;
        headersPaint =  new Paint(overViewPaint);
        headersPaint.setColor(Color.YELLOW);
        summaryPaint = new Paint(overViewPaint);
        summaryPaint.setColor(Color.GREEN);
        intervalsPaint = new Paint(overViewPaint);
        intervalsPaint.setColor(Color.WHITE);

        grid = new PM3(0,0);
    }


}
