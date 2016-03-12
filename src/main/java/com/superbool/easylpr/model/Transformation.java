package com.superbool.easylpr.model;

import com.superbool.easylpr.textdetection.LineSegment;
import com.superbool.easylpr.util.Utility;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kofee on 2016/3/11.
 */
public class Transformation {

    private Mat bigImage;
    private Mat smallImage;
    private Rect regionInBigImage;


    public Transformation(Mat bigImage, Mat smallImage, Rect regionInBigImage) {
        this.bigImage = bigImage;
        this.smallImage = smallImage;
        this.regionInBigImage = regionInBigImage;
    }


    // Re-maps the coordinates from the smallImage to the coordinate space of the bigImage.
    public List<Point> transformSmallPointsToBigImage(List<Point> points) {
        List<Point> bigPoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            double bigX = (points.get(i).x * (regionInBigImage.width / smallImage.cols()));
            double bigY = (points.get(i).y * (regionInBigImage.height / smallImage.rows()));

            bigX = bigX + regionInBigImage.x;
            bigY = bigY + regionInBigImage.y;

            bigPoints.add(new Point(bigX, bigY));
        }

        return bigPoints;
    }


    public Mat getTransformationMatrix(Mat cornersMat, Mat outputCornersMat) {

        // Get transformation matrix
        Mat transmtx = Imgproc.getPerspectiveTransform(cornersMat, outputCornersMat);

        return transmtx;
    }


    public Mat crop(Size outputImageSize, Mat transformationMatrix) {


        Mat deskewed = new Mat(outputImageSize, this.bigImage.type());

        // Apply perspective transformation to the image
        Imgproc.warpPerspective(this.bigImage, deskewed, transformationMatrix, deskewed.size(), Imgproc.INTER_CUBIC);


        return deskewed;
    }


    public Mat remapSmallPointstoCrop(Mat smallMat, Mat transformationMatrix) {
        Mat remappedMat = new Mat();

        Core.perspectiveTransform(smallMat, remappedMat, transformationMatrix);

        return remappedMat;
    }

    public Size getCropSize(List<Point> areaCorners, Size targetSize) {
        // Figure out the approximate width/height of the license plate region, so we can maintain the aspect ratio.
        LineSegment leftEdge = new LineSegment(areaCorners.get(3).x, areaCorners.get(3).y, areaCorners.get(0).x, areaCorners.get(0).y);
        LineSegment rightEdge = new LineSegment(areaCorners.get(2).x, areaCorners.get(2).y, areaCorners.get(1).x, areaCorners.get(1).y);
        LineSegment topEdge = new LineSegment(areaCorners.get(0).x, areaCorners.get(0).y, areaCorners.get(1).x, areaCorners.get(1).y);
        LineSegment bottomEdge = new LineSegment(areaCorners.get(3).x, areaCorners.get(3).y, areaCorners.get(2).x, areaCorners.get(2).y);

        double w = Utility.distanceBetweenPoints(leftEdge.midpoint(), rightEdge.midpoint());
        double h = Utility.distanceBetweenPoints(bottomEdge.midpoint(), topEdge.midpoint());

        if (w <= 0 || h <= 0)
            return new Size(0, 0);

        double aspect = w / h;
        int width = (int) targetSize.width;
        int height = (int) (width / aspect);
        if (height > targetSize.height) {
            height = (int) targetSize.height;
            width = (int) (height * aspect);
        }

        return new Size(width, height);
    }
}
