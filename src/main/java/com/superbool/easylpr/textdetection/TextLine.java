package com.superbool.easylpr.textdetection;

import com.superbool.easylpr.util.Utility;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kofee on 2016/3/9.
 */
public class TextLine {


    public MatOfPoint linePolygon;
    public MatOfPoint textArea;
    public LineSegment topLine;
    public LineSegment bottomLine;

    public LineSegment charBoxTop;
    public LineSegment charBoxBottom;
    public LineSegment charBoxLeft;
    public LineSegment charBoxRight;

    public float lineHeight;
    public float angle;

    public TextLine(List<Point> textArea, List<Point> linePolygon, Size imgSize) {
        List<Point> textAreaInts = new ArrayList<>();
        List<Point> linePolygonInts = new ArrayList<>();

        for (int i = 0; i < textArea.size(); i++)
            textAreaInts.add(new Point(Math.round(textArea.get(i).x), Math.round(textArea.get(i).y)));
        for (int i = 0; i < linePolygon.size(); i++)
            linePolygonInts.add(new Point(Math.round(linePolygon.get(i).x), Math.round(linePolygon.get(i).y)));

        initialize(textAreaInts, linePolygonInts, imgSize);
    }


    public void initialize(List<Point> textArea, List<Point> linePolygon, Size imgSize) {
        if (textArea.size() > 0) {
            if (textArea.size() > 0)
                textArea.clear();
            if (linePolygon.size() > 0)
                linePolygon.clear();

            for (int i = 0; i < textArea.size(); i++)
                textArea.add(textArea.get(i));

            topLine = new LineSegment(linePolygon.get(0).x, linePolygon.get(0).y, linePolygon.get(1).x, linePolygon.get(1).y);
            bottomLine = new LineSegment(linePolygon.get(3).x, linePolygon.get(3).y, linePolygon.get(2).x, linePolygon.get(2).y);

            // Adjust the line polygon so that it always touches the edges
            // This is needed after applying perspective transforms, so just fix it here
            if (linePolygon.get(0).x != 0) {
                linePolygon.get(0).x = 0;
                linePolygon.get(0).y = topLine.getPointAt((float) linePolygon.get(0).x);
            }
            if (linePolygon.get(1).x != imgSize.width) {
                linePolygon.get(1).x = imgSize.width;
                linePolygon.get(1).y = topLine.getPointAt((float) linePolygon.get(1).x);
            }
            if (linePolygon.get(2).x != imgSize.width) {
                linePolygon.get(2).x = imgSize.width;
                linePolygon.get(2).y = bottomLine.getPointAt((float) linePolygon.get(2).x);
            }
            if (linePolygon.get(3).x != 0) {
                linePolygon.get(3).x = 0;
                linePolygon.get(3).y = bottomLine.getPointAt((float) linePolygon.get(3).x);
            }


            this.linePolygon.fromList(linePolygon);


            charBoxTop = new LineSegment(textArea.get(0).x, textArea.get(0).y, textArea.get(1).x, textArea.get(1).y);
            charBoxBottom = new LineSegment(textArea.get(3).x, textArea.get(3).y, textArea.get(2).x, textArea.get(2).y);
            charBoxLeft = new LineSegment(textArea.get(3).x, textArea.get(3).y, textArea.get(0).x, textArea.get(0).y);
            charBoxRight = new LineSegment(textArea.get(2).x, textArea.get(2).y, textArea.get(1).x, textArea.get(1).y);

            // Calculate line height
            float x = ((float) linePolygon.get(1).x) / 2;
            Point midpoint = new Point(x, bottomLine.getPointAt(x));
            Point acrossFromMidpoint = topLine.closestPointOnSegmentTo(midpoint);
            lineHeight = (float) Utility.distanceBetweenPoints(midpoint, acrossFromMidpoint);

            // Subtract a pixel since the height is a little overestimated by the bounding box
            lineHeight = lineHeight - 1;

            angle = (topLine.angle + bottomLine.angle) / 2;

        }
    }


    Mat drawDebugImage(Mat baseImage) {
        Mat debugImage = new Mat(baseImage.size(), baseImage.type());

        baseImage.copyTo(debugImage);

        Imgproc.cvtColor(debugImage, debugImage, Imgproc.COLOR_GRAY2BGR);


        Imgproc.fillConvexPoly(debugImage, linePolygon, new Scalar(0, 0, 165));

        Imgproc.fillConvexPoly(debugImage, textArea, new Scalar(125, 255, 0));

        Imgproc.line(debugImage, topLine.p1, topLine.p2, new Scalar(255, 0, 0), 1);
        Imgproc.line(debugImage, bottomLine.p1, bottomLine.p2, new Scalar(255, 0, 0), 1);

        Imgproc.line(debugImage, charBoxTop.p1, charBoxTop.p2, new Scalar(0, 125, 125), 1);
        Imgproc.line(debugImage, charBoxLeft.p1, charBoxLeft.p2, new Scalar(0, 125, 125), 1);
        Imgproc.line(debugImage, charBoxRight.p1, charBoxRight.p2, new Scalar(0, 125, 125), 1);
        Imgproc.line(debugImage, charBoxBottom.p1, charBoxBottom.p2, new Scalar(0, 125, 125), 1);


        return debugImage;
    }
}
