package com.example.mapinguari.workoutclass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.googlecode.tesseract.android.*;

public class ImgProcess {
    private static final int black = 0xFF000000;
    private static final int white = 0xFFFFFFFF;

    public Bitmap linesImg = null;
    public Bitmap ocrImg = null;
    public String workoutType=null;

    private int imageHeight,imageWidth;

    //actual edges to use
    int leftMargin,rightMargin,topMargin,bottomMargin;


    private final int[] imageToProcess;
    private int[] lightImage;
    private int[] blobImage;
    private int[] OCRImage;
    private int[] linesImage;
    private String cachePath;

    private int[] textLines;
    private int[] solidLines;

    private int[][] columns;

    public Bitmap getOCRImg() {
        ocrImg = Bitmap.createBitmap(OCRImage,imageWidth,imageHeight, Bitmap.Config.ARGB_8888);
        return ocrImg;
    }

    public ImgProcess(Bitmap image, String languagePath) {
        imageWidth  =image.getWidth();
        imageHeight =image.getHeight();
        imageToProcess=new int[imageWidth*imageHeight];

        image.getPixels(imageToProcess, 0, imageWidth, 0, 0, imageWidth, imageHeight);
        cachePath=languagePath;

        lightImage=null;
        blobImage=null;
        OCRImage=null;

        leftMargin=imageWidth/20;
        rightMargin=(imageWidth*9)/10;
        topMargin=0;
        bottomMargin=(9*imageHeight)/10;

        workoutType="";
    }


    public Vector<Vector<String>> ProcessImage() {
        Log.d("ImgProcess", "Processing image; size:" + imageWidth + "x" + imageHeight);
        //do stuff here
        return doEverything();
    }

    private Vector<Vector<String>>  doEverything () {
        //image processing
        if (OCRImage==null)
            filterImageOCR();
        if (blobImage==null)
            filterImageBlob();

        findLines();
        findColumns();

        makeLinesImage();
        linesImg=Bitmap.createBitmap(linesImage, imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

        //text recognition
        Vector<Vector<String>> ret=recognise(OCRImage,imageWidth,imageHeight,leftMargin,rightMargin,
                textLines,solidLines,columns,cachePath);

        /*
        saveImage(lightImage, imageWidth, imageHeight, "lights",cachePath);
        saveImage(blobImage, imageWidth, imageHeight, "blobs",cachePath);
        saveImage(linesImage, imageWidth, imageHeight, "textlines",cachePath);
        */

        return ret;

    }

    // filtered image creation functions
    private void filterImageLight(){
        int filtlh=imageWidth/40,filtlw=imageWidth/40;

        lightImage=new int[imageWidth*imageHeight];
        avgfilter(imageToProcess, lightImage, imageHeight, imageWidth, filtlh, filtlw);
    }

    private void filterImageBlob(){
        int filtlh2=imageWidth/200,filtlw2=imageWidth/200;
        if (lightImage==null)
            filterImageLight();

        blobImage=new int[imageWidth*imageHeight];
        avgfilter(imageToProcess, blobImage, imageHeight, imageWidth, filtlh2, filtlw2);
        threshold(blobImage, lightImage, blobImage, imageWidth * imageHeight, 19, 20);
    }

    private void filterImageOCR(){
        int filthh=imageWidth/1000,filthw=imageWidth/1000;
        if (lightImage==null)
            filterImageLight();

        OCRImage=new int[imageWidth*imageHeight];
        avgfilter(imageToProcess, OCRImage, imageHeight, imageWidth, filthh, filthw);
        threshold(OCRImage,lightImage,OCRImage,imageWidth*imageHeight,18,20);
    }

    //Word and line extraction functions

    private void findLines(){

        //horizontal detection parameters
        int thres=imageWidth/2;		//black line threshold
        int Tthres=imageWidth/40;		//Text line threshold
        int minLineGap=imageHeight/20;	//minimum gap between solid lines
        int minTextSize=imageHeight/50;

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

                    if (j==bottomMargin-1&&j-ntop>30) {
                        TextLines.add(ntop+1);
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

        return;
    }

    void findColumns() {

        int[] cumulative=new int[imageWidth*imageHeight];
        Vector<Vector<Integer>> foundColumns=new Vector<Vector<Integer>>();


        //vertical detection parameters
        int spaceSize;		//size of spaces between words

        //vertical detection variables
        int nTextLines=textLines.length/2;		//number of text lines to process
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
                            columnBreaks.add(j);

                            Log.d("ImgProcess",
                                    "Found Rectangle: x:" + String.valueOf(prev + 1) +
                                            " y:"+String.valueOf(textLines[i * 2]) +
                                            " width:"+ String.valueOf(j - prev - 1) +
                                            " height:" + String.valueOf(textLines[i * 2 + 1] - textLines[i * 2]));
                        }
                        prev = j - numWhiteCol;
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

    private void makeLinesImage(){
        int[] processedImage= Arrays.copyOf(OCRImage, OCRImage.length);
        int i;

        for(Integer line : solidLines){
            for(i=0;i<imageWidth;i++)
                processedImage[line*imageWidth+i]=0xFFFF0000;
        }
        for(Integer line : textLines){
            for(i=0;i<imageWidth;i++)
                processedImage[line*imageWidth+i]=0xFF0000FF;
        }

        for (i=0;i<textLines.length/2;i++) {
            for (int line : columns[i]) {
                for (int k = textLines[i * 2]; k < textLines[i * 2 + 1]; k++)
                    processedImage[k * imageWidth + line] = 0xFF00FF00;
            }
        }

        linesImage=processedImage;
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

    private Vector<Vector<String>> recognise(int[] image, int width, int height, int leftMargin, int rightMargin,
                                             int[] textLines, int[] solidLines, int[][] columns, String cachePath) {

        Vector<Vector<String>> ret=new Vector<Vector<String>>();
        //tesseract variables
        String outText;
        boolean typeline=false;

        byte [] img;
        {
            ByteBuffer buff=ByteBuffer.allocate(4*image.length);
            for (int tint:image)buff.putInt(tint);
            img=buff.array();
        }

        //set up tesseract
        TessBaseAPI api = new TessBaseAPI();
        api.init(cachePath, "lan");
        api.setImage(img, width, height, 4, 4 * width);

        //split horizontally into words
        //OCR strings in found rectangles
        for(int i=0;i<columns.length;i++) {
            //get type line
            if (solidLines.length > 4 && textLines[i * 2] > solidLines[2] &&
                    textLines[i * 2] <= solidLines[3] && !typeline) {

                api.setRectangle(leftMargin, textLines[i * 2],
                        rightMargin - leftMargin, textLines[i * 2 + 1] - textLines[i * 2]);

                workoutType = api.getUTF8Text();
                Log.d("ImgProcess", "Type Line: " + workoutType);
                typeline=true;
                continue;
            } else if (columns[i].length==0){
                continue; //skip empty lines;
            }

            ret.add(new Vector<String>());
            for (int j = 0; j < columns[i].length / 2; j++) {
                api.setRectangle(
                        columns[i][2 * j] + 1,
                        textLines[i * 2],
                        columns[i][2 * j + 1] - columns[i][2 * j] - 1,
                        textLines[i * 2 + 1] - textLines[i * 2]);

                outText = api.getUTF8Text();
                ret.lastElement().add(outText);
                Log.d("ImgProcess", "Found:" + String.valueOf(i) + "-" +
                        String.valueOf(ret.lastElement().size()) + " " + outText);
            }
        }

        api.end();

        return ret;
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



    /**
     * Copies a tesseract traineddata file from the packaged assets to a regular file in the cache
     * so tesseract can find it
     * @param language The name of the language to be loaded
     * @param context Application context to access assets and cache directory
     * @return Whether the traineddata file is now in the cache
     */
    public static boolean loadLanguage(String language, Context context) {
        String directory="tessdata";
        String lpath="tessdata/"+language+".traineddata";
        try {
            File cacheDir=new File(context.getCacheDir(),directory);
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
                Log.d("ImgProcess","wrote "+cacheDir.getAbsolutePath());
            }
            File cachefile=new File(context.getCacheDir(),lpath);
            if (!cachefile.exists()) {
                Log.d("ImgProcess","wrote "+cachefile.getAbsolutePath());
                InputStream i = context.getAssets().open(lpath);
                byte[] buffer = new byte[i.available()];
                i.read(buffer);
                i.close();

                cachefile.createNewFile();
                FileOutputStream o = new FileOutputStream(cachefile);
                o.write(buffer);
                o.close();
            }
        } catch (IOException e) {
            Log.e("ImgProcess", e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void saveImage(int[] image, int width, int height, String name, String Cachepath){
        FileOutputStream cacheFile=null;
        Bitmap bmp = Bitmap.createBitmap(image, width, height, Bitmap.Config.ARGB_8888);

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

