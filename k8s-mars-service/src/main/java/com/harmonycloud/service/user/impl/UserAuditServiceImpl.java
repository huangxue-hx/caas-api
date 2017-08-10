package com.harmonycloud.service.user.impl;


import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.ESFactory;
import com.harmonycloud.common.util.UserAuditSearch;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.service.user.UserAuditService;

/**
 * Created by czm on 2017/3/29.
 */
@Service
public class UserAuditServiceImpl implements UserAuditService {

    @Override
    public ActionReturnUtil serachByQuery(UserAuditSearch userAuditSearch, boolean isAdmin) throws Exception {

        String startTime = userAuditSearch.getStartTime();
        String endTime = userAuditSearch.getEndTime();
        String moduleName = userAuditSearch.getModuleName();
        String keyWords = userAuditSearch.getKeyWords();
        String user = userAuditSearch.getUser();
        String scrollId = userAuditSearch.getScrollId();
        List<String> userLists = userAuditSearch.getUserList();
        Integer pageSize = userAuditSearch.getSize();
        Integer pageNum = userAuditSearch.getPageNum();

        BoolQueryBuilder query = QueryBuilders.boolQuery();


        if (StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime)) {
            if(DateUtil.timeFormat.parse(startTime).after(DateUtil.timeFormat.parse(endTime))){
                return ActionReturnUtil.returnErrorWithMsg("开始时间大于结束时间");
            }
            query.must(QueryBuilders.rangeQuery("opTime").from(startTime).to(endTime));
        }

        if (StringUtils.isNotBlank(moduleName)&&!"all".equals(moduleName)) {
            query.must(QueryBuilders.matchQuery("module", moduleName));
        }

        if (StringUtils.isNotBlank(keyWords)) {
        	//判断是不是中文
        	String regex = "[\u4e00-\u9fa5]";
        	Pattern pattern = Pattern.compile(regex);
        	Matcher matcher = pattern.matcher(keyWords);
            if (matcher.find()) {
            	BoolQueryBuilder queryCh = QueryBuilders.boolQuery();
            	queryCh.should(QueryBuilders.matchPhraseQuery("opFun", keyWords));
            	queryCh.should(QueryBuilders.matchPhraseQuery("user", keyWords));
            	queryCh.should(QueryBuilders.matchPhraseQuery("tenant", keyWords));
            	queryCh.should(QueryBuilders.matchPhraseQuery("module", keyWords));
            	queryCh.should(QueryBuilders.matchPhraseQuery("subject", keyWords));
            	query.must(queryCh);
            }else{  
            	query.must(QueryBuilders.queryStringQuery("*"+keyWords+"*").field("user")
            			.field("opFun").field("tenant").field("module").field("path").field("subject").field("remoteIp"));
            }  
            //query.must(QueryBuilders.multiMatchQuery(keyWords,"user","opFun", "tenant", "module", "path", "subject", "remoteIp"));
        }

        if (userLists != null && userLists.size() > 0) {
            query.must(QueryBuilders.termsQuery("user", userLists));
        }

        if (!isAdmin&&user!=null) {
            query.must(QueryBuilders.matchQuery("user", user));
        }


        return ESFactory.searchFromIndex(query, scrollId, pageSize, pageNum);

    }


    /**
     * isAdmin 大于等于1 表示为管理员、否则为普通成员
     */

    public ActionReturnUtil serachByUserName(String username, boolean isAdmin) throws IOException {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        if (!isAdmin) {
            query.must(QueryBuilders.matchQuery("user", username));
        }
//        ESFactory esFactory = new ESFactory();
        return ESFactory.searchFromIndexByUser(query);

    }

    public ActionReturnUtil serachByModule(String username, String module, boolean isAdmin) throws IOException {

        BoolQueryBuilder query = QueryBuilders.boolQuery();

        if (!isAdmin) {
            query.must(QueryBuilders.termQuery("user", username));
        }

        if (module != null && !module.equals("")) {
            query.must(QueryBuilders.termQuery("module", module));
        }

//        ESFactory esFactory = new ESFactory();
        ActionReturnUtil lists = ESFactory.searchFromIndex(query, null,1000,0);

        return lists;
    }

    public ActionReturnUtil serachAuditsByUser(String username, boolean isAdmin) throws IOException {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        if (!isAdmin) {
            query.must(QueryBuilders.termQuery("user", username));
        }

//        ESFactory esFactory = new ESFactory();
        return ESFactory.searchFromIndex(query, null,10000,0);
    }


	@Override
	public ActionReturnUtil getAuditCount(UserAuditSearch userAuditSearch, boolean isAdmin) throws Exception {
		String startTime = userAuditSearch.getStartTime();
        String endTime = userAuditSearch.getEndTime();
        String moduleName = userAuditSearch.getModuleName();
        String keyWords = userAuditSearch.getKeyWords();
        String user = userAuditSearch.getUser();
        List<String> userLists = userAuditSearch.getUserList();

        BoolQueryBuilder query = QueryBuilders.boolQuery();


        if (StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime)) {
            if(DateUtil.timeFormat.parse(startTime).after(DateUtil.timeFormat.parse(endTime))){
                return ActionReturnUtil.returnErrorWithMsg("开始时间大于结束时间");
            }
            query.must(QueryBuilders.rangeQuery("opTime").from(startTime).to(endTime));
        }

        if (StringUtils.isNotBlank(moduleName)&&!"all".equals(moduleName)) {
            query.must(QueryBuilders.matchQuery("module", moduleName));
        }

        if (StringUtils.isNotBlank(keyWords)) {
            //query.must(QueryBuilders.multiMatchQuery(keyWords,"user","opFun", "tenant", "module", "path", "subject", "remoteIp"));
        	query.must(QueryBuilders.queryStringQuery("*"+keyWords+"*").field("user")
        			.field("opFun").field("tenant").field("module").field("path").field("subject").field("remoteIp"));
        }

        if (userLists != null && userLists.size() > 0) {
            query.must(QueryBuilders.termsQuery("user", userLists));
        }

        if (!isAdmin&&user!=null) {
            query.must(QueryBuilders.matchQuery("user", user));
        }
		return ESFactory.getTotalCounts(query);
	}


}
