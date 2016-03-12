package com.superbool.easylpr.util;

import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Created by kofee on 2016/2/25.
 */
public class ImageViewer {
    private static final Logger logger = LoggerFactory.getLogger(ImageViewer.class);
    private JLabel imageView;

    private Mat image;
    private String windowName;

    /**
     * 如果使用junit测试时调用该方法，图像会一闪而过，可通过sleep()等方式暂时显示
     *
     * @param
     */


    public ImageViewer(Mat image) {
        this.image = image;
        this.windowName = windowName;
    }


    /**
     * @param image      要显示的mat
     * @param windowName 窗口标题
     */
    public ImageViewer(Mat image, String windowName) {
        this.image = image;
        this.windowName = windowName;
    }


    /**
     * 图片显示
     */
    public void imshow() {
        setSystemLookAndFeel();
        JFrame frame = createJFrame(windowName);
        Image loadedImage = toBufferedImage(image);
        imageView.setIcon(new ImageIcon(loadedImage));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 用户点击窗口关闭
    }

    private JFrame createJFrame(String windowName) {
        JFrame frame = new JFrame(windowName);
        imageView = new JLabel();
        final JScrollPane imageScrollPane = new JScrollPane(imageView);
        imageScrollPane.setPreferredSize(new Dimension(640, 480));
        frame.add(imageScrollPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }

    private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error("setSystemLookAndFeel error!", e);
        }
    }

    private Image toBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // get all the pixels
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}