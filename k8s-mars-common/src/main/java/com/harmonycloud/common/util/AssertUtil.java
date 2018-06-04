package com.harmonycloud.common.util;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import java.util.Collection;
import java.util.Map;

import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_CHINESE;
import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_ENGLISH;

public class AssertUtil {

    public static void notBlank(String value, DictEnum dictEnum) {
        if(StringUtils.isBlank(value)) {
            throwIllegalArgumentException(dictEnum);
        }
    }

    public static void notEmpty(Collection collection, DictEnum dictEnum) {
        if(CollectionUtils.isEmpty(collection)) {
            throwIllegalArgumentException(dictEnum);
        }
    }


    public static void notEmpty(Map map, DictEnum dictEnum) {
        if(CollectionUtils.isEmpty(map)) {
            throwIllegalArgumentException(dictEnum);
        }
    }

    public static void notEmpty(Object[] array, DictEnum dictEnum) {
        if(array == null || array.length == 0) {
            throwIllegalArgumentException(dictEnum);
        }
    }

    public static void notNull(Object obj) {
        if(obj == null) {
            throw new IllegalArgumentException(paramPromptMessage());
        }
    }

    public static void notNull(Object obj, DictEnum dictEnum) {
        if(obj == null) {
            throwIllegalArgumentException(dictEnum);
        }
    }

    public static String blankPromptMessage(){
        String language = DictEnum.getCurrentLanguage();
        switch (language){
            case LANGUAGE_CHINESE:
                return ErrorCodeMessage.NOT_BLANK.getReasonChPhrase();
            case LANGUAGE_ENGLISH:
                return ErrorCodeMessage.NOT_BLANK.getReasonEnPhrase();
            default:
                return "";
        }
    }

    public static String paramPromptMessage(){
        String language = DictEnum.getCurrentLanguage();
        switch (language){
            case LANGUAGE_CHINESE:
                return DictEnum.PARAM.getChPhrase() + ErrorCodeMessage.NOT_BLANK.getReasonChPhrase();
            case LANGUAGE_ENGLISH:
                return DictEnum.PARAM.getEnPhrase() + ErrorCodeMessage.NOT_BLANK.getReasonEnPhrase();
            default:
                return "";
        }
    }

    private static void throwIllegalArgumentException(DictEnum dictEnum){
        throw new IllegalArgumentException("["+ErrorCodeMessage.INVALID_PARAMETER.value()+"] "
                + dictEnum.phrase()  + " " + blankPromptMessage());
    }



}
