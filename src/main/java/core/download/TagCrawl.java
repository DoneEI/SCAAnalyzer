package core.download;

import base.enums.ResultEnums;
import base.exception.AnalyzerException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * get all Tags of framework from github
 *
 * @Author: DoneEI
 * @Since: 2021/2/14 1,34 PM
 **/
public class TagCrawl {
    /**
     * github url
     */
    private static final String github = "https://github.com/";

    public static List<String> getTags(String owner, String framework) throws Exception {
        List<String> tagList = new ArrayList<>();

        String urlAppend = "";

        while (true) {
            String url = github + owner + '/' + framework + "/tags";

            Document doc = Jsoup.connect(url + urlAppend).timeout(3000).get();

            Elements tags = doc.getElementsByClass("Box-row position-relative d-flex");

            if (tags.size() == 0) {
                break;
            }

            for (int i = 0; i < tags.size(); i++) {
                Elements res = tags.get(i).getElementsByTag("a");

                if (res != null) {
                    String tag = res.get(0).text().replace('/', '-');
                    tagList.add(tag);

                    if (i == tags.size() - 1) {
                        urlAppend = "?after=" + tag;
                    }
                }
            }
        }

        return tagList;
    }

}
