package core.download;

import base.utils.FileUtils;

import java.io.File;

/**
 * download the file of tag of framework from github
 *
 * @Author: DoneEI
 * @Since: 2021/2/17 10:38 下午
 **/
public class TagFileDownload {
    /**
     * github url
     */
    private static final String github = "https://codeload.github.com/";

    public static String download(String owner, String framework, String tag, String filePath) {
        String url = github + owner + '/' + framework + "/zip/" + tag;

        File zipFile = FileUtils.downLoadFromUrl(url, filePath);

        // unzip the file
        return FileUtils.unZipFiles(zipFile, filePath);
    }

    public static void main(String[] a) {
        download("BVLC", "caffe", "v0.1", "/Users/fuliming/Downloads/百度云下载");
    }

}
