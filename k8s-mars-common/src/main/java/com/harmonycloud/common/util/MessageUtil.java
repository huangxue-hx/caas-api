package com.harmonycloud.common.util;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static com.harmonycloud.common.Constant.CommonConstant.DEFAULT_LANGUAGE_CHINESE;
import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_CHINESE;
import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_ENGLISH;

@Component
public class MessageUtil {
    private static Map<String,String> messageMap = new HashMap<>();
    MessageUtil(){
        messageMap.put("代码检出/编译","Coding Checkout/Building");
        messageMap.put("单元测试","Unit Testing");
        messageMap.put("镜像构建","Image Building");
        messageMap.put("静态扫描","Static Scanning");
        messageMap.put("自定义","Custom Step");
        messageMap.put("应用部署","Deploying Applications");
        messageMap.put("集成测试","Integration Testing");
    }
    //根据系统语言获取信息
    public static String getMessage(ErrorCodeMessage errorCodeMessage){
        return ErrorCodeMessage.getMessageWithLanguage(errorCodeMessage,"",false);
    }
    public static String getMessage(String message){
        String language = DEFAULT_LANGUAGE_CHINESE;
        if(RequestContextHolder.getRequestAttributes() != null
                && ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest() != null){
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String sessionLanguage = String.valueOf( request.getSession().getAttribute("language"));
            if(StringUtils.isNotBlank(sessionLanguage) && !"null".equals(sessionLanguage)){
                language = sessionLanguage;
            }
        }
        switch (language){
            case LANGUAGE_CHINESE:
                return message;
            case LANGUAGE_ENGLISH:
                return messageMap.get(message);
            default:
                return message;
        }
    }
}
