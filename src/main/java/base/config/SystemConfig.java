package base.config;

/**
 * system configs that are private in the system
 *
 * @Author: DoneEI
 * @Since: 2021/2/9 4:33 PM
 **/
public class SystemConfig {

    /**
     * line separator
     */
    public static final char SYSTEM_LINE_SEPARATOR = '\n';

    /**
     * the file path of the jar
     */
    public static final String BASE_APPLICATION_FILE_PATH = System.getProperty("user.dir");
}
