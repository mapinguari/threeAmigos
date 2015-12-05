package com.example.mapinguari.workoutclass.openImajInterface;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.widget.ImageView;


import com.example.mapinguari.workoutclass.R;

import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.feature.local.keypoints.*;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.transforms.HomographyModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import maping.ErgoDetector;

/**
 * Created by mapinguari on 11/1/15.
 */
public class SecondAppInterface {

        private Context context;
        private String keypointsFileName = "pm3concept2logo.key";

        public SecondAppInterface(Context context){
            this.context = context;
        }

        public static Bitmap createBitmap(Image<?, ?> img, Bitmap bmap){
            if (bmap == null || bmap.getWidth() != img.getWidth() || bmap.getHeight() != img.getHeight() || bmap.getConfig() != Bitmap.Config.ARGB_8888){
                bmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
            }
            bmap.setPixels(img.toPackedARGBPixels(), 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
            return bmap;
        }

        public static MBFImage createMBFImage(Bitmap image, boolean alpha){
            final int[] data = new int[image.getHeight()*image.getWidth()];
            image.getPixels(data, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            return new MBFImage(data, image.getWidth(), image.getHeight(), alpha);
        }

        public static void viewImage(Image<?, ?> img, ImageView view){
            Bitmap image = createBitmap(img, null);
            view.setImageBitmap(image);
        }

        public static Bitmap findErgoScreen(Bitmap bitmap){
            MBFImage mbfImageIn = SecondAppInterface.createMBFImage(bitmap, true);
            ErgoDetector ergoDetector = new ErgoDetector();
            Image mbfImageOut = (Image) ergoDetector.concept2LogoGet(mbfImageIn);
            return(createBitmap(mbfImageOut,null));
        }

        public String findErgoScreenAndroid(String filepath) {
            Bitmap img0 = BitmapFactory.decodeFile(filepath);
            MBFImage mbfImage = createMBFImage(img0, false);
            ErgoDetector ergoDetector = new ErgoDetector();
            LocalFeatureList<Keypoint> searchKeypoints = ergoDetector.getKeyPoints(mbfImage);
            FileLocalFeatureList<Keypoint> ergoScreenKeypoints = null;
            try{
                ergoScreenKeypoints = FileLocalFeatureList.read(getKeypointDataFile(),Keypoint.class);
            }catch(Exception e){
                e.printStackTrace();
            }
            HomographyModel homographyModel = ergoDetector.findTransform(ergoScreenKeypoints, searchKeypoints);
            Image transformedImage = mbfImage.transform(homographyModel.getTransform());
            Bitmap img1 = createBitmap(transformedImage, null);
            String fileOutName = makeImageOutName(filepath);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(fileOutName);
                img1.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return fileOutName;
        }

        public String makeImageOutName(String startingFileName){
            File x = new File(startingFileName);
            String outFile = "ES" + x.getName();
            return x.getAbsolutePath() + "/" + outFile;
        }

        public File getKeypointDataFile(){
            File keypointsFile = context.getFileStreamPath(keypointsFileName);
            if(!keypointsFile.exists()){
                keypointsFile = buildKeypointDataFile();
            }
            return keypointsFile;
        }

        public File buildKeypointDataFile() {
            OutputStream outputStream = null;
            InputStream inputStream = null;
            Resources resources = context.getResources();
            File keypointsFile = null;
            try {

                inputStream = resources.openRawResource(R.raw.pm3concept2logo);
                // write the inputStream to a FileOutputStream

                keypointsFile = new File(context.getFilesDir(),keypointsFileName);

                outputStream =
                        new FileOutputStream(keypointsFile);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                System.out.println("Done!");

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        // outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            return keypointsFile;
        }
}
