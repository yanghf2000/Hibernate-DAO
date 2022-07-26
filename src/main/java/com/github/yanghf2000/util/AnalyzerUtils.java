package com.github.yanghf2000.util;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.LinkedHashSet;

public class AnalyzerUtils {

    /**
     * 获取关键词
     * @param title  要分析的词语
     * @return
     * @throws IOException
     */
    private static LinkedHashSet<String> getKeyWordsBySmartCn(String title) throws IOException {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        try(SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
            TokenStream ts = smartChineseAnalyzer.tokenStream("field", title);) {
            CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);

            ts.reset();
            while (ts.incrementToken()) {
                String s = ch.toString();
                // 不为null, 长度大于1，不为数字，如果为字母的话长度不小于3个
                if(s != null && s.length() > 1 && !s.matches("\\d+")
                        && !(s.matches("[a-zA-Z]+") && s.length() < 3)) {
                    set.add(s);
                }
            }
            ts.end();
        }
        return set;
    }

}
