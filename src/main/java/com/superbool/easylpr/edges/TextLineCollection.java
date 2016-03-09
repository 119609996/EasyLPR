package com.superbool.easylpr.edges;

import com.superbool.easylpr.textdetection.LineSegment;
import com.superbool.easylpr.textdetection.TextLine;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by kofee on 2016/3/9.
 */
public class TextLineCollection {


    private LineSegment topCharArea;
    private LineSegment bottomCharArea;


    public float charHeight;
    public float charAngle;

    public LineSegment centerHorizontalLine;
    public LineSegment centerVerticalLine;

    public LineSegment longerSegment;
    public LineSegment shorterSegment;


    public TextLineCollection(List<TextLine> textLines) {


        charHeight = 0;
        charAngle = 0;
        for (int i = 0; i < textLines.size(); i++) {
            charHeight += textLines.get(i).lineHeight;
            charAngle += textLines.get(i).angle;

        }
        charHeight = charHeight / textLines.size();
        charAngle = charAngle / textLines.size();

        this.topCharArea = textLines.get(0).charBoxTop;
        this.bottomCharArea = textLines.get(0).charBoxBottom;
        for (int i = 1; i < textLines.size(); i++) {

            if (this.topCharArea.isPointBelowLine(textLines.get(i).charBoxTop.midpoint()) == false)
                this.topCharArea = textLines.get(i).charBoxTop;

            if (this.bottomCharArea.isPointBelowLine(textLines.get(i).charBoxBottom.midpoint()))
                this.bottomCharArea = textLines.get(i).charBoxBottom;

        }

        longerSegment = this.bottomCharArea;
        shorterSegment = this.topCharArea;
        if (this.topCharArea.length > this.bottomCharArea.length) {
            longerSegment = this.topCharArea;
            shorterSegment = this.bottomCharArea;
        }

        findCenterHorizontal();
        findCenterVertical();
        // Center Vertical Line


    }

    public Mat getDebugImage(Size imageSize) {

        Mat debugImage = Mat.zeros(imageSize, CvType.CV_8U);
        Imgproc.line(debugImage, this.centerHorizontalLine.p1, this.centerHorizontalLine.p2, new Scalar(255, 255, 255), 2);
        Imgproc.line(debugImage, this.centerVerticalLine.p1, this.centerVerticalLine.p2, new Scalar(255, 255, 255), 2);

        return debugImage;

    }


    // Returns 1 for above, 0 for within, and -1 for below
    public int isAboveText(LineSegment line) {
        // Test four points (left and right corner of top and bottom line)

        Point topLeft = line.closestPointOnSegmentTo(topCharArea.p1);
        Point topRight = line.closestPointOnSegmentTo(topCharArea.p2);

        boolean lineIsBelowTop = topCharArea.isPointBelowLine(topLeft) || topCharArea.isPointBelowLine(topRight);

        if (!lineIsBelowTop)
            return 1;

        Point bottomLeft = line.closestPointOnSegmentTo(bottomCharArea.p1);
        Point bottomRight = line.closestPointOnSegmentTo(bottomCharArea.p2);

        boolean lineIsBelowBottom = bottomCharArea.isPointBelowLine(bottomLeft) &&
                bottomCharArea.isPointBelowLine(bottomRight);

        if (lineIsBelowBottom)
            return -1;

        return 0;

    }

    // Returns 1 for left, 0 for within, and -1 for to the right
    public int isLeftOfText(LineSegment line) {

        LineSegment leftSide = new LineSegment(bottomCharArea.p1, topCharArea.p1);

        Point topLeft = line.closestPointOnSegmentTo(leftSide.p2);
        Point bottomLeft = line.closestPointOnSegmentTo(leftSide.p1);

        boolean lineIsAboveLeft = (!leftSide.isPointBelowLine(topLeft)) && (!leftSide.isPointBelowLine(bottomLeft));

        if (lineIsAboveLeft)
            return 1;

        LineSegment rightSide = new LineSegment(bottomCharArea.p2, topCharArea.p2);

        Point topRight = line.closestPointOnSegmentTo(rightSide.p2);
        Point bottomRight = line.closestPointOnSegmentTo(rightSide.p1);


        boolean lineIsBelowRight = rightSide.isPointBelowLine(topRight) && rightSide.isPointBelowLine(bottomRight);

        if (lineIsBelowRight)
            return -1;

        return 0;
    }

    public void findCenterHorizontal() {
        // To find the center horizontal line:
        // Find the longer of the lines (if multiline)
        // Get the nearest point on the bottom-most line for the
        // left and right


        Point leftP1 = shorterSegment.closestPointOnSegmentTo(longerSegment.p1);
        Point leftP2 = longerSegment.p1;
        LineSegment left = new LineSegment(leftP1, leftP2);

        Point leftMidpoint = left.midpoint();


        Point rightP1 = shorterSegment.closestPointOnSegmentTo(longerSegment.p2);
        Point rightP2 = longerSegment.p2;
        LineSegment right = new LineSegment(rightP1, rightP2);

        Point rightMidpoint = right.midpoint();

        this.centerHorizontalLine = new LineSegment(leftMidpoint, rightMidpoint);

    }

    public void findCenterVertical() {
        // To find the center vertical line:
        // Choose the longest line (if multiline)
        // Get the midpoint
        // Draw a line up/down using the closest point on the bottom line


        Point p1 = longerSegment.midpoint();

        Point p2 = shorterSegment.closestPointOnSegmentTo(p1);

        // Draw bottom to top
        if (p1.y < p2.y)
            this.centerVerticalLine = new LineSegment(p1, p2);
        else
            this.centerVerticalLine = new LineSegment(p2, p1);
    }

}
