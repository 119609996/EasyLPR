/*
package com.superbool.easylpr.edges;

import com.superbool.easylpr.Config;
import com.superbool.easylpr.PipelineData;
import com.superbool.easylpr.Transformation;
import com.superbool.easylpr.textdetection.LineSegment;
import com.superbool.easylpr.textdetection.TextLine;
import com.superbool.easylpr.util.Utility;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Created by kofee on 2016/3/10.
 *//*

public class EdgeFinder {
    private static final Logger logger = LoggerFactory.getLogger(EdgeFinder.class);

    private PipelineData pipeline_data;

    List<Point> findEdgeCorners() {

        boolean high_contrast = is_high_contrast(pipeline_data.crop_gray);

        List<Point> returnPoints = new ArrayList<>();

        if (high_contrast) {
            // Try a high-contrast pass first.  If it doesn't return anything, try a normal detection
            returnPoints = detection(true);
        }

        if (!high_contrast || returnPoints.size() == 0) {
            returnPoints = detection(false);
        }

        return returnPoints;

    }

    List<Point> detection(boolean high_contrast) {
        TextLineCollection tlc = new TextLineCollection(pipeline_data.textLines);

        List<Point> corners = new ArrayList<>();


        // If the character segment is especially small, just expand the existing box
        // If it's a nice, long segment, then guess the correct box based on character height/position
        if (high_contrast) {
            Mat crop_gray = new Mat();
            int expandX = (int) (crop_gray.cols() * 0.5);
            int expandY = (int) (crop_gray.rows() * 0.5);
            int w = crop_gray.cols();
            int h = crop_gray.rows();

            corners.add(new Point(-1 * expandX, -1 * expandY));
            corners.add(new Point(expandX + w, -1 * expandY));
            corners.add(new Point(expandX + w, expandY + h));
            corners.add(new Point(-1 * expandX, expandY + h));

        } else if (tlc.longerSegment.getLength() > tlc.charHeight * 3) {

            double charHeightToPlateWidthRatio = pipeline_data.config.plateWidthMM / pipeline_data.config.avgCharHeightMM;
            double idealPixelWidth = tlc.charHeight * (charHeightToPlateWidthRatio * 1.03);    // Add 3% so we don't clip any characters

            double charHeightToPlateHeightRatio = pipeline_data.config.plateHeightMM / pipeline_data.config.avgCharHeightMM;
            double idealPixelHeight = tlc.charHeight * charHeightToPlateHeightRatio;


            double verticalOffset = idealPixelHeight * 1.5 / 2;
            double horizontalOffset = idealPixelWidth * 1.25 / 2;

            LineSegment topLine = tlc.centerHorizontalLine.getParallelLine(verticalOffset);
            LineSegment bottomLine = tlc.centerHorizontalLine.getParallelLine(-1 * verticalOffset);

            LineSegment leftLine = tlc.centerVerticalLine.getParallelLine(-1 * horizontalOffset);
            LineSegment rightLine = tlc.centerVerticalLine.getParallelLine(horizontalOffset);

            Point topLeft = topLine.intersection(leftLine);
            Point topRight = topLine.intersection(rightLine);
            Point botRight = bottomLine.intersection(rightLine);
            Point botLeft = bottomLine.intersection(leftLine);

            corners.add(topLeft);
            corners.add(topRight);
            corners.add(botRight);
            corners.add(botLeft);
        } else {

            int expandX = (int) ((int) ((double) pipeline_data.crop_gray.cols()) * 0.15f);
            int expandY = (int) ((int) ((double) pipeline_data.crop_gray.rows()) * 0.15f);
            int w = pipeline_data.crop_gray.cols();
            int h = pipeline_data.crop_gray.rows();

            corners.add(new Point(-1 * expandX, -1 * expandY));
            corners.add(new Point(expandX + w, -1 * expandY));
            corners.add(new Point(expandX + w, expandY + h));
            corners.add(new Point(-1 * expandX, expandY + h));


        }

        // Re-crop an image (from the original image) using the new coordinates
        //使用新的坐标重新裁剪图像（从原始图像）
        Transformation imgTransform = new Transformation(pipeline_data.grayImg, pipeline_data.crop_gray, pipeline_data.regionOfInterest);
        List<Point> remappedCorners = imgTransform.transformSmallPointsToBigImage(corners);

        Size cropSize = imgTransform.getCropSize(remappedCorners, new Size(pipeline_data.config.templateWidthPx, pipeline_data.config.templateHeightPx));

        Mat transmtx = imgTransform.getTransformationMatrix(remappedCorners, cropSize);
        Mat newCrop = imgTransform.crop(cropSize, transmtx);

        // Re-map the textline coordinates to the new crop  
        List<TextLine> newLines = new ArrayList<>();
        for (int i = 0; i < pipeline_data.textLines.size(); i++) {
            List<Point> textArea = imgTransform.transformSmallPointsToBigImage( pipeline_data.textLines.get(i).textArea);
            List<Point> linePolygon = imgTransform.transformSmallPointsToBigImage(pipeline_data.textLines.get(i).linePolygon);

            List<Point> textAreaRemapped;
            List<Point> linePolygonRemapped;

            textAreaRemapped = imgTransform.remapSmallPointstoCrop(textArea, transmtx);
            linePolygonRemapped = imgTransform.remapSmallPointstoCrop(linePolygon, transmtx);

            newLines.add(new TextLine(textAreaRemapped, linePolygonRemapped, newCrop.size()));
        }

        List<Point> smallPlateCorners;

        if (high_contrast) {
            smallPlateCorners = highContrastDetection(newCrop, newLines);
        } else {
            smallPlateCorners = normalDetection(newCrop, newLines);
        }

        // Transform the best corner points back to the original image
        List<Point> imgArea = new ArrayList<>();
        imgArea.add(new Point(0, 0));
        imgArea.add(new Point(newCrop.cols(), 0));
        imgArea.add(new Point(newCrop.cols(), newCrop.rows()));
        imgArea.add(new Point(0, newCrop.rows()));
        Mat newCropTransmtx = imgTransform.getTransformationMatrix(imgArea, remappedCorners);

        List<Point> cornersInOriginalImg = new ArrayList<>();

        if (smallPlateCorners.size() > 0)
            cornersInOriginalImg = imgTransform.remapSmallPointstoCrop(smallPlateCorners, newCropTransmtx);

        return cornersInOriginalImg;
    }


    List<Point> normalDetection(Mat newCrop, List<TextLine> newLines) {
        // Find the PlateLines for this crop
        PlateLines plateLines = new PlateLines(pipeline_data);
        plateLines.processImage(newCrop, newLines, 1.05f);

        // Get the best corners
        PlateCorners cornerFinder = new PlateCorners(newCrop, plateLines, pipeline_data, newLines);
        return cornerFinder.findPlateCorners();
    }

    List<Point> highContrastDetection(Mat newCrop, List<TextLine> newLines) {


        List<Point> smallPlateCorners;

        if (pipeline_data.config.debugGeneral)
            std::cout << "Performing high-contrast edge detection" << std::endl;

        // Do a morphology operation.  Find the biggest white rectangle that fit most of the char area.

        int morph_size = 3;
        Mat closureElement = Imgproc.getStructuringElement(2, // 0 Rect, 1 cross, 2 ellipse
                new Size(2 * morph_size + 1, 2 * morph_size + 1),
                new Point(morph_size, morph_size));

        Imgproc.morphologyEx(newCrop, newCrop, Imgproc.MORPH_CLOSE, closureElement);
        Imgproc.morphologyEx(newCrop, newCrop, Imgproc.MORPH_OPEN, closureElement);

        Mat thresholded_crop;
        Imgproc.threshold(newCrop, thresholded_crop, 80, 255, Imgproc.THRESH_OTSU);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresholded_crop, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double MIN_AREA = (double) (0.05 * newCrop.cols() * newCrop.rows());
        for (int i = 0;
             i < contours.size(); i++) {
            if (Imgproc.contourArea(contours[i]) < MIN_AREA)
                continue;

            List<Point> smoothedPoints = new ArrayList<>();
            Imgproc.approxPolyDP(contours.get(i), smoothedPoints, 1, true);

            RotatedRect rrect = Imgproc.minAreaRect(smoothedPoints);

            Point[] rect_points = new Point[4];
            rrect.points(rect_points);

            List<Point> sorted_polygon_points = sortPolygonPoints(rect_points, newCrop.size());

            double polygon_width = (Utility.distanceBetweenPoints(sorted_polygon_points.get(0), sorted_polygon_points.get(1)) +
                    Utility.distanceBetweenPoints(sorted_polygon_points.get(3), sorted_polygon_points.get(2))) / 2;
            double polygon_height = (Utility.distanceBetweenPoints(sorted_polygon_points.get(2), sorted_polygon_points.get(1)) +
                    Utility.distanceBetweenPoints(sorted_polygon_points.get(3), sorted_polygon_points.get(0))) / 2;
            // If it touches the edges, disqualify it

            // Create an inner rect, and ztest to make sure all the points are within it
            int x_offset = (int) (newCrop.cols() * 0.1);
            int y_offset = (int) (newCrop.rows() * 0.1);
            Rect insideRect = new Rect(new Point(x_offset, y_offset), new Point(newCrop.cols() - x_offset, newCrop.rows() - y_offset));

            boolean isoutside = false;
            for (int ptidx = 0;
                 ptidx < sorted_polygon_points.size(); ptidx++) {
                if (!insideRect.contains(sorted_polygon_points[ptidx]))
                    isoutside = true;
            }
            if (isoutside)
                continue;

            // If the center is not centered, disqualify it
            double MAX_CLOSENESS_TO_EDGE_PERCENT = 0.2f;
            if (rrect.center.x < (newCrop.cols() * MAX_CLOSENESS_TO_EDGE_PERCENT) ||
                    rrect.center.x > (newCrop.cols() - (newCrop.cols() * MAX_CLOSENESS_TO_EDGE_PERCENT)) ||
                    rrect.center.y < (newCrop.rows() * MAX_CLOSENESS_TO_EDGE_PERCENT) ||
                    rrect.center.y > (newCrop.rows() - (newCrop.rows() * MAX_CLOSENESS_TO_EDGE_PERCENT))) {
                continue;
            }

            // Make sure the aspect ratio is somewhat close to a license plate.
            double aspect_ratio = polygon_width / polygon_height;
            double ideal_aspect_ratio = pipeline_data.config.plateWidthMM / pipeline_data.config.plateHeightMM;

            double ratio = ideal_aspect_ratio / aspect_ratio;

            if (ratio > 2 || ratio < 0.5)
                continue;

            // Make sure that the text line(s) are contained within it

            Rect rect_cover = rrect.boundingRect();
            for (int linenum = 0;
                 linenum < newLines.size(); linenum++) {
                for (int r = 0;
                     r < newLines[linenum].textArea.size();
                     r++) {
                    if (!rect_cover.contains(newLines[linenum].textArea[r])) {
                        isoutside = true;
                        break;
                    }
                }
            }
            if (isoutside)
                continue;


            for (int ridx = 0; ridx < 4; ridx++) {
                smallPlateCorners.add(sorted_polygon_points[ridx]);
            }


        }


        return smallPlateCorners;
    }

    */
/**
     * 判断是否是高对比度
     *
     * @param crop  灰度图像
     * @return
     *//*

    boolean is_high_contrast(final Mat crop) {

        int stride = 2;

        int rows = crop.rows();
        int cols = crop.cols() / stride;

        long startTime = System.currentTimeMillis();
        // 计算像素点强度
        double avg_intensity = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < crop.cols(); x += stride) {
                avg_intensity = avg_intensity + crop.get(y, x)[0];
            }
        }

        avg_intensity = avg_intensity / (rows * cols * 255);

        // Calculate RMS contrast
        double contrast = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < crop.cols(); x += stride) {
                contrast += Math.pow(((crop.get(y, x)[0] / 255.0) - avg_intensity), 2.0);
            }
        }
        contrast /= rows * cols;

        contrast = Math.pow(contrast, 0.5);

        logger.debug("高对比度计算时间{}", System.currentTimeMillis() - startTime);

        return contrast > Config.contrastDetectionThreshold;
    }

}
*/
