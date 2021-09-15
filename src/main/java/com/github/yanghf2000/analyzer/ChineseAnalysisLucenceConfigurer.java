package com.github.yanghf2000.analyzer;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

/**
 * 中文分词（lucence）
 */
public class ChineseAnalysisLucenceConfigurer implements LuceneAnalysisConfigurer {

    @Override
    public void configure(LuceneAnalysisConfigurationContext context) {
        context.analyzer(AnalyzerName.CHINESE).instance(new SmartChineseAnalyzer());
    }

}
