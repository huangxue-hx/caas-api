package com.harmonycloud.common.enumm;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * es查询方式
 */
public enum EsSearchTypeEnum {

    MATCH("match","分词搜索"),
    MATCH_PHRASE("matchPhrase","精确搜索"),
    WILDCARD("wildcard","模糊搜索"),
    REGEXP("regexp","正则表达式搜索");

    private String code;
    private String name;

    /**
     * 存放所有的code和Enmu的转换.
     */
    private static final Map<String, EsSearchTypeEnum> ES_SEARCH_TYPE_MAP = new ConcurrentHashMap<>(
            EsSearchTypeEnum.values().length);


    static {
        /**
         * 将所有的实体类放入到map中,提供查询.
         */
        for (EsSearchTypeEnum type : EnumSet.allOf(EsSearchTypeEnum.class)) {
            ES_SEARCH_TYPE_MAP.put(type.getCode(), type);
        }
    }

    EsSearchTypeEnum(String code, String name) {
        this.setCode(code);
        this.setName(name);
    }

    public static Map<String, EsSearchTypeEnum> getEsSearchTypeMap(){
        return ES_SEARCH_TYPE_MAP;
    }

    public static EsSearchTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        return ES_SEARCH_TYPE_MAP.get(code);
    }

    public String getCode() {
        return code;
    }

    private void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }
}
