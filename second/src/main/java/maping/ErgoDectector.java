package maping;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.*;
import org.openimaj.image.analysis.algorithm.HoughLines;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;

import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;

/**
 * Created by mapinguari on 9/14/15.
 */
public class ErgoDectector {
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

        FImage prac = target.flatten();
        CannyEdgeDetector cannyEdgeDetector = new CannyEdgeDetector((float) 1);
        cannyEdgeDetector.processImage(prac);
        HoughLines houghLines = new HoughLines((float) 1);
        houghLines.analyseImage(prac);
        for(Line2d line : houghLines.getBestLines(20)){
            prac.drawLine(line, 3, RGBColour.WHITE[0]);

        }

        DisplayUtilities.display(prac, "Canny");
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

    //Top function. This is the entry point
    //Currently just using DoGSIFTEngine
    public Image getScreenImage( ErgoModel pmX, ErgoFeature ergoFeature, MBFImage fullimage){
        //Find Keypoints in the feature we are going to search for and the image we are searching for it in
        //DisplayUtilities.display(fullimage);
        LocalFeatureList<Keypoint> featurePoints = getKeyPoints(getFeatureImage(pmX, ergoFeature));
        LocalFeatureList<Keypoint> targetKeypoints = getKeyPoints(fullimage);


        //Use these keypoints to attempt to find an affine transform from the full image to the feature image
        HomographyModel affineTransformModel = findTransform(featurePoints, targetKeypoints);
        //Calculate where the main screen should be if the image where transformed to be parallel to the camera
        Rectangle screenRectangle = mainScreenRectangle(pmX, ergoFeature, getFeatureRectangle(pmX, ergoFeature));

        //fullimage.drawShape(
        //        getFeatureRectangle(pmX,ergoFeature).transform(affineTransformModel.getTransform().inverse()), 3, RGBColour.BLUE);
        //fullimage.drawShape(screenRectangle.transform(affineTransformModel.getTransform().inverse()),3, RGBColour.RED);
        DisplayUtilities.display(fullimage);

        //Transform the image back to flush to the screen
        Image transformedImage = fullimage.transform(affineTransformModel.getTransform());
        DisplayUtilities.display(transformedImage);
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

    public LocalFeatureList<Keypoint> getKeyPoints( MBFImage featureImage){
        DoGSIFTEngine engine = new DoGSIFTEngine();
        //I should really be saying a keypoint list then just pulling this back into memory but I don't know how to do this yet.
        LocalFeatureList<Keypoint> targetKeypoints;
        targetKeypoints = engine.findFeatures(featureImage.flatten());
        return targetKeypoints;
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
}
