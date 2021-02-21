package base.utils;

import base.config.SystemConfig;
import base.config.UserConfig;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This utils try its best to find the (coarse-grained) context of sca.
 * 
 * @Author: DoneEI
 * @Since: 2021/2/10 5:23 PM
 **/
public class ContextUtils {

    /**
     * we defined several regular expressions of method for different languages. These regex are not so strict but
     * useful.
     */
    private static final Pattern javaMethod = Pattern.compile(
        "^\\s*(public|private|protected)?((^|\\s+)(static|synchronized|final))*((^|\\s+)[\\w\\s<>,\\[\\]]+)?\\s+(\\w|_|$)+[(][\\[\\]\\w\\s_?.\\$<>,@]*[)]?(\\s*throws\\s*\\w+)?\\s*[{]?\\s*$");

    private static final Pattern pythonMethod =
        Pattern.compile("^\\s*(def|class)\\s+(\\w|_)*\\s*[(](,|\\w|\\s|_|=|.|\\$)*([)]\\s*:)?$");

    private static final Pattern defaultMethod = Pattern.compile(".*\\s+.*\\s*[(](,|\\w|\\s|_|\\$|:)*[)]\\s*[{]?\\s*$");

    /**
     * Comment pattern
     */
    private static final Pattern comment = Pattern.compile("^\\s*[*/#]+");

    public static String getContext(List<String> lines, int current, String fileName) {
        int startLine = Math.max(current - UserConfig.SCA_CONTEXT_WINDOW, 0);
        int endLine = Math.min(current + UserConfig.SCA_CONTEXT_WINDOW, lines.size() - 1);

        if (fileName.endsWith(".py")) {
            return parsePython(lines, current, startLine, endLine);
        } else {
            int[] depths = new int[endLine - startLine + 1];
            int[] comments = new int[endLine - startLine + 1];

            if (fileName.endsWith(".java")) {
                return parseOthers(lines, current, startLine, endLine, depths, comments, javaMethod);
            } else {
                return parseOthers(lines, current, startLine, endLine, depths, comments, defaultMethod);
            }
        }
    }

    private static String parseOthers(List<String> lines, int current, int startLine, int endLine, int[] depths,
        int[] comments, Pattern method) {

        int frontMethodStartIdx = -1, laterMethodStartIdx = -1, depth = 0;

        // forward traversal
        for (int i = current; i >= startLine; i--) {
            // is comment?
            Matcher cm = comment.matcher(lines.get(i));

            if (cm.find()) {
                comments[i - startLine] = 1;
            }

            depths[i - startLine] = depth;

            // calculate the depth of current line
            if (lines.get(i).contains("{")) {
                depth -= 1;
            }

            if (lines.get(i).contains("}")) {
                depth += 1;
            }

            if (frontMethodStartIdx == -1) {
                Matcher m = method.matcher(lines.get(i));

                if (m.find()) {
                    frontMethodStartIdx = i;
                }
            }

        }

        depth = 0;
        // backward traversal
        for (int i = current + 1; i <= endLine; i++) {
            // calculate the depth of current line
            if (lines.get(i).contains("{")) {
                depth += 1;
            }

            if (lines.get(i).contains("}")) {
                depth -= 1;
            }

            depths[i - startLine] = depth;

            if (laterMethodStartIdx == -1) {
                Matcher m = method.matcher(lines.get(i));

                if (m.find()) {
                    laterMethodStartIdx = i;
                }
            }

        }

        int resultStartLine = startLine, resultEndLine = endLine;

        if (frontMethodStartIdx != -1 && depths[frontMethodStartIdx - startLine] <= depths[current - startLine]) {
            for (int i = frontMethodStartIdx - 1; i >= startLine; i--) {
                if (comments[i - startLine] != 1) {
                    resultStartLine = i + 1;
                    break;
                }
            }

            for (int i = current + 1; i <= endLine; i++) {
                if (depths[i - startLine] < depths[frontMethodStartIdx - startLine]) {
                    resultEndLine = i;
                    break;
                }
            }

        } else {
            if (laterMethodStartIdx != -1 && depths[laterMethodStartIdx - startLine] > depths[current - startLine]) {
                for (int i = current - 1; i >= startLine; i--) {
                    if (comments[i - startLine] != 1) {
                        resultStartLine = i + 1;
                        break;
                    }
                }

                for (int i = laterMethodStartIdx; i <= endLine; i++) {
                    if (depths[i - startLine] < depths[laterMethodStartIdx - startLine]) {
                        resultEndLine = i;
                        break;
                    }
                }
            }
        }

        StringBuilder builder = new StringBuilder();

        for (int i = resultStartLine; i <= resultEndLine; i++) {
            builder.append(lines.get(i));
            builder.append(SystemConfig.SYSTEM_LINE_SEPARATOR);
        }

        return builder.toString();

    }

    private static String parsePython(List<String> lines, int current, int startLine, int endLine) {

        int frontMethodStartIdx = -1, laterMethodStartIdx = -1;

        // forward traversal
        for (int i = current; i >= startLine; i--) {

            Matcher m = pythonMethod.matcher(lines.get(i));

            if (m.find()) {
                frontMethodStartIdx = i;
                break;
            }

        }

        // backward traversal
        for (int i = current + 1; i <= endLine; i++) {
            Matcher m = pythonMethod.matcher(lines.get(i));

            if (m.find()) {
                laterMethodStartIdx = i;
                break;
            }

        }

        int resultStartLine = frontMethodStartIdx == -1 ? startLine : frontMethodStartIdx;
        int resultEndLine = laterMethodStartIdx == -1 ? endLine : laterMethodStartIdx - 1;

        StringBuilder builder = new StringBuilder();

        for (int i = resultStartLine; i <= resultEndLine; i++) {
            builder.append(lines.get(i));
            builder.append(SystemConfig.SYSTEM_LINE_SEPARATOR);
        }

        return builder.toString();
    }

}
