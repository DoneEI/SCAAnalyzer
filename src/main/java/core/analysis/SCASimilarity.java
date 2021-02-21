package core.analysis;

import base.config.UserConfig;
import base.utils.StringUtils;
import entity.SCA;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

import java.util.*;

/**
 * Calculate the similarity between SCAs
 *
 * @Author: DoneEI
 * @Since: 2021/2/15 10:03 PM
 **/
public class SCASimilarity {

    /**
     * For each SCA in the source, find all similar SCAs in the target
     * 
     * @param source
     *            source SCAs
     * @param target
     *            target SCAs
     */
    public static List<String> calculateSimilarity(String sVersion, String tVersion, List<SCA> source,
        List<SCA> target) {
        List<String> s = getSCAContent(source);
        List<String> t = getSCAContent(target);

        Map<SCA, Map<SCA, Double>> res = new HashMap<>(s.size());

        NormalizedStringSimilarity similarity = new NormalizedLevenshtein();

        for (int i = 0; i < s.size(); i++) {
            Map<SCA, Double> tmp = new HashMap<>();

            for (int j = 0; j < t.size(); j++) {
                if (s.get(i).equals(t.get(j))) {
                    continue;
                }

                double score = similarity.similarity(s.get(i), t.get(j));

                if (score > UserConfig.SCA_SIMILARITY_THRESHOLD) {
                    tmp.put(target.get(j), score);
                }
            }

            res.put(source.get(i), tmp);
        }

        List<String> results = new ArrayList<>();

        for (SCA sca : res.keySet()) {
            Map<SCA, Double> tmp = res.get(sca);

            if (tmp.size() == 0) {
                continue;
            }

            results.add(
                "Assumption in " + sVersion + ": " + sca.getFilePath() + "(" + sca.getLine() + ") " + sca.getContent());

            List<Map.Entry<SCA, Double>> list = new ArrayList<>(tmp.entrySet());

            list.sort((o1, o2) -> {
                if (o2.getValue() > o1.getValue()) {
                    return 1;
                } else if (o2.getValue().equals(o1.getValue())) {
                    return 0;
                } else {
                    return -1;
                }
            });

            results.add("----------- Assumption(s) in " + tVersion + "----------------------------------------------");

            for (Map.Entry<SCA, Double> r : list) {
                SCA sim = r.getKey();
                results.add(
                    sim.getFilePath() + "(" + sim.getLine() + ") " + sim.getContent() + "  Score: " + tmp.get(sim));
            }

            results.add("------------------------------------------------------------------------------");
            results.add("");
        }

        return results;
    }

    private static List<String> getSCAContent(List<SCA> scaList) {
        List<String> copy = new ArrayList<>(scaList.size());

        for (SCA s : scaList) {
            copy.add(StringUtils.keepOrdinaryChar(s.getContent()));
        }

        return copy;
    }
}
