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
import android.util.Log;

import com.googlecode.tesseract.android.*;

public class ImgProcess {
    private static final int black = 0xFF000000;
    private static final int white = 0xFFFFFFFF;

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

                imageOUT[i*width+j]=(average<<16)
                        +(average<<8)+average;
            }
        }
    }

    private static Vector<Vector<String>> recognise(int[] sliceImage, int[] imageIN, int height,
                                                    int width, String Cachepath) {

        int[] cumulative=new int[width*height];
        int[] processedImage= Arrays.copyOf(imageIN, imageIN.length);


        //actual edges to use
        int leftMargin=width/40;
        int rightMargin=(width*9)/10;
        int topMargin=0;
        int bottomMargin=(9*height)/10;

        //horizontal detection parameters
        int thres=width/2;		//black line threshold
        int Tthres=width/20;		//Text line threshold
        int minLineGap=height/20;	//minimum gap between solid lines
        int minTextSize=height/50;

        //vertical detection parameters
        int spaceSize=width/30;		//size of spaces between words


        //horizontal detection variables
        int LastSolidLine=0;	//last found solid line
        int top=0;		//current text row top
        int ntop=0;		//next text row top
        int PixelCount;	//number of black pixels
        int PreviousCount=0;	//number of black pixels in last row

        Vector<Integer> HorizontalLines=new Vector<>(); 	//stored solid lines
        Vector<Integer> TextLines=new Vector<>();		//stored text lines

        HorizontalLines.add(0);

        //vertical detection variables
        int nTextLines;		//number of text lines to process
        int vthres;		//threshold for vertical detection
        int RowHeight;		//height of current text row
        int prev=0;		//previous word end
        int firstUp;		//first white/black transition, ignore
        //because will only be white space
        int numWhiteCol;	//number of white columns in a row

        //loop variables
        int i,j;

        Vector<Vector<String>> ret=new Vector<>();


        //tesseract variables
        String outText;
        byte [] img;
        {
            ByteBuffer buff=ByteBuffer.allocate(4*imageIN.length);
            for (int tint:imageIN)buff.putInt(tint);
            img=buff.array();
        }

        //set up tesseract
        TessBaseAPI api = new TessBaseAPI();
        api.init(Cachepath, "lan");
        api.setImage(img, width, height,4,4*width);

        for (j=topMargin;j<bottomMargin;j++) {
            PixelCount=0;

            for (i=leftMargin;i<rightMargin;i++) {
                if (sliceImage[j*width+i]==black){
                    PixelCount++;
                    cumulative[j*width+i]= (j>0)?1 +cumulative[(j-1)*width+i]:1;
                } else {
                    cumulative[j*width+i]=(j>0)?cumulative[(j-1)*width+i]:0;
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

        for(Integer line : HorizontalLines){
            for(i=0;i<width;i++)
                processedImage[line*width+i]=0xFFFF0000;
        }
        for(Integer line : TextLines){
            for(i=0;i<width;i++)
                processedImage[line*width+i]=0xFF0000FF;
        }


        //split horizontally into words

        nTextLines=TextLines.size()/2;

        for (i=0;i<nTextLines;i++) {

            //skip everything before second black line, start of numbers
            if (HorizontalLines.size() > 4 &&
                    TextLines.get(i * 2) <= HorizontalLines.get(4)) {
                continue;
            } else if (i > 0 && HorizontalLines.size() > 4 && TextLines.get(i * 2 - 1) <= HorizontalLines.get(4) &&
                    TextLines.get(i * 2) >= HorizontalLines.get(4)) {
                api.init(Cachepath, "lan");
            }

            RowHeight = TextLines.get(i * 2 + 1) - TextLines.get(i * 2);

            vthres = 1 + RowHeight / 20;
            spaceSize = width / 10;
            boolean cont;
            Vector<String> foundThisRow,foundThisRowLast=null;
            Vector<Integer> columnBreaks=null;

            do {
                numWhiteCol = 0;
                firstUp = 0;
                columnBreaks=new Vector<Integer>();
                foundThisRow=new Vector<String>();
                cont = false;
                for (j = leftMargin; j < rightMargin; j++) {
                    PixelCount = cumulative[TextLines.get(i * 2 + 1) * width + j] -
                            cumulative[TextLines.get(i * 2) * width + j];


                    if ((numWhiteCol > spaceSize && PixelCount > vthres) ||
                            j == (rightMargin - 1)) {

                        if (firstUp == 0) {
                            firstUp = 1;
                        } else {
                            columnBreaks.add(prev);
                            columnBreaks.add(j);
                            api.setRectangle(prev + 1, TextLines.get(i * 2), j - prev - 1,
                                    TextLines.get(i * 2 + 1) - TextLines.get(i * 2));

                            outText = api.getUTF8Text();
                            foundThisRow.add(outText);
                            Log.d("ImgProcess", "Rectangle:" + String.valueOf(prev + 1) + "-" + String.valueOf(j - prev - 1) + "x" +
                                    String.valueOf(TextLines.get(i * 2)) + "-" + String.valueOf(TextLines.get(i * 2 + 1) - TextLines.get(i * 2)) +
                                    ", Found:" + String.valueOf(i) + "-" + String.valueOf(foundThisRow.size()) + " " + outText);
                        }
                        prev = j - numWhiteCol;
                    }

                    if (PixelCount <= vthres)
                        numWhiteCol++;
                    else
                        numWhiteCol = 0;

                }

                if (foundThisRow.size() < 4 && spaceSize >= 1) {
                    spaceSize = spaceSize / 2;
                    cont = true;
                } else if (foundThisRow.size() > 4 && foundThisRowLast!=null) {
                    foundThisRow=foundThisRowLast;
                }
                foundThisRowLast=foundThisRow;

            } while (cont);
            ret.add(foundThisRow);

            for(Integer line : columnBreaks){
                for(int k= TextLines.get(i * 2);k<TextLines.get(i * 2 + 1);k++)
                    processedImage[k*width+line]=0xFF00FF00;
            }

        }

        saveImage(processedImage,width,height,"textlines",Cachepath);
        api.end();

        return ret;
    }

    private static Vector<Vector<String> >  doEverything (int[] imageIN, int height,
                                                          int width, String Cachepath ) {

        //filter parameters
        int filthh=width/400,filthw=width/400;//high filter
        int filtlh=width/40,filtlw=width/40;  //low filter 1
        int filtlh2=width/40,filtlw2=width/40;  //low filter 2

        int[] LowArray=new int[width*height];		//Array for low pass filtered image
        int[] HighArray=new int[width*height];

        //recognised strings
        Vector<Vector<String>>  recognised;

        //check input parameters are OK?
        if ((width*height)==0 ||filtlh<filthh||filtlw<filthw||
                width<2*filtlw||height<2*filtlh) {
            return null;
        }

        //image processing
        avgfilter(imageIN,HighArray,height,width,filthh,filthw);
        avgfilter(imageIN,LowArray,height,width,filtlh,filtlw);
        threshold(HighArray,LowArray,HighArray,width*height,19,20);

        avgfilter(imageIN,LowArray,height,width,filtlh2,filtlw2);
        threshold(imageIN,LowArray,imageIN,width*height,18,20);

        //text recognition
        recognised=recognise(HighArray,imageIN,height,width,Cachepath);

        //probably need to do some formatting stuff here?

        return recognised;
    }


    public static Vector<Vector<String>> ProcessImage(Bitmap image, String Cachepath) {
        int width = image.getWidth(),height=image.getHeight();
        int[] array=new int[width*height];
        image.getPixels(array, 0, width, 0, 0, width, height);

        Log.d("ImgProcess", "Processing image; size:" + width + "x" + height);

        //do stuff here
        Vector<Vector<String>> values=doEverything(array,height,width,Cachepath);
        return values;
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

