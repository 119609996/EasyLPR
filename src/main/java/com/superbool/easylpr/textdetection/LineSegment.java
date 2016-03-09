package com.superbool.easylpr.textdetection;

import com.superbool.easylpr.util.Utility;
import org.opencv.core.Point;

/**
 * Created by kofee on 2016/3/9.
 * 定义一条线段
 */
public class LineSegment {

    public Point p1;
    public Point p2;
    public float slope;
    public float length;
    public float angle;

    // LineSegment(Point point1, Point point2);
    public LineSegment() {
    }

    public LineSegment(double x1, double y1, double x2, double y2) {
        p1 = new Point(x1, y1);
        p2 = new Point(x2, y2);
    }

    public LineSegment(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void init(double x1, double y1, double x2, double y2) {
        p1 = new Point(x1, y1);
        p2 = new Point(x2, y2);
    }

    public boolean isPointBelowLine(Point tp) {
        return ((p2.x - p1.x) * (tp.y - p1.y) - (p2.y - p1.y) * (tp.x - p1.x)) > 0;
    }

    public float getPointAt(float x) {
        return (float) (slope * (x - p2.x) + p2.y);
    }

    public float getXPointAt(float y) {
        float y_intercept = getPointAt(0);
        return (y - y_intercept) / slope;
    }

    public Point closestPointOnSegmentTo(Point p) {
        float top = (float) ((p.x - p1.x) * (p2.x - p1.x) + (p.y - p1.y) * (p2.y - p1.y));

        float bottom = (float) Utility.distanceBetweenPoints(p2, p1);
        bottom = bottom * bottom;

        float u = top / bottom;

        float x = (float) (p1.x + u * (p2.x - p1.x));
        float y = (float) (p1.y + u * (p2.y - p1.y));

        return new Point(x, y);
    }

    public Point intersection(LineSegment line) {
        float c1, c2;
        float intersection_X = -1, intersection_Y = -1;

        c1 = (float) (p1.y - slope * p1.x); // which is same as y2 - slope * x2

        c2 = (float) (line.p2.y - line.slope * line.p2.x); // which is same as y2 - slope * x2

        if ((slope - line.slope) == 0) {
            //std::cout << "No Intersection between the lines" << endl;
        } else if (p1.x == p2.x) {
            // Line1 is vertical
            return new Point(p1.x, line.getPointAt((float) p1.x));
        } else if (line.p1.x == line.p2.x) {
            // Line2 is vertical
            return new Point(line.p1.x, getPointAt((float) line.p1.x));
        } else {
            intersection_X = (c2 - c1) / (slope - line.slope);
            intersection_Y = slope * intersection_X + c1;
        }

        return new Point(intersection_X, intersection_Y);
    }

    public LineSegment getParallelLine(float distance) {
        float diff_x = (float) (p2.x - p1.x);
        float diff_y = (float) (p2.y - p1.y);
        float angle = (float) Math.atan2(diff_x, diff_y);
        float dist_x = (float) (distance * Math.cos(angle));
        float dist_y = (float) (-distance * Math.sin(angle));

        int offsetX = Math.round(dist_x);
        int offsetY = Math.round(dist_y);

        LineSegment result = new LineSegment(p1.x + offsetX, p1.y + offsetY,
                p2.x + offsetX, p2.y + offsetY);

        return result;
    }

    public Point midpoint() {
        // Handle the case where the line is vertical
        if (p1.x == p2.x) {
            float ydiff = (float) (p2.y - p1.y);
            float y = (float) (p1.y + (ydiff / 2));
            return new Point(p1.x, y);
        }
        float diff = (float) (p2.x - p1.x);
        float midX = ((float) p1.x) + (diff / 2);
        int midY = (int) getPointAt(midX);

        return new Point(midX, midY);
    }

    @Override
    public String toString() {
        return "LineSegment{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                ", slope=" + slope +
                ", length=" + length +
                ", angle=" + angle +
                '}';
    }


}
