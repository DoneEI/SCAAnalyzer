package base.exception;

import base.enums.ResultEnums;

/**
 * self-defined exception
 *
 * @Author: DoneEI
 * @Since: 2021/2/9 3:04 PM
 **/
public class AnalyzerException extends RuntimeException {
    /**
     * error code
     */
    private ResultEnums errorCode;

    /**
     * error message for output
     */
    private String errorMsg;

    /**
     * constructor for this class
     * 
     * @param errorCode
     *            error code
     * @param errorMsg
     *            error message for output
     */
    public AnalyzerException(ResultEnums errorCode, String errorMsg) {
        this.errorMsg = errorMsg;
        this.errorCode = errorCode;
    }

    public ResultEnums getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
