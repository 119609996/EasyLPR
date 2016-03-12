package com.superbool.easylpr.edges;

import com.superbool.easylpr.model.PipelineData;
import com.superbool.easylpr.textdetection.LineSegment;
import com.superbool.easylpr.textdetection.TextLine;
import com.superbool.easylpr.util.ImageViewer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kofee on 2016/3/9.
 */
public class PlateLines {

    private static final Logger logger = LoggerFactory.getLogger(PlateLines.class);

    class PlateLine {
        LineSegment line;
        double confidence;
    }

    private static final double MIN_CONFIDENCE = 0.3f;

    public List<PlateLine> horizontalLines;
    public List<PlateLine> verticalLines;

    public List<Point> winningCorners;

    private PipelineData pipelineData;
    private boolean debug;


    /**
     * 构造方法
     *
     * @param pipelineData
     */
    public PlateLines(PipelineData pipelineData) {
        this.pipelineData = pipelineData;

        this.debug = pipelineData.config.debugPlateLines;

        logger.debug("PlateLines constructor");
    }


    public void processImage(Mat inputImage, List<TextLine> textLines, double sensitivity) {
        logger.debug("PlateLines findLines");

        long startTime = System.currentTimeMillis();


        // Ignore input images that are pure white or pure black
        //忽略输入纯白色或纯黑色图像
        Scalar avgPixelIntensity = Core.mean(inputImage);
        if (avgPixelIntensity.val[0] >= 252)
            return;
        else if (avgPixelIntensity.val[0] <= 3)
            return;

        // Do a bilateral filter to clean the noise but keep edges sharp
        //使用双边滤波来清除噪声，但保留边缘
        Mat smoothed = new Mat(inputImage.size(), inputImage.type());
        Imgproc.bilateralFilter(inputImage, smoothed, 3, 45, 45);

        Mat edges = new Mat(inputImage.size(), inputImage.type());
        Imgproc.Canny(smoothed, edges, 66, 133);

        // Create a mask that is dilated based on the detected characters
        //创建一个基于被检测到的字符的遮罩
        Mat mask = Mat.zeros(inputImage.size(), CvType.CV_8U);

        for (int i = 0; i < textLines.size(); i++) {
            List<MatOfPoint> polygons = new ArrayList<>();
            polygons.add(textLines.get(i).textArea);
            ////用指定颜色填充指定闭合的多边形
            Imgproc.fillPoly(mask, polygons, new Scalar(255, 255, 255));
        }


        Mat kernel = Imgproc.getStructuringElement(1, new Size(1 + 1, 2 * 1 + 1), new Point(1, 1));
        Imgproc.dilate(mask, mask, kernel);
        Core.bitwise_not(mask, mask);

        // AND canny edges with the character mask
        Core.bitwise_and(edges, mask, edges);


        List<PlateLine> hlines = getLines(edges, sensitivity, false);
        List<PlateLine> vlines = getLines(edges, sensitivity, true);
        for (int i = 0; i < hlines.size(); i++) {
            horizontalLines.add(hlines.get(i));
        }

        for (int i = 0; i < vlines.size(); i++) {
            verticalLines.add(vlines.get(i));
        }

        // if debug is enabled, draw the image
        if (this.debug) {
            Mat debugImgHoriz = new Mat(edges.size(), edges.type());
            Mat debugImgVert = new Mat(edges.size(), edges.type());
            edges.copyTo(debugImgHoriz);
            edges.copyTo(debugImgVert);
            Imgproc.cvtColor(debugImgHoriz, debugImgHoriz, Imgproc.COLOR_GRAY2BGR);
            Imgproc.cvtColor(debugImgVert, debugImgVert, Imgproc.COLOR_GRAY2BGR);

            for (int i = 0; i < this.horizontalLines.size(); i++) {
                Imgproc.line(debugImgHoriz, this.horizontalLines.get(i).line.getP1(), this.horizontalLines.get(i).line.getP2(), new Scalar(0, 0, 255), 1);
            }

            for (int i = 0; i < this.verticalLines.size(); i++) {
                Imgproc.line(debugImgVert, this.verticalLines.get(i).line.getP1(), this.verticalLines.get(i).line.getP2(), new Scalar(0, 0, 255), 1);
            }

            List<Mat> images = new ArrayList<>();
            images.add(debugImgHoriz);
            images.add(debugImgVert);

            ImageViewer imageHoriz = new ImageViewer(debugImgHoriz, "debugImgHoriz");
            ImageViewer imageVert = new ImageViewer(debugImgVert, "debugImgVert");
            imageHoriz.imshow();
            imageVert.imshow();
        }

        logger.debug("Plate Lines Time:{}ms", System.currentTimeMillis() - startTime);

    }


    private List<PlateLine> getLines(Mat edges, double sensitivityMultiplier, boolean vertical) {

        logger.debug("PlateLines::getLines");

        int HORIZONTAL_SENSITIVITY = (int) pipelineData.config.plateLinesSensitivityHorizontal;
        int VERTICAL_SENSITIVITY = (int) pipelineData.config.plateLinesSensitivityVertical;

        Mat allLinesMat = new Mat();
        List<PlateLine> filteredLines = new ArrayList<>();

        int sensitivity;
        if (vertical) {
            sensitivity = (int) (VERTICAL_SENSITIVITY * (1.0 / sensitivityMultiplier));
        } else {
            sensitivity = (int) (HORIZONTAL_SENSITIVITY * (1.0 / sensitivityMultiplier));
        }

        //image为输入图像，要求是8位单通道图像
        // lines为输出的直线向量，每条线用4个元素表示，即直线的两个端点的4个坐标值
        // rho和theta分别为距离和角度的分辨率
        //threshold为阈值
        Imgproc.HoughLines(edges, allLinesMat, 1, Math.PI / 180, sensitivity);
        for (int i = 0; i < allLinesMat.rows(); i++) {
            double rho = allLinesMat.get(i, 0)[0], theta = allLinesMat.get(i, 0)[1];
            Point pt1 = new Point(), pt2 = new Point();
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;

            double angle = theta * (180 / Math.PI);
            pt1.x = x0 + 1000 * (-b);
            pt1.y = y0 + 1000 * (a);
            pt2.x = x0 - 1000 * (-b);
            pt2.y = y0 - 1000 * (a);

            if (vertical) {
                if (angle < 20 || angle > 340 || (angle > 160 && angle < 210)) {
                    // good vertical

                    LineSegment line;
                    if (pt1.y <= pt2.y) {
                        line = new LineSegment(pt2.x, pt2.y, pt1.x, pt1.y);
                    } else {
                        line = new LineSegment(pt1.x, pt1.y, pt2.x, pt2.y);
                    }

                    // Get rid of the -1000, 1000 stuff.  Terminate at the edges of the image
                    // Helps with debugging/rounding issues later
                    LineSegment top = new LineSegment(0, 0, edges.cols(), 0);
                    LineSegment bottom = new LineSegment(0, edges.rows(), edges.cols(), edges.rows());
                    Point p1 = line.intersection(bottom);
                    Point p2 = line.intersection(top);

                    PlateLine plateLine = new PlateLine();
                    plateLine.line = new LineSegment(p1.x, p1.y, p2.x, p2.y);
                    plateLine.confidence = (1.0 - MIN_CONFIDENCE) * (allLinesMat.rows() - i) / allLinesMat.rows() + MIN_CONFIDENCE;
                    filteredLines.add(plateLine);
                }
            } else {
                if ((angle > 70 && angle < 110) || (angle > 250 && angle < 290)) {
                    // good horizontal

                    LineSegment line;
                    if (pt1.x <= pt2.x) {
                        line = new LineSegment(pt1.x, pt1.y, pt2.x, pt2.y);
                    }
                    else {
                        line = new LineSegment(pt2.x, pt2.y, pt1.x, pt1.y);
                    }

                    // Get rid of the -1000, 1000 stuff.  Terminate at the edges of the image
                    // Helps with debugging/ rounding issues later
                    double newY1 = line.getPointAt(0);
                    double newY2 = line.getPointAt(edges.cols());

                    PlateLine plateLine = new PlateLine();
                    plateLine.line = new LineSegment(0, newY1, edges.cols(), newY2);
                    plateLine.confidence = (1.0 - MIN_CONFIDENCE) * (allLinesMat.rows() - i) / allLinesMat.rows() + MIN_CONFIDENCE;
                    filteredLines.add(plateLine);
                }
            }
        }

        return filteredLines;
    }

    private Mat customGrayscaleConversion(Mat src) {
        Mat img_hsv = new Mat();
        Imgproc.cvtColor(src, img_hsv, Imgproc.COLOR_BGR2HSV);

        Mat grayscale = new Mat(img_hsv.size(), CvType.CV_8U);
        Mat hue = new Mat(img_hsv.size(), CvType.CV_8U);

        for (int row = 0; row < img_hsv.rows(); row++) {
            for (int col = 0; col < img_hsv.cols(); col++) {
                int h = (int) img_hsv.get(row, col)[0];
                //int s = (int) img_hsv.get(row, col)[1];
                int v = (int) img_hsv.get(row, col)[2];

                int pixval = (int) Math.pow(v, 1.05);

                if (pixval > 255) {
                    pixval = 255;
                }
                grayscale.put(row, col, pixval);

                hue.put(row, col, h * (255.0 / 180.0));
            }
        }

        //displayImage(config, "Hue", hue);
        return grayscale;
    }

}
