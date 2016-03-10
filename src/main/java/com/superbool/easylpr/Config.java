package com.superbool.easylpr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by kofee on 2016/3/10.
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    enum DETECTOR_TYPE {
        DETECTOR_LBP_CPU,
        DETECTOR_LBP_GPU,
        DETECTOR_MORPH_CPU,
        DETECTOR_LBP_OPENCL
    }

    public boolean loaded;

    public String config_file_path;

    public String country;

    public int detector;

    public float detection_iteration_increase;
    public int detectionStrictness;
    public float maxPlateWidthPercent;
    public float maxPlateHeightPercent;
    public int maxDetectionInputWidth;
    public int maxDetectionInputHeight;

    public float contrastDetectionThreshold;

    public boolean skipDetection;

    public boolean auto_invert;
    public boolean always_invert;

    public String prewarp;

    public int maxPlateAngleDegrees;

    public float minPlateSizeWidthPx;
    public float minPlateSizeHeightPx;

    public boolean multiline;

    public float plateWidthMM;
    public float plateHeightMM;

    public List<Float> charHeightMM;
    public List<Float> charWidthMM;

    public float avgCharHeightMM;
    public float avgCharWidthMM;

    public float charWhitespaceTopMM;
    public float charWhitespaceBotMM;
    public float charWhitespaceBetweenLinesMM;

    public int templateWidthPx;
    public int templateHeightPx;

    public int ocrImageWidthPx;
    public int ocrImageHeightPx;

    public int stateIdImageWidthPx;
    public int stateIdimageHeightPx;

    public float charAnalysisMinPercent;
    public float charAnalysisHeightRange;
    public float charAnalysisHeightStepSize;
    public int charAnalysisNumSteps;

    public float plateLinesSensitivityVertical;
    float plateLinesSensitivityHorizontal;

    public float segmentationMinSpeckleHeightPercent;
    public int segmentationMinBoxWidthPx;
    public float segmentationMinCharHeightPercent;
    public float segmentationMaxCharWidthvsAverage;

    public String detectorFile;

    public String ocrLanguage;
    public int ocrMinFontSize;

    public boolean mustMatchPattern;

    public float postProcessMinConfidence;
    public float postProcessConfidenceSkipLevel;
    public int postProcessMinCharacters;
    public int postProcessMaxCharacters;

    public String postProcessRegexLetters;
    public String postProcessRegexNumbers;

    public boolean debugGeneral;
    public boolean debugTiming;
    public boolean debugPrewarp;
    public boolean debugDetector;
    public boolean debugStateId;
    public boolean debugPlateLines;
    public boolean debugPlateCorners;
    public boolean debugCharSegmenter;
    public boolean debugCharAnalysis;
    public boolean debugColorFiler;
    public boolean debugOcr;
    public boolean debugPostProcess;
    public boolean debugShowImages;
    public boolean debugPauseOnFrame;

    public String runtimeBaseDir;

    public List<String> loaded_countries;

    private float ocrImagePercent;
    private float stateIdImagePercent;


    private static final String RUNTIME_DIR = "/runtime_data";
    private static final String CONFIG_FILE = "/openalpr.conf";
    private static final String KEYPOINTS_DIR = "/keypoints";
    private static final String CASCADE_DIR = "/region/";
    private static final String POSTPROCESS_DIR = "/postprocess";
    private static final String DEFAULT_CONFIG_FILE = "/etc/openalpr/openalpr.conf";
    private static final String ENV_VARIABLE_CONFIG_FILE = "OPENALPR_CONFIG_FILE";


}
