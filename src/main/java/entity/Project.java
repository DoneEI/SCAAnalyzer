package entity;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Project
 *
 * @Author: DoneEI
 * @Since: 2021/2/16 11:03 下午
 **/
@Data
public class Project implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3862519784240475303L;

    /**
     * framework of this project
     */
    private String framework;

    /**
     * owner of the framework
     */
    private String owner;

    /**
     * file path of this project
     */
    private String filePath;

    /**
     * tags of framework
     */
    private Map<String, Tag> tags;

    public Map<String, Tag> getTags(boolean createIfEmpty) {
        if (getTags() == null) {
            if (createIfEmpty) {
                return new LinkedHashMap<>();
            }
        }

        return getTags();
    }

    public Project(String framework, String owner, String filePath) {
        this.framework = framework;
        this.owner = owner;
        this.filePath = filePath;
    }
}
