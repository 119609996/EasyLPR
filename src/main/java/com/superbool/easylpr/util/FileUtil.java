package com.superbool.easylpr.util;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by kofee on 2016/2/24.
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static final String resourcesPath;

    static {
        String classPath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
        classPath = classPath.substring(1, classPath.length());
        resourcesPath = classPath;
    }

    /**
     * opencv imread() imwrite()无法读取或者写入带有中文路径或者中文名的图片
     * 支持的格式目前已测试的有jpg、bmp、png
     * @param args
     */

    /**
     * 获取资源文件目录
     *
     * @return
     */
    public static String getResourcesPath() {
        return resourcesPath;
    }

    /**
     * 使用classpath路径作为资源路径读取
     *
     * @param filename
     * @return
     */
    public static Mat imreadRes(String filename) {
        return imreadAbs(resourcesPath + filename);
    }

    /**
     * 调用opencv的imread读取图片，使用绝对路径AbsolutePath，为空的时候返回null
     *
     * @param filename
     * @return
     */
    public static Mat imreadAbs(String filename) {
        logger.debug("读取图片的路径:filePath={}", filename);
        Mat mat = Imgcodecs.imread(filename);
        if (mat.dataAddr() == 0) {
            logger.warn("Count not read image file={} to mat !", filename);
            return null;
        } else {
            return mat;
        }
    }

    /**
     * 写入到classpath路径
     *
     * @param filename
     * @param mat
     * @return
     */
    public static boolean imwriteRes(String filename, Mat mat) {
        return imwriteAbs(resourcesPath + filename, mat);
    }

    /**
     * 写入到绝对路径
     * 由于opencv的imwrite()方法无法自动创建不存在的目录，所以需要自己先判断目录是否存在，然后再写入图像
     *
     * @param filename
     * @param mat
     * @return
     */
    public static boolean imwriteAbs(String filename, Mat mat) {
        File file = new File(filename);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        boolean result = Imgcodecs.imwrite(filename, mat);
        logger.debug("写入图片的路径:filePath={},status={}", filename, result);
        return result;
    }


    public static void drowROIRect(Mat src, List<RotatedRect> roiRects, String tag) {
        Mat debugMat = new Mat();
        src.copyTo(debugMat);
        for (RotatedRect roiRect : roiRects) {
            //在图中画出找到的矩形区域
            Point[] rect_points = new Point[4];
            roiRect.points(rect_points);
            for (int j = 0; j < 4; j++) {
                Imgproc.line(debugMat, rect_points[j], rect_points[(j + 1) % 4], new Scalar(0, 255, 255), 1, 8, 0);
            }

        }
        FileUtil.imwriteRes("image/temp/src_roi_rect_" + tag + ".jpg", debugMat);
    }
}
