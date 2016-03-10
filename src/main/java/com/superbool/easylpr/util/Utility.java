package com.superbool.easylpr.util;

import org.opencv.core.Point;

/**
 * Created by kofee on 2016/3/9.
 */
public class Utility {

    /**
     * 计算两点之间的距离
     *
     * @param p1 点p1
     * @param p2 点p2
     * @return 两点之间距离
     */
    public static double distanceBetweenPoints(Point p1, Point p2) {
        double asquared = (p2.x - p1.x) * (p2.x - p1.x);
        double bsquared = (p2.y - p1.y) * (p2.y - p1.y);

        return Math.sqrt(asquared + bsquared);
    }

    /**
     * 计算两点之间的角度
     *
     * @param p1
     * @param p2
     * @return
     */
    public static double angleBetweenPoints(Point p1, Point p2) {
        double deltaY = p2.y - p1.y;
        double deltaX = p2.x - p1.x;

        return Math.atan2((float) deltaY, (float) deltaX) * (180 / Math.PI);
    }
}
