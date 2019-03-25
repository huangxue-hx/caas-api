package com.harmonycloud.service.user;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 用户审计日志和apiserver审计日志父类
 * @author liangli
 */
public class BaseAuditService {

    protected static TransportClient platformEsClient;

    protected SearchRequestBuilder multiIndexSearch(TransportClient esClient, List<String> indexList) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = Class.forName("org.elasticsearch.action.search.SearchRequestBuilder");
        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch();
        List<Object> objectList = new ArrayList<>();
        for (String s : indexList) {
            objectList.add(s);
        }
        String[] strArray = objectList.toArray(new String[objectList.size()]);
        Method method = clazz.getMethod("setIndices", new Class[]{String[].class});
        searchRequestBuilder = (SearchRequestBuilder)method.invoke(searchRequestBuilder, new Object[]{strArray});
        return searchRequestBuilder;
    }
}
