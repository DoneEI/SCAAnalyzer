package base.utils;

import java.util.regex.Pattern;

/**
 * String utils
 *
 * @author DoneEI
 * @since 2019/8/19 4:49 PM
 */
public class StringUtils {

    /**
     * pattern for cleaning the unexpected characters in the str
     */
    private static Pattern cleanUnexpectedCharPattern = Pattern.compile("[-*#/]");

    /**
     * pattern for only keeping ordinary characters in the str
     */
    private static Pattern keepOrdinaryCharPattern = Pattern.compile("[\\W]");

    /**
     * pattern for cleaning the unexpected characters in the str
     */
    private static Pattern cleanWhiteSpacePattern = Pattern.compile("\\s+");

    /**
     * judge whether s1 equals s2
     *
     * @param s1
     *            str1
     * @param s2
     *            str2
     * @return equal?
     */
    public static boolean equal(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    /**
     * judge whether str is empty
     *
     * @param s
     *            目标字符串
     */
    public static boolean isNotEmpty(String s) {
        return s != null && !"".equals(s);
    }

    /**
     * clean string text
     * 
     * @param text
     *            dirty string
     * @return clean string
     */
    public static String cleanText(String text) {
        text = cleanUnexpectedCharPattern.matcher(text).replaceAll(" ");
        text = cleanWhiteSpacePattern.matcher(text).replaceAll(" ");

        return text;
    }

    /**
     * clean string text
     * 
     * @param text
     *            dirty string
     * @return clean string
     */
    public static String keepOrdinaryChar(String text) {
        text = keepOrdinaryCharPattern.matcher(text).replaceAll(" ");
        text = cleanWhiteSpacePattern.matcher(text).replaceAll(" ");

        return text;
    }

}
