package base.enums;

/**
 * SCA enums
 * 
 * @Author: DoneEI
 * @Since: 2021/2/15 9:58 PM
 **/
public enum SCAEnums {
    /**
     * sca from source code comments
     */
    SOURCE_CODE_COMMENT("SOURCE_CODE_COMMENT"),

    /**
     * sca from other sources
     */
    NON_SOURCE_CODE_COMMENT("NON_SOURCE_CODE_COMMENT");

    /**
     * code
     */
    private String code;

    SCAEnums(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
