package com.example.mapinguari.workoutclass.cameraClasses;

import com.example.mapinguari.workoutclass.mObjects.Line;
import com.example.mapinguari.workoutclass.mObjects.Point;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by mapinguari on 4/13/16.
 */
public class ErgoScreenChecker implements Camera.PreviewCallback{

    int rectsDistance;
    Rect checkRect;
    Rect outRect;
    int noOfCheckLines;
    int imWidth;
    int imHeight;
    ScreenInterfaceFinder sif;
    SurfaceView drawView;
    Context context;


    public ErgoScreenChecker(int rectsDistance, Rect innerRect, View rectParent,Camera cs, int noOfCheckLines, ScreenInterfaceFinder sif,SurfaceView graphicsView,Context context) {
        this.rectsDistance = rectsDistance;
        this.checkRect = fixRectangle(innerRect,rectParent.getWidth(),rectParent.getHeight(),
                cs.getParameters().getPictureSize().height,cs.getParameters().getPictureSize().width);
        outRect = new Rect(checkRect.left -rectsDistance,checkRect.top - rectsDistance, checkRect.right + rectsDistance, checkRect.bottom + rectsDistance);
        this.noOfCheckLines = noOfCheckLines;
        this.sif = sif;
        imWidth = cs.getParameters().getPictureSize().width;
        imHeight = cs.getParameters().getPictureSize().height;
        drawView = graphicsView;
        this.context = context;
    }

    //This function will find interface points for top and bottom
    public ArrayList<Point<Integer>> getColPoints(byte[] data, int xStart,int yStart,int xFinish, int yFinish){
        ArrayList<Point<Integer>> result = new ArrayList<>(noOfCheckLines);
            int gapSize;
            int cur;
            gapSize = (xFinish - xStart) / (noOfCheckLines - 1);
            cur = xStart;
            byte[] currentData;
            gapSize = gapSize > 0 ? gapSize : 1;
            while (cur <= xFinish) {
                currentData = getIntermediates(data, cur, yStart, cur, yFinish);
                result.add(new Point(cur,yStart + sif.findInterfacePoint(currentData)));
                cur += gapSize;
            }
        return result;
    }

    //this function will find interface points for left and right
    public ArrayList<Point<Integer>> getRowPoints(byte[] data, int xStart,int yStart,int xFinish, int yFinish){
        ArrayList<Point<Integer>> result = new ArrayList<>(noOfCheckLines);
        int gapSize;
        int cur;
        byte[] currentData;
        int diff;

        gapSize = (yFinish - yStart) / (noOfCheckLines - 1);
        cur = yStart;
        gapSize = gapSize > 0 ? gapSize : 1;

        while (cur <= yFinish) {
            currentData = getIntermediates(data, xStart,cur, xFinish, cur);
            diff = sif.findInterfacePoint(currentData);
            result.add(new Point(xStart + diff , cur ));
            cur += gapSize;
        }
        return result;
    }


    private byte[] getIntermediates(byte[] data, int x1, int y1, int x2, int y2){
        byte[] result;
        if (y1 == y2){
            int xm,xM;
            xm = Math.min(x1,x2);
            xM = Math.max(x1,x2);
            result = new byte[(xM - xm) + 1];
            int j = 0;
            for(int i = y1*imWidth + xm; i <= xM;i++){
                result[j] = data[i];
                j++;
            }
        } else if(x1 == x2){
            int ym,yM;
            ym = Math.min(y1,y2);
            yM = Math.max(y1,y2);
            result = new byte[(yM - ym) + 1];
            int j = 0;
            for(int i = ym*imWidth + x1; i <= yM*imWidth + x1;i = i + imWidth){
                result[j] = data[i];
                j++;
            }
        } else {
            result = null;
        }
        return result;
    }

    private Rect fixRectangle(Rect current,float domVert, float domHor, float imVert, float imHor) {
        //get the dimensions of the rectangle relative to the view it is contained in
        float Rx = current.left;
        float Ry = current.right;
        float Rw = current.width();
        float Rh = current.height();

        //get the dimensions of the parent view of the rectangle
        float vpW = domHor;
        float vpH = domVert;

        float pW = imHor;
        float pH = imVert;

        float horLam = pW / vpW;
        float verLam = pH / vpH;

        int trueW = (int) (Rw * horLam);
        int trueH = (int) (Rh * verLam);
        int trueX = (int) (Rx * horLam);
        int trueY = (int) (Ry * verLam);
        return new Rect(trueX,trueY,trueX+trueW,trueY+trueH);

    }

    public Line bestFit(ArrayList<Point<Integer>> points){
        int[] ys = yValues(points);
        int[] xs = xValues(points);
        double xMean = mean(xs);
        double yMean = mean(ys);
        double grad = (mean2(xs,ys) - xMean*yMean) / (mean2(xs,xs) - xMean*xMean);
        double constant =  yMean - grad*xMean;
        return new Line(grad,constant);
    }

    public int[] xValues(ArrayList<Point<Integer>> points){
        int[] res = new int[points.size()];
        for(int i = 0; i < points.size(); i++){
            res[i] = points.get(i).x;
        }
        return res;
    }


    public int[] yValues(ArrayList<Point<Integer>> points){
        int[] res = new int[points.size()];
        for(int i = 0; i < points.size(); i++){
            res[i] = points.get(i).y;
        }
        return res;
    }

    public boolean reasonablePoint(Point<Integer> p){
        return outRect.contains(p.x,p.y) && !checkRect.contains(p.x,p.y);

    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        //TODO: BE  AWARE I AM NOT HANDLING NULL POINTS PASSED BACK HERE! Next section likely to crash activity.
        ArrayList<Point<Integer>> lefts = getRowPoints(data,checkRect.left - rectsDistance,checkRect.top,checkRect.left,checkRect.bottom);
        ArrayList<Point<Integer>> rights = getRowPoints(data,checkRect.right,checkRect.top,checkRect.right + rectsDistance,checkRect.bottom);
        ArrayList<Point<Integer>> tops = getColPoints(data, checkRect.left, checkRect.top - rectsDistance, checkRect.right, checkRect.top);
        ArrayList<Point<Integer>> bottoms = getColPoints(data, checkRect.left, checkRect.bottom, checkRect.right, checkRect.bottom + rectsDistance);

        //DAVID! If you plan on drawing your points back to the screen via the drawView be aware, there is a coordinate transformation to perform.
        // The camera preview (the picture you see live on the screen) comes in a predefined set of sizes. Therefore it is stretched to fit in the
        // View which is holding it. For all of these points above they are currently in ImgCoords, not ViewCoords. So you need figure out the
        // factor by which to multiply the points by to transform them back to what will be correct in the streched image. I think for x it is
        // viewWidth/previewWidth, and similar for height. I will do these calculations myself for my own drawing, I just havent put them up yet,
        // If you are ahead of me on this just be aware of that.

        Line left = bestFit(lefts);
        Line right = bestFit(rights);
        Line bottom = bestFit(bottoms);
        Line top = bestFit(tops);
        Point topleft = left.intersection(top);
        Point topright = top.intersection(right);
        Point bottomright = right.intersection(bottom);
        Point bottomleft = bottom.intersection(left);


        float xlam = drawView.getWidth() / camera.getParameters().getPreviewSize().width;
        float ylam = drawView.getHeight() / camera.getParameters().getPreviewSize().height;


        topleft = new Point(xlam * (float) topleft.x, ylam * (float) topleft.y);
        topright = new Point(xlam * (float) topright.x, ylam * (float) topright.y);
        bottomleft = new Point(xlam * (float) bottomleft.x, ylam * (float) bottomleft.y);
        bottomright = new Point(xlam * (float) bottomright.x, ylam * (float) bottomright.y);


        boolean reasonableRect = reasonablePoint(topleft) && reasonablePoint(topright) && reasonablePoint(bottomleft) && reasonablePoint(bottomright);
        if(reasonableRect){
            //CONSIDER MOVING ALL THIS DRAWING LOGIC TO A SEPERATE THREAD!

            SurfaceHolder sh = drawView.getHolder();
            Canvas drawToMe = sh.lockCanvas();
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(context.getResources().getColor(android.R.color.holo_orange_light));

            drawToMe.drawLine((float) topleft.x, (float) topleft.y, (float) topright.x, (float) topright.y, paint);
            drawToMe.drawLine((float) topright.x,(float) topright.y,(float) bottomright.x,(float) bottomright.y,paint);
            drawToMe.drawLine((float) topleft.x,(float) topleft.y,(float) bottomleft.x,(float) bottomleft.y,paint);
            drawToMe.drawLine((float) bottomleft.x,(float) bottomleft.y,(float) bottomright.x,(float) bottomright.y,paint);

            sh.unlockCanvasAndPost(drawToMe);
        }
    }

    public double mean(int[] vs){
        int sum = 0;
        for(int i = 0; i < vs.length;i++){
            sum += vs[i];
        }
        return ((double) sum) / ((double) vs.length);
    }

    public double mean2(int[] fs, int[] ss){
        int sum = 0;
        for(int i = 0; i < fs.length; i++){
            sum += (fs[i] * ss[i]);
        }
        return ((double) sum) / ((double) fs.length);
    }
}
