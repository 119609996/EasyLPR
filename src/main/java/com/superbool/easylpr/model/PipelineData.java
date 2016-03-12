package com.superbool.easylpr.model;

import com.superbool.easylpr.Config;
import com.superbool.easylpr.edges.ScoreKeeper;
import com.superbool.easylpr.textdetection.TextLine;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kofee on 2016/3/10.
 */
public class PipelineData {


    // Inputs
    //定义所有的变量
    public Config config;

    //PreWarp prewarp;

    public Mat colorImg;
    public Mat grayImg;
    public Rect regionOfInterest;

    public boolean isMultiline;

    public Mat crop_gray;

    public Mat color_deskewed;

    public boolean hasPlateBorder;
    public Mat plateBorderMask;
    public List<TextLine> textLines;

    public List<Mat> thresholds;

    public List<Point> plate_corners;


    // Outputs
    public boolean plate_inverted;

    public String region_code;
    public float region_confidence;

    public boolean disqualified;
    public String disqualify_reason;

    public ScoreKeeper confidence_weights;

    // Boxes around characters in cropped image
    // Each row in a multiline plate is an entry in the vector
    public List<List<Rect>> charRegions;

    // Same data, just not broken down by line
    public List<Rect> charRegionsFlat;


    public PipelineData(Mat colorImage, Rect regionOfInterest, Config config) {
        Mat grayImage = new Mat();

        if (colorImage.channels() > 2) {
            Imgproc.cvtColor(colorImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        } else {
            grayImage = colorImage;
        }

        this.init(colorImage, grayImage, regionOfInterest, config);
    }

    public PipelineData(Mat colorImage, Mat grayImg, Rect regionOfInterest, Config config) {
        this.init(colorImage, grayImg, regionOfInterest, config);
    }


    public void clearThresholds() {
        thresholds = new ArrayList<>();
    }

    public void init(Mat colorImage, Mat grayImage, Rect regionOfInterest, Config config) {
        this.colorImg = colorImage;
        this.grayImg = grayImage;
        this.regionOfInterest = regionOfInterest;
        this.config = config;
        this.region_confidence = 0;
        this.plate_inverted = false;
        this.disqualified = false;
        this.disqualify_reason = "";
    }
}
