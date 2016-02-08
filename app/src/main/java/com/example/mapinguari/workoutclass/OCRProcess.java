package com.example.mapinguari.workoutclass;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

public class OCRProcess {
    private String workoutType=null;
    Vector<Vector<String>> strings;

    public Vector<Vector<String>> getStrings(){
        return strings;
    }

    public String getWorkoutType () {
        return workoutType;
    }

    public OCRProcess(String languagePath, ImgProcess processedImage){
        Log.d("OCRProcess",languagePath);
        workoutType="";
        strings=recognise(processedImage.getOCRImg(),processedImage.getLines(),languagePath);
    }


    private Vector<Vector<String>> recognise(Bitmap OCRImage, ImgProcess.LineSpecifier lines, String cachePath) {

        Vector<Vector<String>> ret=new Vector<Vector<String>>();
        //tesseract variables
        String outText;
        boolean typeline=false;

        int width=OCRImage.getWidth(),height=OCRImage.getHeight();
        int[] image=new int[width*height];
        OCRImage.getPixels(image, 0, width, 0, 0, width, height);

        int leftMargin=lines.leftMargin;
        int rightMargin=lines.rightMargin;
        int[] solidLines=lines.solidLines;
        int[] textLines=lines.textLines;
        int[][] columns=lines.columns;

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
                Log.d("OCRProcess", "Type Line: " + workoutType);
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
                Log.d("OCRProcess", "Found:" + String.valueOf(i) + "-" +
                        String.valueOf(ret.lastElement().size()) + " " + outText);
            }
        }

        api.end();

        return ret;
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
                Log.d("OCRProcess","wrote "+cachefile.getAbsolutePath());
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
            Log.e("OCRProcess", e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
