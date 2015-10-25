package maping;


import org.openimaj.image.*;
import org.openimaj.image.Image;

import java.io.File;

import javax.swing.JFrame;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        System.out.println("Please Input File Path");
        File a = new File("/home/mapinguari/Downloads/ergos/");
        File[] fileList = a.listFiles();
        ErgoDectector thing = new ErgoDectector();
        String currentPath;
        for(File file: fileList){
            currentPath = file.getAbsolutePath();
            Image result = thing.findErgoScreen(currentPath);
            DisplayUtilities.display(result, file.getName());
        }
        System.out.println("STOP");
        }


}
