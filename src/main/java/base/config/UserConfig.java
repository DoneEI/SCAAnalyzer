package base.config;

import base.enums.ResultEnums;
import base.exception.AnalyzerException;
import base.utils.StringUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * configs that can be set by users
 * 
 * @Author: DoneEI
 * @Since: 2021/2/9 6:13 PM
 **/
public class UserConfig {
    /**
     * SCADetector will match the whole string of assumption in the current context. the line range of context is
     * [max(0,currentLine - windowSize, min(fileEndLine, currentLine + windowSize)]
     */
    public static Integer SCA_DETECTOR_CONTENT_WINDOW = 1;

    /**
     * SCADetector will use multi-threads to scan the files in the directory. Users can adjust the number of threads
     * manually according to the actual situation.
     */
    public static Integer SCA_DETECTOR_THREAD_NUM = 3;

    /**
     * To improve the performance, we leave lines that are more than 4096 characters by default and let users manually
     * handle these lines. Users can adjust the number of this value according to the actual situation.
     */
    public static Integer SCA_DETECTOR_LINES_MAX_CHARACTERS = 4096;

    /**
     * The context window of SCA the line range of context is [max(0,currentLine - windowSize, min(fileEndLine,
     * currentLine + windowSize)]
     */
    public static Integer SCA_CONTEXT_WINDOW = 50;

    /**
     * The similarity threshold between two assumptions.
     */
    public static Double SCA_SIMILARITY_THRESHOLD = 0.7;

    /**
     * Since we focus on the SCA from the source code comment, we exclude assumptions from other sources. Here, we
     * define several types of file.
     */
    public static Set<String> SCA_FILTER = new HashSet<>(Arrays.asList("md", "td", "pbtxt", "rst"));

    public static void init(String filePath) {
        try {
            // read properties file
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            Properties p = new Properties();

            p.load(in);

            // set values
            if (StringUtils.isNotEmpty(p.getProperty("SCA_DETECTOR_CONTENT_WINDOW"))) {
                int newValue = Integer.parseInt(p.getProperty("SCA_DETECTOR_CONTENT_WINDOW"));

                if (newValue > 0) {
                    SCA_DETECTOR_CONTENT_WINDOW = newValue;
                } else {
                    SCA_DETECTOR_CONTENT_WINDOW = 1;
                }
            }

            if (StringUtils.isNotEmpty(p.getProperty("SCA_DETECTOR_THREAD_NUM"))) {
                int newValue = Integer.parseInt(p.getProperty("SCA_DETECTOR_THREAD_NUM"));

                if (newValue > 0) {
                    SCA_DETECTOR_THREAD_NUM = newValue;
                } else {
                    SCA_DETECTOR_THREAD_NUM = 3;
                }
            }

            if (StringUtils.isNotEmpty(p.getProperty("SCA_DETECTOR_LINES_MAX_CHARACTERS"))) {
                int newValue = Integer.parseInt(p.getProperty("SCA_DETECTOR_LINES_MAX_CHARACTERS"));

                if (newValue > 0) {
                    SCA_DETECTOR_LINES_MAX_CHARACTERS = newValue;
                } else {
                    SCA_DETECTOR_LINES_MAX_CHARACTERS = 4096;
                }
            }

            if (StringUtils.isNotEmpty(p.getProperty("SCA_CONTEXT_WINDOW"))) {
                int newValue = Integer.parseInt(p.getProperty("SCA_CONTEXT_WINDOW"));

                if (newValue > 0) {
                    SCA_CONTEXT_WINDOW = newValue;
                } else {
                    SCA_CONTEXT_WINDOW = 50;
                }
            }

            if (StringUtils.isNotEmpty(p.getProperty("SCA_SIMILARITY_THRESHOLD"))) {
                double newValue = Double.parseDouble(p.getProperty("SCA_SIMILARITY_THRESHOLD"));

                if (newValue > 0 && newValue <= 1) {
                    SCA_SIMILARITY_THRESHOLD = newValue;
                } else {
                    SCA_SIMILARITY_THRESHOLD = 0.7;
                }
            }

            if (StringUtils.isNotEmpty(p.getProperty("SCA_FILTER"))) {
                String[] filter_types = p.getProperty("SCA_FILTER").split(",");
                SCA_FILTER = new HashSet<>(Arrays.asList(filter_types));
            }

        } catch (Exception e) {
            throw new AnalyzerException(ResultEnums.ERROR, "error: can not load properties");
        }

    }
}
