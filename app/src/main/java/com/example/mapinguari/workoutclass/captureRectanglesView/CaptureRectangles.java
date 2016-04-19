package com.example.mapinguari.workoutclass.captureRectanglesView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.example.mapinguari.workoutclass.ergoGrids.PM3;

/**
 * Created by mapinguari on 4/13/16.
 */
public class CaptureRectangles extends View {

    Rect inner,outer;

    int marginSize = 30;

    private void init(){
        inner = new Rect(0,0,1,1);
        outer = new Rect(0,0,1,1);
    }


    public CaptureRectangles(Context context) {
        super(context);
        init();
    }

    public CaptureRectangles(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CaptureRectangles(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        outer = new Rect(0,0,w,h);
        inner = new Rect(marginSize,marginSize,w-marginSize,h-marginSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint outerPaint = new Paint();
        outerPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
        outerPaint.setStrokeWidth(5);
        outerPaint.setStyle(Paint.Style.STROKE);
        Paint innerPaint = new Paint();
        innerPaint.setColor(getResources().getColor(android.R.color.holo_blue_bright));
        innerPaint.setStrokeWidth(5);
        innerPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(outer, outerPaint);
        canvas.drawRect(inner,innerPaint);

    }
}
