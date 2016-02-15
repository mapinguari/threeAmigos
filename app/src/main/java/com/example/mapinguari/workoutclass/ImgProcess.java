package com.example.mapinguari.workoutclass;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import android.graphics.Bitmap;
import android.util.Log;

public class ImgProcess {
    private static final int black = 0xFF000000;
    private static final int white = 0xFFFFFFFF;

    private int imageHeight,imageWidth;
    private final int[] imageToProcess;
    private int[] lightImage;
    private int[] blobImage;
    private int[] OCRImage;

    private int leftMargin,rightMargin,topMargin,bottomMargin;
    private int[] textLines;
    private int[] solidLines;
    private int[][] columns;


    public class LineSpecifier {
        public final int leftMargin,rightMargin;
        public final int[] textLines;
        public final int[] solidLines;

        public final int[][] columns;

        LineSpecifier(int lMargin, int rMargin, int[] tLines, int[] sLines, int[][] cols){
            leftMargin=lMargin;
            rightMargin=rMargin;
            textLines=tLines;
            solidLines=sLines;
            columns=cols;
        }

    }


    public Bitmap getLinesImg() {
        return Bitmap.createBitmap(makeLinesImage(OCRImage),
                imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
    }

    public Bitmap getOCRImg(){
        return Bitmap.createBitmap(OCRImage, imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
    }

    public LineSpecifier getLines() {
        return new LineSpecifier(leftMargin,rightMargin,textLines,solidLines,columns);
    }

    public ImgProcess(Bitmap image) {
        imageWidth  =image.getWidth();
        imageHeight =image.getHeight();
        imageToProcess=new int[imageWidth*imageHeight];
        image.getPixels(imageToProcess, 0, imageWidth, 0, 0, imageWidth, imageHeight);

        leftMargin=imageWidth/20;
        rightMargin=(imageWidth*9)/10;
        topMargin=0;
        bottomMargin=(9*imageHeight)/10;

        Log.d("ImgProcess", "Processing image; size:" + imageWidth + "x" + imageHeight);
        lightImage=new int[imageWidth*imageHeight];
        blobImage=new int[imageWidth*imageHeight];
        OCRImage=new int[imageWidth*imageHeight];

        filterImageLight();
        filterImageBlob();
        filterImageOCR();

        findLines();
        findColumns();
    }

    //functions for producing processed images
    private void filterImageLight(){
        int filtlh=imageWidth/40,filtlw=imageWidth/40;
        avgfilter(imageToProcess, lightImage, imageHeight, imageWidth, filtlh, filtlw);
    }

    private void filterImageBlob(){
        int filtlh2=imageWidth/200,filtlw2=imageWidth/200;
        if (lightImage==null)
            filterImageLight();

        avgfilter(imageToProcess, blobImage, imageHeight, imageWidth, filtlh2, filtlw2);
        threshold(blobImage, lightImage, blobImage, imageWidth * imageHeight, 19, 20);
    }

    private void filterImageOCR(){
        int filthh=imageWidth/1000,filthw=imageWidth/1000;
        if (lightImage==null)
            filterImageLight();

        avgfilter(imageToProcess, OCRImage, imageHeight, imageWidth, filthh, filthw);
        threshold(OCRImage,lightImage,OCRImage,imageWidth*imageHeight,18,20);
    }

    private int[] makeLinesImage(int[] input){
        int[] linesImage= Arrays.copyOf(input, input.length);
        int i;

        for(Integer line : solidLines){
            for(i=0;i<imageWidth;i++)
                linesImage[line*imageWidth+i]=0xFFFF0000;
        }
        for(Integer line : textLines){
            for(i=0;i<imageWidth;i++)
                linesImage[line*imageWidth+i]=0xFF0000FF;
        }

        for (i=0;i<textLines.length/2;i++) {
            for (int line : columns[i]) {
                for (int k = textLines[i * 2]; k < textLines[i * 2 + 1]; k++)
                    linesImage[k * imageWidth + line] = 0xFF00FF00;
            }
        }

        return linesImage;
    }

    //Word and line extraction functions

    private void findLines(){

        //horizontal detection parameters
        int thres=imageWidth/2;		//black line threshold
        int Tthres=imageWidth/40;		//Text line threshold
        int minLineGap=imageHeight/20;	//minimum gap between solid lines
        int minTextSize=imageHeight/50;

        int OCRSpace = (imageWidth/50)+1;

        //horizontal detection variables
        int LastSolidLine=0;	//last found solid line
        int top=0;		//current text row top
        int ntop=0;		//next text row top
        int PixelCount;	//number of black pixels
        int PreviousCount=0;	//number of black pixels in last row

        Vector<Integer> HorizontalLines=new Vector<>(); 	//stored solid lines
        Vector<Integer> TextLines=new Vector<>();		//stored text lines

        HorizontalLines.add(0);

        int i,j;
        for (j=topMargin;j<bottomMargin;j++) {
            PixelCount=0;

            for (i=leftMargin;i<rightMargin;i++) {
                if (blobImage[j*imageWidth+i]==black){
                    PixelCount++;
                }
            }
            //text rows
            if ((PixelCount>Tthres&&PreviousCount<=Tthres)
                    ||j==bottomMargin-1) {
                if (top<HorizontalLines.lastElement()) {
                    top=ntop;
                } else if (minTextSize<j-top) {
                    TextLines.add(top);

                    if (j==bottomMargin-1&&j-ntop>OCRSpace) {
                        TextLines.add(ntop+OCRSpace);
                    } else {
                        TextLines.add(j);
                    }
                    top=ntop;
                }

            } else if (PixelCount<=Tthres&&PreviousCount>Tthres) {
                ntop=j;
            }

            PreviousCount=PixelCount;
            //solid lines
            if(PixelCount>thres&&((j-LastSolidLine)>minLineGap)) {

                HorizontalLines.add(j);
                HorizontalLines.add(j);
                LastSolidLine=j;
            } else if (PixelCount>thres) {
                HorizontalLines.set(HorizontalLines.size()-1,j);
                LastSolidLine=j;
            }
        }

        //save found lines to object
        textLines=new int[TextLines.size()];
        solidLines=new int[HorizontalLines.size()];

        for (i=0;i<textLines.length;i++)
            textLines[i]=TextLines.get(i);

        for (i=0;i<solidLines.length;i++)
            solidLines[i]=HorizontalLines.get(i);

    }

    void findColumns() {

        int[] cumulative=new int[imageWidth*imageHeight];
        Vector<Vector<Integer>> foundColumns=new Vector<Vector<Integer>>();


        //vertical detection parameters
        int spaceSize;		//size of spaces between words
        int OCRSpace = (imageWidth/50)+1;

        //vertical detection variables
        int nTextLines=textLines.length/2;//number of text lines to process
        int vthres;		                        //threshold for vertical detection
        int RowHeight;		                    //height of current text row
        int prev=0;		                        //previous word end
        int firstUp;		                    //first white/black transition, ignore
                                                //because will only be white space
        int numWhiteCol;	                    //number of white columns in a row

        int PixelCount;

        //loop variables
        int i,j;


        //build cumulative array
        for (j=topMargin;j<bottomMargin;j++) {
            for (i = leftMargin; i < rightMargin; i++) {
                if (blobImage[j * imageWidth + i] == black) {
                    cumulative[j * imageWidth + i] = (j > 0) ? 1 + cumulative[(j - 1) * imageWidth + i] : 1;
                } else {
                    cumulative[j * imageWidth + i] = (j > 0) ? cumulative[(j - 1) * imageWidth + i] : 0;
                }
            }
        }

        for (i=0;i<nTextLines;i++) {

            //skip everything before second black line, start of numbers
            if (solidLines.length > 4 && textLines[i * 2] <= solidLines[4]) {
                foundColumns.add(new Vector<Integer>());
                continue; // Lines before intervals
            } else if (i > 0 && solidLines.length > 4 && textLines[i * 2 - 1] <= solidLines[4] &&
                    textLines[i * 2] >= solidLines[4]) {
                ;//any actions for first text line after second solid line
            }

            RowHeight = textLines[i * 2 + 1] - textLines[i * 2];

            vthres = 1 + RowHeight / 20;
            spaceSize = imageWidth / 10;
            boolean cont;
            int upperBound = spaceSize;
            int lowerBound = 0;
            int noOfRows;
            Vector<Integer> columnBreaks;

            do {
                Log.d("Col Space", "Row" + Integer.toString(i) + ": " + Integer.toString(spaceSize));
                numWhiteCol = 0;
                firstUp = 0;
                columnBreaks=new Vector<Integer>();

                for (j = leftMargin; j < rightMargin; j++) {
                    PixelCount = cumulative[textLines[i * 2 + 1] * imageWidth + j] -
                            cumulative[textLines[i * 2] * imageWidth + j];


                    if ((numWhiteCol > spaceSize && PixelCount > vthres) ||
                            j == (rightMargin - 1)) {

                        if (firstUp == 0) {
                            firstUp = 1;
                        } else {
                            columnBreaks.add(prev);
                            columnBreaks.add(j - numWhiteCol + OCRSpace);

                            Log.d("ImgProcess",
                                    "Found Rectangle: x:" + String.valueOf(prev + 1) +
                                            " y:"+String.valueOf(textLines[i * 2]) +
                                            " width:"+ String.valueOf(j - numWhiteCol + OCRSpace - prev - 1) +
                                            " height:" + String.valueOf(textLines[i * 2 + 1] - textLines[i * 2]));
                        }
                        prev = j - OCRSpace;
                    }

                    if (PixelCount <= vthres)
                        numWhiteCol++;
                    else
                        numWhiteCol = 0;

                }

                noOfRows = columnBreaks.size()/2;
                //space size too big
                if (noOfRows < 4) {
                    if(spaceSize < upperBound)
                        upperBound = spaceSize;
                    //space size too small
                } else if (noOfRows > 4) {
                    if(spaceSize > lowerBound)
                        lowerBound = spaceSize;
                }

                spaceSize = (upperBound + lowerBound) / 2;
                cont = true;

                if(upperBound - lowerBound < 2)
                    cont = false;

                if(noOfRows == 4)
                    cont = false;

            } while (cont);

            foundColumns.add(columnBreaks);

        }

        columns=new int[foundColumns.size()][];
        for (i=0;i<columns.length;i++){
            columns[i]=new int[foundColumns.get(i).size()];
            for(j=0;j<columns[i].length;j++){
                columns[i][j]=foundColumns.get(i).get(j);
            }
        }

        return;
    }

    /**
     *  Compares every value in imageIN to every value in thresArr, writing the white value to
     *  imageOUT if the imageIN value is greater than the value in thresArr or the black value
     *  otherwise
     *
     * @param imageIN  Input image array
     * @param thresArr Threshold level array
     * @param imageOUT Array to write result to
     * @param length The length of the arrays
     * @param multiplier Integer multiplier to scale imageIN value
     * @param divider Integer divider to scale imageIN value
     */
    private static void threshold(int[] imageIN, int[]thresArr, int[] imageOUT,
                                  int length, int multiplier , int divider) {
        int a,b;
        int i;

        for (i=0;i<length;i++) {
            a=pixel_avg(imageIN[i]);
            b=pixel_avg(thresArr[i]);

            if (a>(((b*multiplier)+(divider/2))/divider))
                imageOUT[i]=white;
            else
                imageOUT[i]=black;
        }
    }

    /**
     * Filters an image, to make each pixel the average of all pixels in a rectangle centred on
     * itself
     * @param imageIN The image to carry out filtering on
     * @param imageOUT The array to write the filtered image to, can be the same as imageIN
     * @param height The height of the image
     * @param width The width of the image
     * @param filth The height of the filtering rectangle
     * @param filtw The width of the filtering rectangle
     */
    private static void avgfilter(int[] imageIN, int[] imageOUT, int height,
                                  int width, int filth, int filtw){
        int i,j,intensity,average;
        if (filth==0 || filtw==0) {
            for (i=0;i<width*height;i++)
                imageOUT[i]=imageIN[i];
            return;
        }

        int[] cumulative=new int[width*height];

        cumulative[0]=imageIN[0];
        for (j=1;j<width;j++) {
            intensity=pixel_avg(imageIN[j]);
            cumulative[j]=cumulative[j-1]+intensity;
        }

        for (i=1;i<height;i++) {
            cumulative[i*width]=imageIN[i*width];

            for (j=1;j<width;j++) {
                intensity=pixel_avg(imageIN[i*width+j]);
                cumulative[i*width+j]=cumulative[i*width+j-1]+
                        intensity;
                cumulative[i*width+j-1]=cumulative[(i-1)*width+j-1]
                        +cumulative[i*width+j-1];
            }
        }

        cumulative[height*width-1]=cumulative[(height-1)*width-1]
                +cumulative[height*width-1];

        //filter stuff
        for (i=filth;i<height-filth;i++) {
            for (j=filtw;j<width-filtw;j++) {
                average=
                        (cumulative[(i+filth)*width+j+filtw]-
                                cumulative[(i+filth)*width+j-filtw])-
                                (cumulative[(i-filth)*width+j+filtw]-
                                        cumulative[(i-filth)*width+j-filtw]);

                average/=(4*filth*filtw);

                imageOUT[i*width+j]=0xFF000000+(average<<16)
                        +(average<<8)+average;
            }
        }
    }

    /**
     * Returns the average value of the three colour components from a integer pixel
     * @param value The pixels value
     * @return The average of the components
     */
    private static int pixel_avg(int value) {
        return (((value >> 16) & 0x000000FF)+
                ((value >>8 ) & 0x000000FF)+
                (value & 0x000000FF))/3;
    }

    private static void saveImage(Bitmap bmp, String name, String Cachepath){
        FileOutputStream cacheFile=null;

        try {
            cacheFile = new FileOutputStream(Cachepath+"/" + name + ".png");
            bmp.compress(Bitmap.CompressFormat.PNG,100,cacheFile);
        } catch (IOException e) {

        } finally {
            try {
                cacheFile.close();
            } catch (IOException e) {

            }
        }
    }

}

