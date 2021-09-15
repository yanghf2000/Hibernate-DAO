package com.github.yanghf2000.analyzer;

/**
 * 分词器名称
 */
public class AnalyzerName {

    /**
     * 中文
     */
    public static final String CHINESE = "chinese";


    /**
     * ik_max_word<br>
     * ik_max_word 和 ik_smart 什么区别?<br>
     * ik_max_word: 会将文本做最细粒度的拆分，比如会将“中华人民共和国国歌”拆分为“中华人民共和国,中华人民,中华,华人,人民共和国,
     * 人民,人,民,共和国,共和,和,国国,国歌”，会穷尽各种可能的组合，适合 Term Query；<br>
     * ik_smart: 会做最粗粒度的拆分，比如会将“中华人民共和国国歌”拆分为“中华人民共和国,国歌”，适合 Phrase 查询。
     */
    public static final String IK_MAX_WORD = "ik_max_word";

    /**
     * ik_smart<br>
     * ik_max_word 和 ik_smart 什么区别?<br>
     * ik_max_word: 会将文本做最细粒度的拆分，比如会将“中华人民共和国国歌”拆分为“中华人民共和国,中华人民,中华,华人,人民共和国,
     * 人民,人,民,共和国,共和,和,国国,国歌”，会穷尽各种可能的组合，适合 Term Query；<br>
     * ik_smart: 会做最粗粒度的拆分，比如会将“中华人民共和国国歌”拆分为“中华人民共和国,国歌”，适合 Phrase 查询。
     */
    public static final String IK_SMART = "ik_smart";

}
