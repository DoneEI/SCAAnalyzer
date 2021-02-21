package base.utils;

import base.enums.ResultEnums;
import base.exception.AnalyzerException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteFont;
import entity.SCA;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel utils
 *
 * @Author: DoneEI
 * @Since: 2021/2/11 9:41 PM
 **/
public class ExcelUtils {
    /**
     * simple write Excel - single sheet
     * 
     * @param path
     *            name of excel
     * @param fileName
     *            file name of excel (not including file path)
     * @param data
     *            data
     * @param dataCls
     *            class of data
     */
    public static void simpleWrite(String path, String fileName, Class<?>[] dataCls, String[] sheetNames,
        List<?>... data) throws IOException {
        ExcelWriter excelWriter = null;

        if (dataCls.length != sheetNames.length && dataCls.length != data.length) {
            throw new AnalyzerException(ResultEnums.ERROR, "The length of argument may not match");
        }

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            excelWriter = EasyExcel.write(path + File.separator + fileName).build();

            for (int i = 0; i < data.length; i++) {
                WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetNames[i]).head(dataCls[i]).build();
                excelWriter.write(data[i], writeSheet);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    public static <T> List<T> simpleRead(String filePath, Integer sheetIdx, Class<T> dataCls) {
        List<T> results = new ArrayList<>();

        ExcelReader excelReader = null;
        try {
            excelReader = EasyExcel.read(filePath).build();

            ReadSheet sheet =
                EasyExcel.readSheet(sheetIdx).head(dataCls).registerReadListener(new AnalysisEventListener<T>() {
                    @Override
                    public void invoke(T data, AnalysisContext analysisContext) {
                        results.add(data);
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                        // complete
                    }
                }).build();

            excelReader.read(sheet);
        } finally {
            if (excelReader != null) {
                excelReader.finish();
            }
        }

        return results;
    }
}
