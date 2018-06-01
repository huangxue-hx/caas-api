package com.harmonycloud.service.system.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dto.config.ControllerUrlMapping;
import com.harmonycloud.dto.config.MethodUrlMapping;
import com.harmonycloud.service.system.ApiService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;


/**
 * Created by zhangkui
 */
@Service
public class ApiServiceImpl implements ApiService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{propertiesReader['sourcecode.dir']}")
    private String sourceCodeDir;

    @Override
    public Map<String, ControllerUrlMapping> generateUrlMapping(String order, HttpServletRequest request) {
        //获取平台controller类以及方法的注释信息
        Map<String,String> methodDescs = new HashMap<>();
        Map<String,String> controllerDescs = new HashMap<>();
        String apiControllerDir = sourceCodeDir;
        if(StringUtils.isNotBlank(apiControllerDir)) {
            if (sourceCodeDir.endsWith("/") || sourceCodeDir.endsWith("\\")) {
                apiControllerDir += "k8s-mars-api/src/main/java";
            } else {
                apiControllerDir += "/k8s-mars-api/src/main/java";
            }
            File file = new File(apiControllerDir);
            try {
                getMethodDesc(file, methodDescs, controllerDescs);
            }catch (Exception e){
                logger.error("获取接口的注释信息失败",e);
            }
        }
        //获取spring收集的所有controller的url mapping
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
        RequestMappingHandlerMapping rmhp = wc.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = rmhp.getHandlerMethods();
        Map<String, ControllerUrlMapping> urlMappings = new HashMap<>();
        for (Iterator<RequestMappingInfo> iterator = map.keySet().iterator(); iterator.hasNext();) {
            RequestMappingInfo info = iterator.next();
            HandlerMethod method = map.get(info);
            String controllerName = method.getBean().toString();
            String firstLetter = controllerName.substring(0,1).toUpperCase();
            controllerName = firstLetter + controllerName.substring(1);
            ControllerUrlMapping controllerUrlMapping = urlMappings.get(controllerName);
            if(controllerUrlMapping == null){
                controllerUrlMapping = new ControllerUrlMapping();
                controllerUrlMapping.setControllerName(controllerName);
                controllerUrlMapping.setControllerDesc(controllerDescs.get(controllerName));
                List<MethodUrlMapping> methodUrlMappings = new ArrayList();
                controllerUrlMapping.setMethodUrlMappings(methodUrlMappings);
                urlMappings.put(controllerName, controllerUrlMapping);
            }
            MethodUrlMapping urlMapping = new MethodUrlMapping();
            urlMapping.setControllerName(controllerName);
            urlMapping.setMethodName(method.getMethod().getName());
            urlMapping.setMethodDesc(methodDescs.get(controllerName+ CommonConstant.DOT + urlMapping.getMethodName()));
            urlMapping.setRestUrl(info.getPatternsCondition().toString().replace("[","").replace("]",""));
            urlMapping.setHttpMethod(info.getMethodsCondition().toString().replace("[","").replace("]",""));

            MethodParameter[] params = method.getMethodParameters();
            StringBuffer buffer = new StringBuffer();
            for(int i=0;i<params.length;i++) {
                if(i == params.length -1){
                    buffer.append(params[i].getParameterType().getSimpleName());
                }else {
                    buffer.append(params[i].getParameterType().getSimpleName() + CommonConstant.COMMA);
                }
            }
            urlMapping.setParams(buffer.toString());
            controllerUrlMapping.getMethodUrlMappings().add(urlMapping);
        }
        return urlMappings;
    }

    /**
     * 获取方法或controller类的注释信息
     * @param file controller类文件
     * @param methodDescs 方法注释信息
     * @param controllerDescs controller注释信息
     * @throws Exception
     */
    private void getMethodDesc(File file, Map<String,String> methodDescs, Map<String,String> controllerDescs) throws Exception{
        if(!file.exists()){
            return;
        }
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f : files){
                getMethodDesc(f,methodDescs,controllerDescs);
            }
        }else{
            if(file.getName().endsWith(".java")){
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
                String controllerName = file.getName().replace(".java","");
                String line = "";
                boolean descStart = false;
                boolean classStart = false;
                String methodDesc = "";
                String controllerDesc = "";
                while((line = reader.readLine())!=null){
                    line = line.trim();
                    //注释开始
                    if(line.startsWith("/**")){
                        descStart = true;
                    }
                    //类定义开始
                    if(line.startsWith("public class")){
                        controllerDescs.put(controllerName,controllerDesc.replaceAll("\\*"," ")
                                .replaceAll("/"," ").trim());
                        controllerDesc = "";
                        classStart = true;
                        continue;
                    }
                    if (descStart) {
                        //注释开始，将注释拼接到一个字符串，只收集方法或类的描述信息，不包含@param,@return注释信息，等描述信息规范，完整之后再收集
                        if(!line.replaceAll(" ","").contains("*@")
                                && (line.startsWith("*") || line.startsWith("/*"))) {
                            if (classStart) {
                                methodDesc += line.replaceFirst("/*", "");
                            } else {
                                controllerDesc += line.replaceFirst("/*", "");
                            }
                        }else{
                            descStart = false;
                        }
                    }
                    //注释结束
                    if(line.endsWith("*/")){
                        descStart = false;
                    }
                    //方法定义，获取方法名，将方法名对应的描述放入map
                    if(classStart && line.startsWith("public")){
                        if(line.indexOf("(") ==-1){
                            throw new MarsRuntimeException(ErrorCodeMessage.METHOD_FORMAT_ERROR, line ,false);
                        }
                        String method = line.substring(0, line.indexOf("("));
                        method = method.substring(method.lastIndexOf(" ")+1);
                        methodDescs.put(controllerName + CommonConstant.DOT + method, methodDesc.replaceAll("\\*"," ")
                                .replaceAll("/"," ").trim());
                        methodDesc = "";
                        descStart = false;
                    }else if(classStart && line.startsWith("//") && line.contains("public") && line.contains("ActionReturnUtil")){
                        //方法体已经被注释，忽略该方法
                        methodDesc = "";
                        descStart = false;
                    }
                }
            }
        }
    }

}