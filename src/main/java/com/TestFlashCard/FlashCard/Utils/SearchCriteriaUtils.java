package com.TestFlashCard.FlashCard.Utils;
import com.TestFlashCard.FlashCard.repository.critetia.SearchCriteria;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchCriteriaUtils {

    public static List<SearchCriteria> convert(String[] searchArr) {
        List<SearchCriteria> list = new ArrayList<>();

        if (searchArr == null) return list;

        for (String s : searchArr) {
            if (!StringUtils.hasLength(s)) continue;

            if (s.contains(">=")) {
                String[] p = s.split(">=");
                list.add(new SearchCriteria(p[0], ">=", p[1]));
            } else if (s.contains("<=")) {
                String[] p = s.split("<=");
                list.add(new SearchCriteria(p[0], "<=", p[1]));
            } else if (s.contains(">")) {
                String[] p = s.split(">");
                list.add(new SearchCriteria(p[0], ">", p[1]));
            } else if (s.contains("<")) {
                String[] p = s.split("<");
                list.add(new SearchCriteria(p[0], "<", p[1]));
            } else if (s.contains(":")) {
                String[] p = s.split(":");
                list.add(new SearchCriteria(p[0], ":", p[1]));
            }
        }

        return list;
    }
}
