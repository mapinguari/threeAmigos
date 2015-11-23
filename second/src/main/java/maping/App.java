package maping;


import com.sun.org.apache.xpath.internal.operations.Bool;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.*;
import org.openimaj.image.Image;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.WriteableBinary;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Scanner;

import javax.swing.JFrame;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        ErgoDetector thing = new ErgoDetector();
        //thing.saveImageKeyPoints();
        float succ = 0;
        File a = new File("/home/mapinguari/Downloads/ergos/");
        File[] fileList = a.listFiles();
        String currentPath;
        float tot = fileList.length;
        char test;
        for(File file: fileList){
            currentPath = file.getAbsolutePath();
            Image result = thing.findErgoScreen(currentPath);
            JFrame currentScreen = DisplayUtilities.display(result, file.getName());
            try {
                Scanner s= new Scanner(System.in);
                test = s.next().charAt(0);
                if(test == 'y'){
                    succ += 1;
                    System.out.print(succ);
                }
                currentScreen.dispatchEvent(new WindowEvent(currentScreen, WindowEvent.WINDOW_CLOSING));
            }catch(Exception e){}

            }
        System.out.println("SUCCESS % = " + Float.toString((succ / tot) * 100));
        }
}
