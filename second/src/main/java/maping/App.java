package maping;


import org.openimaj.image.*;
import org.openimaj.image.Image;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        System.out.println("Please Input File Path");
        String filePath = "/home/mapinguari/Downloads/ergos/IMG_20150903_120044.jpg";
            ErgoDectector thing = new ErgoDectector();
            Image result = thing.findErgoScreen(filePath);
            DisplayUtilities.display(result);
        }


}
