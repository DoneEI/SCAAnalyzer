package entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Tag of framework
 * 
 * @Author: DoneEI
 * @Since: 2021/2/17 12:09 上午
 **/
@Data
public class Tag implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7142851551081009571L;

    /**
     * tag version
     */
    private String version;

    /**
     * the file (directory) of this tag
     */
    private String tagFilePath;

    /**
     * the assumptions of this tag
     */
    private List<SCA> assumptions;

    /**
     * the assumptions of this tag
     */
    private List<String> unhandledLines;

    public Tag(String version) {
        this.version = version;
    }
}
