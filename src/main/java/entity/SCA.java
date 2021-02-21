package entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: DoneEI
 * @Since: 2021/2/9 4:11 PM
 **/

@Data
public class SCA implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5934749274293776650L;

    /**
     * The framework that SCA belongs to
     */
    @ExcelProperty(value = "framework")
    private String framework;

    /**
     * version of framework
     */
    @ExcelProperty(value = "tag")
    private String tag;

    /**
     * file path (relative to the framework, contains file name)
     */
    @ExcelProperty(value = "file")
    private String filePath;

    /**
     * the line that the keyword appears in the file
     */
    @ExcelProperty(value = "line")
    private Integer line;

    /**
     * keyword (assume,assumed,assumes,assumption,assumptions,assuming)
     */
    @ExcelProperty(value = "Term")
    private String keyword;

    /**
     * type of SCA
     */
    @ExcelProperty(value = "source")
    private String type;

    /**
     * context of SCA
     */
    @ExcelProperty(value = "context")
    private String context;

    /**
     * content of SCA
     */
    @ExcelProperty(value = "assumption")
    private String content;

    /**
     * constructor
     */
    public SCA(String framework, String tag, String filePath, Integer line, String keyword) {
        this.framework = framework;
        this.tag = tag;
        this.filePath = filePath;
        this.line = line;
        this.keyword = keyword;
    }

    public SCA() {

    }
}
