package com.superbool.easylpr.util;

import org.opencv.core.Point;

/**
 * Created by kofee on 2016/3/9.
 */
public class Utility {

    public static double distanceBetweenPoints(Point p1, Point p2) {
        float asquared = (float) ((p2.x - p1.x) * (p2.x - p1.x));
        float bsquared = (float) ((p2.y - p1.y) * (p2.y - p1.y));

        return Math.sqrt(asquared + bsquared);
    }
}
