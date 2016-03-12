package com.superbool.easylpr.ztest;

import com.superbool.easylpr.util.FileUtil;
import com.superbool.easylpr.util.ImageViewer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by kofee on 2016/3/12.
 */
public class UtilTest {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //注意程序运行的时候需要在VM option添加该行 指明opencv的dll文件所在路径
        //-Djava.library.path=$PROJECT_DIR$\opencv\x64
    }

    private static Mat src = FileUtil.imreadRes("image/test_white.jpg");

    public static void main(String[] args) {
        //imshow();
        houghLines();
    }

    private static void imshow() {
        ImageViewer imageViewer = new ImageViewer(src, "src");
        imageViewer.imshow();
    }

    private static void houghLines() {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        ImageViewer imageViewer = new ImageViewer(gray, "gray");
        imageViewer.imshow();

        Mat midImage = new Mat();
        Imgproc.Canny(src, midImage, 50, 200);//进行一此canny边缘检测
        ImageViewer midViewer = new ImageViewer(midImage, "midImage");
        midViewer.imshow();


        Mat hlMat = new Mat();
        Imgproc.HoughLines(midImage, hlMat, 1, Math.PI / 180, 150);

        System.out.println(midImage);

        System.out.println(hlMat);
        System.out.println(hlMat.dump());
        //ImageViewer hlViewer = new ImageViewer(hlMat, "hlMat");
        //hlViewer.imshow();
    }
}
