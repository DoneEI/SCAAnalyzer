package core.extraction;

import base.config.UserConfig;
import base.enums.SCAEnums;
import entity.SCA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * filter SCA
 * 
 * @Author: DoneEI
 * @Since: 2021/2/14 6:58 PM
 **/
public class SCAFilter {
    public static Map<String, List<SCA>> filter(List<SCA> scaList) {
        List<SCA> remaining = new ArrayList<>();
        List<SCA> filtered = new ArrayList<>();

        for (SCA sca : scaList) {
            // filter the sca based on the type of file
            String type = sca.getFilePath().substring(sca.getFilePath().lastIndexOf('.') + 1);

            if (UserConfig.SCA_FILTER.contains(type)) {
                sca.setType(SCAEnums.NON_SOURCE_CODE_COMMENT.getCode());
                filtered.add(sca);
            } else {
                sca.setType(SCAEnums.SOURCE_CODE_COMMENT.getCode());
                remaining.add(sca);
            }

        }

        Map<String, List<SCA>> res = new HashMap<>(2);

        res.put(SCAEnums.NON_SOURCE_CODE_COMMENT.getCode(), filtered);
        res.put(SCAEnums.SOURCE_CODE_COMMENT.getCode(), remaining);

        return res;
    }
}
