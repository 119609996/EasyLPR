package com.superbool.easylpr.edges;

import com.sun.javafx.geom.Vec2f;
import com.superbool.easylpr.textdetection.LineSegment;
import com.superbool.easylpr.textdetection.TextLine;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kofee on 2016/3/9.
 */
public class PlateLines {

    class PlateLine {
        LineSegment line;
        float confidence;
    }

    private static final float MIN_CONFIDENCE = 0.3f;

    public List<PlateLine> horizontalLines;
    public List<PlateLine> verticalLines;

    public List<Point> winningCorners;

    private PipelineData pipelineData;
    private boolean debug;


    public PlateLines(PipelineData*pipelineData) {
        this.pipelineData = pipelineData;

        this.debug = pipelineData -> config -> debugPlateLines;

        if (debug)
            cout << "PlateLines constructor" << endl;
    }


    void processImage(Mat inputImage, List<TextLine> textLines, float sensitivity) {
        if (this.debug)
            cout << "PlateLines findLines" << endl;

        timespec startTime;
        getTimeMonotonic( & startTime);


        // Ignore input images that are pure white or pure black
        Scalar avgPixelIntensity = mean(inputImage);
        if (avgPixelIntensity.get(0) >= 252)
            return;
        else if (avgPixelIntensity.get(0) <= 3)
            return;

        // Do a bilateral filter to clean the noise but keep edges sharp
        Mat smoothed = new Mat(inputImage.size(), inputImage.type());
        Imgproc.bilateralFilter(inputImage, smoothed, 3, 45, 45);

        Mat edges = new Mat(inputImage.size(), inputImage.type());
        Imgproc.Canny(smoothed, edges, 66, 133);

        // Create a mask that is dilated based on the detected characters


        Mat mask = Mat.zeros(inputImage.size(), CvType.CV_8U);

        for (int i = 0; i < textLines.size(); i++) {
            List<MatOfPoint> polygons = new ArrayList<>();
            polygons.add(textLines.get(i).textArea);
            Imgproc.fillPoly(mask, polygons, new Scalar(255, 255, 255));
        }


        Imgproc.dilate(mask, mask, getStructuringElement(1, new Size(1 + 1, 2 * 1 + 1), new Point(1, 1)));
        bitwise_not(mask, mask);

        // AND canny edges with the character mask
        bitwise_and(edges, mask, edges);


        List<PlateLine> hlines = this.getLines(edges, sensitivity, false);
        List<PlateLine> vlines = this.getLines(edges, sensitivity, true);
        for (int i = 0; i < hlines.size(); i++)
            this.horizontalLines.push_back(hlines.get(i));
        for (int i = 0; i < vlines.size(); i++)
            this.verticalLines.add(vlines.get(i));

        // if debug is enabled, draw the image
        if (this.debug) {
            Mat debugImgHoriz = new Mat(edges.size(), edges.type());
            Mat debugImgVert = new Mat(edges.size(), edges.type());
            edges.copyTo(debugImgHoriz);
            edges.copyTo(debugImgVert);
            Imgproc.cvtColor(debugImgHoriz, debugImgHoriz, Imgproc.COLOR_GRAY2BGR);
            Imgproc.cvtColor(debugImgVert, debugImgVert, Imgproc.COLOR_GRAY2BGR);

            for (int i = 0; i < this.horizontalLines.size(); i++) {
                Imgproc.line(debugImgHoriz, this.horizontalLines.get(i).line.p1, this.horizontalLines.get(i).line.p2, Scalar(0, 0, 255), 1, CV_AA);
            }

            for (int i = 0; i < this.verticalLines.size(); i++) {
                Imgproc.line(debugImgVert, this.verticalLines.get(i).line.p1, this.verticalLines.get(i).line.p2, Scalar(0, 0, 255), 1, CV_AA);
            }

            List<Mat> images = new ArrayList<>();
            images.add(debugImgHoriz);
            images.add(debugImgVert);

            Mat dashboard = drawImageDashboard(images, debugImgVert.type(), 1);
            displayImage(pipelineData -> config, "Hough Lines", dashboard);
        }

        if (pipelineData -> config -> debugTiming) {
            timespec endTime;
            getTimeMonotonic( & endTime);
            cout << "Plate Lines Time: " << diffclock(startTime, endTime) << "ms." << endl;
        }

    }


    List<PlateLine> getLines(Mat edges, float sensitivityMultiplier, boolean vertical) {
        if (this.debug)
            cout << "PlateLines::getLines" << endl;

        int HORIZONTAL_SENSITIVITY = pipelineData -> config -> plateLinesSensitivityHorizontal;
        int VERTICAL_SENSITIVITY = pipelineData -> config -> plateLinesSensitivityVertical;

        List<Vec2f> allLines = new ArrayList<>();
        List<PlateLine> filteredLines = new ArrayList<>();

        int sensitivity;
        if (vertical)
            sensitivity = (int) (VERTICAL_SENSITIVITY * (1.0 / sensitivityMultiplier));
        else
            sensitivity = (int) (HORIZONTAL_SENSITIVITY * (1.0 / sensitivityMultiplier));

        Imgproc.HoughLines(edges, allLines, 1, Imgproc.CV_PI / 180, sensitivity, 0, 0);

        for (int i = 0; i < allLines.size(); i++) {
            float rho = allLines.get(i).get(0), theta = allLines.get(i).get(1);
            Point pt1, pt2;
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;

            double angle = theta * (180 / CV_PI);
            pt1.x = cvRound(x0 + 1000 * (-b));
            pt1.y = cvRound(y0 + 1000 * (a));
            pt2.x = cvRound(x0 - 1000 * (-b));
            pt2.y = cvRound(y0 - 1000 * (a));

            if (vertical) {
                if (angle < 20 || angle > 340 || (angle > 160 && angle < 210)) {
                    // good vertical

                    LineSegment line;
                    if (pt1.y <= pt2.y)
                        line = new LineSegment(pt2.x, pt2.y, pt1.x, pt1.y);
                    else
                        line = new LineSegment(pt1.x, pt1.y, pt2.x, pt2.y);

                    // Get rid of the -1000, 1000 stuff.  Terminate at the edges of the image
                    // Helps with debugging/rounding issues later
                    LineSegment top = new LineSegment(0, 0, edges.cols(), 0);
                    LineSegment bottom = new LineSegment(0, edges.rows(), edges.cols(), edges.rows());
                    Point p1 = line.intersection(bottom);
                    Point p2 = line.intersection(top);

                    PlateLine plateLine = new PlateLine();
                    plateLine.line = new LineSegment(p1.x, p1.y, p2.x, p2.y);
                    plateLine.confidence = (float) ((1.0 - MIN_CONFIDENCE) * ((float) (allLines.size() - i)) / ((float) allLines.size()) + MIN_CONFIDENCE);
                    filteredLines.add(plateLine);
                }
            } else {
                if ((angle > 70 && angle < 110) || (angle > 250 && angle < 290)) {
                    // good horizontal

                    LineSegment line;
                    if (pt1.x <= pt2.x)
                        line = new LineSegment(pt1.x, pt1.y, pt2.x, pt2.y);
                    else
                        line = new LineSegment(pt2.x, pt2.y, pt1.x, pt1.y);

                    // Get rid of the -1000, 1000 stuff.  Terminate at the edges of the image
                    // Helps with debugging/ rounding issues later
                    int newY1 = (int) line.getPointAt(0);
                    int newY2 = (int) line.getPointAt(edges.cols());

                    PlateLine plateLine = new PlateLine();
                    plateLine.line = new LineSegment(0, newY1, edges.cols(), newY2);
                    plateLine.confidence = (float) ((1.0 - MIN_CONFIDENCE) * ((float) (allLines.size() - i)) / ((float) allLines.size()) + MIN_CONFIDENCE);
                    filteredLines.add(plateLine);
                }
            }
        }

        return filteredLines;
    }

    Mat customGrayscaleConversion(Mat src) {
        Mat img_hsv = new Mat();
        Imgproc.cvtColor(src, img_hsv, Imgproc.COLOR_BGR2HSV);

        Mat grayscale = new Mat(img_hsv.size(), CvType.CV_8U);
        Mat hue = new Mat(img_hsv.size(), CvType.CV_8U);

        for (int row = 0; row < img_hsv.rows(); row++) {
            for (int col = 0; col < img_hsv.cols(); col++) {
                int h = (int) img_hsv.at < Vec3b > (row, col).get(0);
                //int s = (int) img_hsv.at<Vec3b>(row, col).get(1);
                int v = (int) img_hsv.at < Vec3b > (row, col).get(2);

                int pixval = pow(v, 1.05);

                if (pixval > 255)
                    pixval = 255;
                grayscale.at<int> (row, col)=pixval;

                hue.at<int> (row, col)=h * (255.0 / 180.0);
            }
        }

        //displayImage(config, "Hue", hue);
        return grayscale;
    }

}
