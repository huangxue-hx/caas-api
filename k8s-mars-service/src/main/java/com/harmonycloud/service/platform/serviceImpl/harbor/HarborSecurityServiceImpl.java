package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HarborUtil;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zsl on 2017/1/22.
 */
@Service
public class HarborSecurityServiceImpl implements HarborSecurityService {
	
	@Autowired
	private HarborUtil harborUtil;

    /**
     * 展示Clair对repo的扫描结果
     *
     * @param flagName 扫描纬度 user or project
     * @param name     username or projectName
     * @return
     */
    @Override
    public ActionReturnUtil clairStatistcs(String flagName, String name) throws Exception {
        if (StringUtils.isEmpty(flagName)) {
            return ActionReturnUtil.returnErrorWithMsg("flag name cannot be null");
        }
        if (StringUtils.isEmpty(name)) {
            return ActionReturnUtil.returnErrorWithMsg("name cannot be null");
        }

        String url = HarborClient.getPrefix() + "/api/repositories/getClairStatistcs";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        Map<String, Object> params = new HashMap<>();
        params.put("flag_name", flagName);
        params.put("name", name);

        return HttpClientUtil.httpGetRequest(url, headers, params);
    }

    /**
     * 扫描总结
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil vulnerabilitySummary(String repoName, String tag) throws Exception {
        if (StringUtils.isEmpty(repoName)) {
            return ActionReturnUtil.returnErrorWithMsg("repository name cannot be null");
        }
        if (StringUtils.isEmpty(tag)) {
            return ActionReturnUtil.returnErrorWithMsg("tag cannot be null");
        }
        repoName = repoName + ":" + tag;

        String url = HarborClient.getPrefix() + "/api/repositories/getVulnerabilitySummary";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repoName);

        return HttpClientUtil.httpGetRequest(url, headers, params);
    }

    /**
     * 扫描总结 -package纬度
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil vulnerabilitiesByPackage(String repoName, String tag) throws Exception {
        if (StringUtils.isEmpty(repoName)) {
            return ActionReturnUtil.returnErrorWithMsg("repository name cannot be null");
        }
        if (StringUtils.isEmpty(tag)) {
            return ActionReturnUtil.returnErrorWithMsg("tag cannot be null");
        }
        repoName = repoName + ":" + tag;

        String url = HarborClient.getPrefix() + "/api/repositories/getVulnerabilitiesByPackage";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repoName);

        return HttpClientUtil.httpGetRequest(url, headers, params);
    }

	@Override
	public ActionReturnUtil refreshImageRepo() throws Exception {
		String url = HarborClient.getPrefix() + "/api/internal/syncregistry";
		Map<String, Object> headers = new HashMap<>();
	    headers.put("cookie", harborUtil.checkCookieTimeout());
	    return HttpClientUtil.httpPostRequestForHarbor(url, headers, null);
	}

}
