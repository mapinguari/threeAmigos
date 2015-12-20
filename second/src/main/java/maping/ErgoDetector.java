package maping;

import org.apache.tools.ant.taskdefs.Local;
import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.*;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.algorithm.HoughLines;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGColourSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.asift.ASIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.pixel.IntValuePixel;

import org.openimaj.image.processing.resize.ResizeProcessor;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mapinguari on 9/14/15.
 */
public class ErgoDetector {

    private String currentKeyPointsFileName = "pm3concept2Logo.key";


    public Image findErgoScreen(String filePath){
        MBFImage target = null;
        try {
            target = ImageUtilities.readMBF(new File(filePath));
            double size = target.getWidth() * target.getHeight() * 3;
            System.out.println(size);
            while(size > 1000000){
                target = ResizeProcessor.halfSize(target);
                size = target.getWidth() * target.getHeight() * 3;

            }

        } catch (Exception e){
        }

        Image ergoScreen = this.getScreenImage(ErgoModel.PM3, ErgoFeature.C2Logo, target);

        return ergoScreen;
    }

    public HomographyModel findTransform(LocalFeatureList<Keypoint> featurePoints, LocalFeatureList<Keypoint> imagePoints){
        //RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(5.0, 1500,
          //      new RANSAC.PercentageInliersStoppingCondition(0.75));

        RobustHomographyEstimator md = new RobustHomographyEstimator(1.0, 10000, new RANSAC.PercentageInliersStoppingCondition(0.75) , HomographyRefinement.SINGLE_IMAGE_TRANSFER);

        LocalFeatureMatcher<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
                new FastBasicKeypointMatcher<Keypoint>(8), md);

        matcher.setModelFeatures(featurePoints);
        matcher.findMatches(imagePoints);
        return md.getModel();
    }

    public Rectangle mainScreenRectangle(ErgoModel pmX, ErgoFeature ergoFeature, Rectangle featureRect){
        Rectangle originalFeat = getMeasures(pmX,ergoFeature);
        Rectangle originalScreen = getMeasures(pmX, ErgoFeature.MainScreen);
        float tlx = featureRect.x + (featureRect.width / originalFeat.width) * Math.abs(originalScreen.x - originalFeat.x);
        float tly = featureRect.y + (featureRect.height / originalFeat.height) * Math.abs(originalScreen.y - originalFeat.y);
        float width = originalScreen.width * (featureRect.width / originalFeat.width);
        float height = originalScreen.height * (featureRect.width / originalFeat.width);
        Rectangle screenRect = new Rectangle(tlx,tly,width,height);
        return screenRect;
    }


    public Image getScreenImageComp(MBFImage fullImage){
        MBFImage screenI = null;
        try{
            screenI = ImageUtilities.readMBF((new File("/home/mapinguari/Downloads/pm3.jpg")));
        } catch(Exception e){}

        LocalFeatureList<Keypoint> featurePoints = getKeyPointsColour(screenI);
        LocalFeatureList<Keypoint> targetKeypoints = getKeyPointsColour(fullImage);

        List<Point2d> corners = new ArrayList<Point2d>();
        corners.add(0,new IntValuePixel(31,39));
        corners.add(1,new IntValuePixel(252,36));
        corners.add(2,new IntValuePixel(250,253));
        corners.add(3,new IntValuePixel(30,252));
        Polygon screen = new Polygon(corners);

        List<Point2d> oCorners = new ArrayList<Point2d>();
        oCorners.add(0,new IntValuePixel(10,12));
        oCorners.add(1,new IntValuePixel(292,9));
        oCorners.add(2,new IntValuePixel(290,293));
        oCorners.add(3,new IntValuePixel(13,293));
        Polygon outer = new Polygon(oCorners);

        ArrayList<Keypoint> carryPoints = new ArrayList<Keypoint>();

        System.out.println(featurePoints.size());

        for(Keypoint p : featurePoints){
            if(screen.isInside(p) || !(outer.isInside(p))){
            }
            else{
                carryPoints.add(p);
            }

        }

        LocalFeatureList<Keypoint> trueFeaturePoints = new MemoryLocalFeatureList<Keypoint>(carryPoints);
        for(Point2d p: trueFeaturePoints){
            screenI.drawPoint(p,RGBColour.BLUE,4);
        }


        DisplayUtilities.display(screenI, "ScreenI");

        for(Point2d p: targetKeypoints){
            fullImage.drawPoint(p, RGBColour.BLUE, 4);
        }

        DisplayUtilities.display(fullImage);


        System.out.println(trueFeaturePoints.size());

        HomographyModel homographyModel = findTransform(trueFeaturePoints,targetKeypoints);

        Image transformedImage = fullImage.transform(homographyModel.getTransform());
        DisplayUtilities.display(transformedImage,"Complete Screen");

        return null;
    }


    public Image concept2LogoGet(MBFImage fullimage){
        return getScreenImage(ErgoModel.PM3, ErgoFeature.C2Logo, fullimage);
    }

    //Top function. This is the entry point
    //Currently just using DoGSIFTEngine
    public Image getScreenImage( ErgoModel pmX, ErgoFeature ergoFeature, MBFImage fullimage){
        //Find Keypoints in the feature we are going to search for and the image we are searching for it in
        //DisplayUtilities.display(fullimage);
        LocalFeatureList<Keypoint> featurePoints = readImageKeyPoints();
                //getKeyPoints(getFeatureImage(pmX, ergoFeature));
        LocalFeatureList<Keypoint> targetKeypoints = getKeyPoints(fullimage);


        //Use these keypoints to attempt to find an affine transform from the full image to the feature image
        HomographyModel affineTransformModel = findTransform(featurePoints, targetKeypoints);
        //Calculate where the main screen should be if the image where transformed to be parallel to the camera
        Rectangle screenRectangle = mainScreenRectangle(pmX, ergoFeature, getFeatureRectangle(pmX, ergoFeature));

        //fullimage.drawShape(
        //        getFeatureRectangle(pmX,ergoFeature).transform(affineTransformModel.getTransform().inverse()), 3, RGBColour.BLUE);
        //fullimage.drawShape(screenRectangle.transform(affineTransformModel.getTransform().inverse()),3, RGBColour.RED);
        //DisplayUtilities.display(fullimage);

//        File imagePath = new File("/home/mapinguari/Downloads/pm3concept2Logo.jpg");
//        MBFImage featureImage = null;
//        try {
//            featureImage = ImageUtilities.readMBF(imagePath);
//        } catch (Exception e){
//
//        }
//
//        fullimage.drawShape(
//                featureImage.getBounds().transform(affineTransformModel.getTransform().inverse()), 3, RGBColour.BLUE);
//        DisplayUtilities.display(fullimage);
//

        //Transform the image back to flush to the screen
        Image transformedImage = fullimage.transform(affineTransformModel.getTransform());
        //DisplayUtilities.display(transformedImage);
        //extract from the image the area which should contain the screen
        Image resolvedScreen = transformedImage.extractROI(screenRectangle);
        return resolvedScreen;
    }

    public MBFImage getFeatureImage(ErgoModel pmX, ErgoFeature ergoFeature){
        File imagePath = null;
        MBFImage featureImage;
        if(pmX == ErgoModel.PM3 || pmX == ErgoModel.PM4){
            switch(ergoFeature){
                case C2Logo: imagePath = new File("/home/mapinguari/Downloads/pm3concept2Logo.jpg");
                    break;
                default :
                    return null;
            }
        }
        // PM5
        else {
            switch (ergoFeature) {
                case C2Logo:
                    break;
                case ModelLogo:
                    break;
                case LeftButton:
                    break;
                case MiddleButton:
                    break;
                case RightButton:
                    break;
                case SideButtons:
                    break;
                case MainScreen:
                    break;
            }
        }

        try {
            featureImage = ImageUtilities.readMBF(imagePath);
        } catch (IOException e) {
            featureImage = null;
        }
        return featureImage;

    }

    public LocalFeatureList<Keypoint> getKeyPointsColour( MBFImage featureImage){
        ASIFTEngine engine = new ASIFTEngine();
        //I should really be saying a keypoint list then just pulling this back into memory but I don't know how to do this yet.
        LocalFeatureList<Keypoint> targetKeypoints;
        targetKeypoints = engine.findKeypoints(featureImage.flatten());
        return targetKeypoints;
    }

    public LocalFeatureList<Keypoint> getKeyPoints( MBFImage featureImage){
        DoGSIFTEngine engine = new DoGSIFTEngine();
        engine.getOptions().setPeakThreshold((float) 0.3);
        //I should really be saying a keypoint list then just pulling this back into memory but I don't know how to do this yet.
        LocalFeatureList<Keypoint> targetKeypoints;
        targetKeypoints = engine.findFeatures(featureImage.flatten());
        System.out.println(engine.getOptions().getPeakThreshold());
        return targetKeypoints;
    }


    public Image takeAndDraw(MBFImage image){
        LocalFeatureList<Keypoint> a = getKeyPoints(image);
        image.drawPoints(a,RGBColour.RED,3);
        return image;
    }

    public Rectangle getFeatureRectangle(ErgoModel pmX, ErgoFeature ergoFeature){
        MBFImage image = getFeatureImage(pmX, ergoFeature);
        //DisplayUtilities.display(image);
        Rectangle out = new Rectangle(7,5,154,18);
        return out;
    }

    private Rectangle getMeasures(ErgoModel pmX, ErgoFeature ergoFeature){
        Rectangle outRect = null;
        if(pmX == ErgoModel.PM3 || pmX == ErgoModel.PM4){
            switch(ergoFeature){
                case C2Logo: outRect = new Rectangle(7,(float) 4.5,60,(float)6.5);
                    break;
                case ModelLogo:
                    break;
                case LeftButton:
                    break;
                case MiddleButton:
                    break;
                case RightButton:
                    break;
                case SideButtons:
                    break;
                case MainScreen: outRect = new Rectangle((float) 16.5,(float) 16.5,78,77);
                    break;
            }
        }
        // PM5
        else {
            switch (ergoFeature){
                case C2Logo:
                    break;
                case ModelLogo:
                    break;
                case LeftButton:
                    break;
                case MiddleButton:
                    break;
                case RightButton:
                    break;
                case SideButtons:
                    break;
                case MainScreen:
                    break;
            }
        }
        return outRect;
    }

    public enum ErgoModel {
        PM3,
        PM4,
        PM5
    }

    public enum ErgoFeature {
        C2Logo,
        ModelLogo,
        LeftButton,
        MiddleButton,
        RightButton,
        SideButtons,
        MainScreen
    }


    public LocalFeatureList<Keypoint> readImageKeyPoints(){
        ClassLoader classLoader = getClass().getClassLoader();
        File keyPointsFile = new File(classLoader.getResource("pm3concept2Logo.key").getFile());
        LocalFeatureList<Keypoint> keypoints = null;
        try{
            keypoints = FileLocalFeatureList.read(keyPointsFile, Keypoint.class);
        }catch(IOException e){
            e.printStackTrace();
        }
        return keypoints;
    }

    public void saveImageKeyPoints(){
        ErgoDetector ed = new ErgoDetector();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            MBFImage logoImg = ImageUtilities.readMBF(new File(classLoader.getResource("pm3concept2Logo.jpg").getFile()));

            LocalFeatureList<Keypoint> featureList = ed.getKeyPoints(logoImg);

            File keyPointsFile = new File(currentKeyPointsFileName);
            boolean b = keyPointsFile.createNewFile();
//            RandomAccessFile randomAccessFile = new RandomAccessFile(keyPointsFile,"rw");
//            featureList.writeBinary(randomAccessFile);
//            randomAccessFile.close();
            PrintWriter printWriter = new PrintWriter(keyPointsFile);
            featureList.writeASCII(printWriter);
            printWriter.close();


            System.out.println("KeyPoints Saved");

            FileLocalFeatureList<Keypoint> fileLocalFeatureList = FileLocalFeatureList.read(keyPointsFile, Keypoint.class);

            System.out.println("file length" + Integer.toString(fileLocalFeatureList.size()));

            boolean res = featureList.equals(((LocalFeatureList) fileLocalFeatureList).subList(1,10));

            System.out.println("And the result is!!!!! " + Boolean.toString(res));

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
