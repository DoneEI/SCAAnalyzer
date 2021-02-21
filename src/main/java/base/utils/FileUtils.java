package base.utils;

import base.config.SystemConfig;
import base.enums.ResultEnums;
import base.exception.AnalyzerException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * file utils
 * 
 * @Author: DoneEI
 * @Since: 2021/2/9 4:23 PM
 **/
public class FileUtils {

    /**
     * read file lines
     *
     * @param file
     *            file
     * @return List<String>
     */
    public static List<String> readFileByLines(File file) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            // 一次读一行，读入null时文件结束bai
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    /**
     * read file lines
     *
     * @param filePath
     *            file
     * @return List<String>
     */
    public static List<String> readFileByLines(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AnalyzerException(ResultEnums.ERROR, "error: unable to read file: " + filePath);
        }
    }

    public static void writeFile(String filePath, List<String> list) throws IOException {

        File file = new File(filePath);

        if (!file.exists()) {
            file.createNewFile();
        }

        Writer out = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(out);
        for (String s : list) {
            bw.write(s);
            bw.newLine();
            bw.flush();
        }
        bw.close();
    }

    /**
     * get path of cur relative to base
     *
     * @param cur
     *            current file path
     * @param base
     *            base file path
     * @return a relative path of cur
     */
    public static String getRelativePath(String cur, String base) {
        if (StringUtils.isNotEmpty(cur)) {
            if (cur.startsWith(base)) {
                return cur.substring(base.length() + File.separator.length());
            }

            return cur;
        }

        return null;
    }

    /**
     * download file from url
     * 
     * @param urlStr
     *            url
     * @param savePath
     *            the path to save the file
     * @return file
     */
    public static File downLoadFromUrl(String urlStr, String savePath) {
        try {

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            // timeout
            conn.setConnectTimeout(3000);

            // request property
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            String fileName = conn.getHeaderField("content-disposition");

            fileName = fileName.substring(fileName.indexOf("filename=") + 9);

            // get data from the inputStream
            InputStream inputStream = conn.getInputStream();

            byte[] buffer = new byte[1024];
            int len = 0;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

            bos.close();

            byte[] data = bos.toByteArray();

            File saveFile = new File(savePath);

            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }

            File file = new File(savePath + File.separator + fileName);

            if (file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();

            inputStream.close();

            return file;

        } catch (IOException ioe) {
            throw new AnalyzerException(ResultEnums.ERROR, String.format("we can not download file from %s", urlStr));
        }

    }

    /**
     * unzip the zip file
     * 
     * @param zipFile
     *            zip file
     * @param descDir
     *            the path to save the unzip file
     */
    public static String unZipFiles(File zipFile, String descDir) {
        try {
            File pathFile = new File(descDir);

            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }

            ZipFile zip = new ZipFile(zipFile);

            for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry)entries.nextElement();

                String zipEntryName = entry.getName();

                InputStream in = zip.getInputStream(entry);

                String outPath = (descDir + File.separator + zipEntryName).replaceAll("\\*", File.separator);

                File file = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));

                if (!file.exists()) {
                    file.mkdirs();
                }

                if (new File(outPath).isDirectory()) {
                    continue;
                }

                OutputStream out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[1024];
                int len;

                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }

                in.close();
                out.close();
            }

            String filePath = zip.getName();

            return filePath.endsWith(".zip") ? filePath.substring(0, filePath.length() - 4) : filePath;

        } catch (Exception e) {
            throw new AnalyzerException(ResultEnums.ERROR,
                String.format("error: can not unzip the file: %s", zipFile.getName()));
        }

    }

}
