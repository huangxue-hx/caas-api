package com.harmonycloud.filters;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Created by lucia on 2018/6/27.
 */
public class UrlWhiteListHandler {

    private static List<Pattern> urlPattern;

    public static List<Pattern> initUrlPattern(String whiteListUrl){
        if(StringUtils.isBlank(whiteListUrl)){
            return Collections.emptyList();
        }
        String[] urls = whiteListUrl.split(",");
        int length = urls.length;
        urlPattern = new ArrayList<>();
        for(int i = 0; i < length; ++i) {
            String url = urls[i];
            String regex = url.replace("*", "(.*)").replace("?", "(.{1})");
            Pattern pattern = Pattern.compile(regex, CASE_INSENSITIVE);
            if (pattern != null) {
                urlPattern.add(pattern);
            }
        }
        return urlPattern;
    }

    public static boolean isWhiteUrl(String uri) {
        return isWhiteUrl(urlPattern, uri);
    }

    public static boolean isWhiteUrl(List<Pattern> urls, String uri) {
        String reqUri = uri;
        if (uri.contains(";")) {
            String[] split = uri.split(";");
            reqUri = split[0];
        }
        Iterator iterator = null;
        if(!CollectionUtils.isEmpty(urls)){
            iterator = urls.iterator();
        }else if(!CollectionUtils.isEmpty(urlPattern)){
            iterator = urlPattern.iterator();
        }else{
            return false;
        }
        Matcher matcher;
        do {
            if (!iterator.hasNext()) {
                return false;
            }
            Pattern exclusion = (Pattern)iterator.next();
            matcher = exclusion.matcher(reqUri);
        } while(!matcher.matches());

        return true;
    }

}
