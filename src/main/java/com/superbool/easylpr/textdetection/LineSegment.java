package com.superbool.easylpr.textdetection;

import com.superbool.easylpr.util.Utility;
import org.opencv.core.Point;

/**
 * Created by kofee on 2016/3/9.
 * 定义一条线段 根据平面直角坐标系
 */
public class LineSegment {

    private Point p1;
    private Point p2;
    private double slope;   //斜率
    private double length;  //长度
    private double angle;   //角度

    //只允许get操作
    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    public double getSlope() {
        return slope;
    }

    public double getLength() {
        return length;
    }

    public double getAngle() {
        return angle;
    }

    //两个初始化方法
    public LineSegment(double x1, double y1, double x2, double y2) {
        p1 = new Point(x1, y1);
        p2 = new Point(x2, y2);
        init();
    }

    public LineSegment(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        init();
    }

    private void init() {
        if (p2.x - p1.x == 0) {
            slope = Long.MIN_VALUE;
        } else {
            slope = (p2.y - p1.y) / (p2.x - p1.x);
        }

        length = Utility.distanceBetweenPoints(p1, p2);

        angle = Utility.angleBetweenPoints(p1, p2);
    }

    /**
     * 判断点是否在直线下方
     *
     * @param tp
     * @return
     */
    public boolean isPointBelowLine(Point tp) {
        return ((p2.x - p1.x) * (tp.y - p1.y) - (p2.y - p1.y) * (tp.x - p1.x)) > 0;
    }

    /**
     * 横坐标x 求纵坐标y
     *
     * @param x
     * @return
     */
    public double getPointAt(double x) {
        return slope * (x - p2.x) + p2.y;
    }

    /**
     * 纵坐标x 求横坐标x
     *
     * @param y
     * @return
     */
    public double getXPointAt(double y) {
        double y_intercept = getPointAt(0);
        return (y - y_intercept) / slope;
    }

    /**
     * 求线段所在直线上离p最近的点
     *
     * @param p
     * @return
     */
    public Point closestPointOnSegmentTo(Point p) {
        double top = (p.x - p1.x) * (p2.x - p1.x) + (p.y - p1.y) * (p2.y - p1.y);

        double bottom = Utility.distanceBetweenPoints(p2, p1);
        bottom = bottom * bottom;

        double u = top / bottom;

        double x = p1.x + u * (p2.x - p1.x);
        double y = p1.y + u * (p2.y - p1.y);

        return new Point(x, y);
    }

    /**
     * 两条线段的所在直线的交点坐标
     *
     * @param line
     * @return
     */
    public Point intersection(LineSegment line) {
        double c1, c2;
        double intersection_X = 0, intersection_Y = 0;

        c1 = p1.y - slope * p1.x; // which is same as y2 - slope * x2

        c2 = line.p2.y - line.slope * line.p2.x; // which is same as y2 - slope * x2

        if ((slope - line.slope) == 0) {
            //两条直线平行 没有交点
            //std::cout << "No Intersection between the lines" << endl;
            return null;
        } else if (p1.x == p2.x) {
            //this直线垂直x轴
            // Line1 is vertical
            return new Point(p1.x, line.getPointAt(p1.x));
        } else if (line.p1.x == line.p2.x) {
            //line垂直x轴
            // Line2 is vertical
            return new Point(line.p1.x, getPointAt(line.p1.x));
        } else {
            intersection_X = (c2 - c1) / (slope - line.slope);
            intersection_Y = slope * intersection_X + c1;
        }

        return new Point(intersection_X, intersection_Y);
    }

    /**
     * 计算距离原直线的平行线
     *
     * @param distance
     * @return
     */
    public LineSegment getParallelLine(double distance) {
        double diff_x = p2.x - p1.x;
        double diff_y = p2.y - p1.y;
        double angle = Math.atan2(diff_x, diff_y);
        double dist_x = distance * Math.cos(angle);
        double dist_y = -distance * Math.sin(angle);

        //double offsetX = Math.round(dist_x);
        //double offsetY = Math.round(dist_y);

        LineSegment result = new LineSegment(p1.x + dist_x, p1.y + dist_y, p2.x + dist_x, p2.y + dist_y);

        return result;
    }

    /**
     * 计算两点的中点
     *
     * @return
     */
    public Point midpoint() {
        // Handle the case where the line is vertical
        if (p1.x == p2.x) {
            double ydiff = p2.y - p1.y;
            double y = p1.y + (ydiff / 2);
            return new Point(p1.x, y);
        }
        double diff = p2.x - p1.x;
        double midX = p1.x + (diff / 2);
        double midY = getPointAt(midX);

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
