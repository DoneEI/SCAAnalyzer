package core.extraction;

import base.config.UserConfig;
import base.enums.ResultEnums;
import base.exception.AnalyzerException;
import base.utils.ContextUtils;
import base.utils.FileUtils;
import base.utils.StringUtils;
import core.analysis.SCASimilarity;
import entity.SCA;
import ui.frame.Console;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * automatic detect self-claimed assumptions from the given version of framework
 *
 * @Author: DoneEI
 * @Since: 2021/2/9 1:41 PM
 **/
public class SCADetector {
    /**
     * the filepath of framework
     */
    private String framework;

    /**
     * the file of framework
     */
    private File frameworkFile;

    /**
     * version of framework
     */
    private String tag;

    /**
     * sca results of version of framework
     */
    private List<SCA> results;

    /**
     * To improve the performance of detector, we leave the lines that contain more than 4096 characters and let the
     * user process them manually.
     */
    private List<String> unhandledLines;

    /**
     * pattern for all self-claimed assumptions
     */
    private final Pattern scaPattern =
        Pattern.compile("(^|[^.!?#/*]*\\W)assum(e|es|ed|ption|ptions|ing)(\\W[^.!?]*|$)?([.!?]|$)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    public SCADetector(String framework, String tag, String filePath) {
        // init
        this.framework = framework;
        this.tag = tag;
        this.frameworkFile = new File(filePath);

        this.results = new ArrayList<>();
        this.unhandledLines = Collections.synchronizedList(new ArrayList<>());

        // simple check
        if (!frameworkFile.exists()) {
            throw new AnalyzerException(ResultEnums.FILE_NOT_EXIST, String.format("file: %s does not exist", filePath));
        }

        if (!frameworkFile.isDirectory()) {
            throw new AnalyzerException(ResultEnums.FILE_TYPE_ERROR,
                String.format("file: %s is not a expected directory", filePath));
        }

    }

    public Map<String, List> detect() throws InterruptedException {
        File[] child;

        // double check
        if (frameworkFile == null || (child = frameworkFile.listFiles()) == null) {
            throw new AnalyzerException(ResultEnums.FILE_NOT_EXIST,
                String.format("we can not find the file of the framework: %s", framework));
        }

        int threads_num = Math.min(child.length, UserConfig.SCA_DETECTOR_THREAD_NUM);

        // A synchronization aid that allows the main thread to wait until
        // a set of operations being performed in other threads completes.
        CountDownLatch lock = new CountDownLatch(threads_num);

        int part = child.length / threads_num;

        for (int i = 0; i < threads_num; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                int start = part * finalI;
                int end = finalI == threads_num - 1 ? child.length : start + part;

                List<SCA> res = detectHelper(start, end, child);

                synchronized (this) {
                    results.addAll(res);
                }

                // countDown lock when it completes
                lock.countDown();
            });

            thread.start();
        }

        lock.await();

        Map<String, List> res = new HashMap<>(2);

        // filter
        SCAFilter.filter(results);

        res.put("SCA", results);
        res.put("unhandledLines", unhandledLines);

        return res;
    }

    private List<SCA> detectHelper(int start, int end, File... files) {
        if (files == null || files.length == 0) {
            return null;
        }

        List<SCA> scaList = new ArrayList<>();

        for (int t = start; t < end; t++) {
            Stack<File> stack = new Stack<>();

            stack.push(files[t]);

            while (!stack.empty()) {
                File f = stack.pop();

                if (f == null) {
                    continue;
                }

                if (f.isFile()) {
                    String filePath = FileUtils.getRelativePath(f.getAbsolutePath(), frameworkFile.getAbsolutePath());

                    // read file
                    List<String> lines = FileUtils.readFileByLines(f);

                    for (int i = 0; i < lines.size(); i++) {
                        if (lines.get(i).length() > UserConfig.SCA_DETECTOR_LINES_MAX_CHARACTERS) {
                            unhandledLines.add(filePath + "(" + (i + 1) + ")");
                            continue;
                        }

                        Matcher match = scaPattern.matcher(lines.get(i));

                        while (match.find()) {
                            // if match.find() is true, it means this line contains keyword of assumption
                            SCA sca =
                                new SCA(framework, tag, filePath, (i + 1), "assum" + match.group(2).toLowerCase());

                            StringBuilder windowContext = new StringBuilder();

                            // search the whole string of assumption in a window of context
                            for (int j = Math.max(0, i - UserConfig.SCA_DETECTOR_CONTENT_WINDOW);
                                j <= Math.min(lines.size() - 1, i + UserConfig.SCA_DETECTOR_CONTENT_WINDOW); j++) {
                                windowContext.append(lines.get(j));
                                windowContext.append(' ');
                            }

                            Matcher m = scaPattern.matcher(StringUtils.cleanText(windowContext.toString()));

                            // we assume that m.find() is always true here, but we still check the results of m.find()
                            // to prove that all assumptions could be recalled.
                            if (m.find()) {
                                sca.setContent(m.group(0));
                            } else {
                                throw new AnalyzerException(ResultEnums.ERROR, String.format(
                                    "obvious assumption is not recorded in file: %s, line: %d", filePath, (i + 1)));
                            }

                            // searchContext
                            sca.setContext(ContextUtils.getContext(lines, i, Objects.requireNonNull(filePath)));

                            scaList.add(sca);
                        }
                    }

                } else if (f.isDirectory()) {
                    for (File file : Objects.requireNonNull(f.listFiles())) {
                        stack.push(file);
                    }
                }
            }

        }

        return scaList;
    }

}
