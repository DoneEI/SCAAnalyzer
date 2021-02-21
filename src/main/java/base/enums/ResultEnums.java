package base.enums;

/**
 * result enums
 *
 * @Author: DoneEI
 * @Since: 2021/2/9 3:08 PM
 **/
public enum ResultEnums {
    /**
     * file exists
     */
    FILE_EXIST("FILE_EXIST"),

    /**
     * file does not exist
     */
    FILE_NOT_EXIST("FILE_NOT_EXIST"),

    /**
     * file type error
     */
    FILE_TYPE_ERROR("FILE_TYPE_ERROR"),

    /**
     * system error
     */
    ERROR("ERROR"),

    ;

    /**
     * code
     */
    private String code;

    ResultEnums(String code) {
        this.code = code;
    }
}
