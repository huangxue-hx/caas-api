package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.HarborSecurityFlagNameEnum;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.TenantHarborDetail;
import com.harmonycloud.service.platform.bean.harbor.HarborManifest;
import com.harmonycloud.service.platform.bean.harbor.HarborSecurityClairStatistcs;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;

import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.harmonycloud.common.Constant.CommonConstant.SLASH;

/**
 * Created by zsl on 2017/1/22.
 */
@Service
public class HarborSecurityServiceImpl implements HarborSecurityService {

    @Autowired
    ClusterService clusterService;
    @Autowired
    NamespaceLocalService namespaceLocalService;
    @Autowired
    RoleBindingService roleBindingService;
    @Autowired
    HarborUserService harborUserService;
    @Autowired
    HarborService harborService;
    @Autowired
    HarborProjectService harborProjectService;

    /**
     * 展示Clair对repo的扫描结果
     *
     * @param flagName 扫描纬度 user or project
     * @param name     username or projectName
     * @return
     */
    @Override
    public ActionReturnUtil clairStatistcs(String harborHost, String flagName, String name) throws Exception {
        AssertUtil.notBlank(flagName, DictEnum.FLAG_TYPE);
        AssertUtil.notBlank(name, DictEnum.NAME);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/getClairStatistcs";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("flag_name", flagName);
        params.put("name", name);

        return HttpsClientUtil.httpGetRequest(url, headers, params);
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
    public ActionReturnUtil vulnerabilitySummary(String harborHost, String repoName, String tag) throws Exception {
        AssertUtil.notBlank(repoName, DictEnum.IMAGE_NAME);
        AssertUtil.notBlank(tag, DictEnum.IMAGE_TAG);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        repoName = repoName + ":" + tag;

        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/getVulnerabilitySummary";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repoName);

        return HttpsClientUtil.httpGetRequest(url, headers, params);
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
    public ActionReturnUtil vulnerabilitiesByPackage(String harborHost, String repoName, String tag) throws Exception {
        AssertUtil.notBlank(repoName, DictEnum.IMAGE_NAME);
        AssertUtil.notBlank(repoName, DictEnum.IMAGE_TAG);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        repoName = repoName + ":" + tag;

        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/getVulnerabilitiesByPackage";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repoName);

        return HttpsClientUtil.httpGetRequest(url, headers, params);
    }

    /**
     * 在project纬度展示Clair对repo的扫描结果
     *
     * @param projectName project name
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil clairStatistcsOfProject(String harborHost, String projectName) throws Exception {

        ActionReturnUtil actionReturnUtil = this.clairStatistcs(harborHost,
                HarborSecurityFlagNameEnum.PROJECT.getFlagName(), projectName);

        if (actionReturnUtil.isSuccess()) {
            if (actionReturnUtil.get("data") != null) {
                actionReturnUtil.put("data", getHarborSecurityClairStatistcsResp(actionReturnUtil.get("data").toString()));
            }
        }

        return actionReturnUtil;
    }

    @Override
    public ActionReturnUtil getRepositoryClairStatistcs(Integer repositoryId) throws Exception {
        ImageRepository imageRepository = harborProjectService.findRepositoryById(repositoryId);
        return this.clairStatistcsOfProject(imageRepository.getHarborHost(), imageRepository.getHarborProjectName());
    }

    @Override
    public ActionReturnUtil manifestsOfTag(Integer repositoryId, String imageName, String tag) throws Exception {
        ImageRepository imageRepository = harborProjectService.findRepositoryById(repositoryId);
        return this.manifestsOfTag(imageRepository.getHarborHost(), imageName,tag);
    }

    /**
     * tag详情
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     */
    @Override
    public ActionReturnUtil manifestsOfTag(String harborHost, String repoName, String tag) throws Exception {
        ActionReturnUtil manifestsResponse = harborService.getManifests(harborHost, repoName, tag);

        if (manifestsResponse.isSuccess()) {
            if (manifestsResponse.get("data") != null) {
                return ActionReturnUtil.returnSuccessWithData(getHarborManifestResp(harborHost,
                        manifestsResponse.get("data").toString(), repoName, tag));
            }
        }
        return ActionReturnUtil.returnError();
    }

    /**
     * 在user纬度展示Clair对repo的扫描结果
     *
     * @param username username
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil clairStatistcsOfUser(String harborHost, String username) throws Exception {

        ActionReturnUtil actionReturnUtil = this.clairStatistcs(harborHost, HarborSecurityFlagNameEnum.USER.getFlagName(), username);

        if (actionReturnUtil.isSuccess()) {
            if (actionReturnUtil.get("data") != null) {
                actionReturnUtil.put("data", getHarborSecurityClairStatistcsResp(actionReturnUtil.get("data").toString()));
            }
        }

        return actionReturnUtil;
    }

    /**
     * 得到harbor security clair statistcs response
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private HarborSecurityClairStatistcs getHarborSecurityClairStatistcsResp(String dataJson) throws Exception{
        HarborSecurityClairStatistcs harborSecurityClairStatistcs = new HarborSecurityClairStatistcs();
        if (StringUtils.isNotEmpty(dataJson)) {
            Map<String, Object> map = JsonUtil.jsonToMap(dataJson);
            if (map != null) {
                if (map.get("image_num") != null) {
                    harborSecurityClairStatistcs.setImage_num(Integer.parseInt(map.get("image_num").toString()));
                }
                if (map.get("unsecurity_image_num") != null) {
                    harborSecurityClairStatistcs.setUnsecurity_image_num(Integer.parseInt(map.get("unsecurity_image_num").toString()));
                }
                if (map.get("clair_not_Support") != null) {
                    harborSecurityClairStatistcs.setClair_not_Support(Integer.parseInt(map.get("clair_not_Support").toString()));
                }
                if (map.get("clair_success") != null) {
                    harborSecurityClairStatistcs.setClair_success(Integer.parseInt(map.get("clair_success").toString()));
                }
                if (map.get("abnormal") != null) {
                    harborSecurityClairStatistcs.setAbnormal(Integer.parseInt(map.get("abnormal").toString()));
                }
                if(map.get("mild")!=null){
                    harborSecurityClairStatistcs.setMild(Integer.parseInt(map.get("mild").toString()));
                }
            }
        }
        return harborSecurityClairStatistcs;
    }

    /**
     * 得到harbor manifest response
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private HarborManifest getHarborManifestResp(String harborHost, String dataJson, String repoName, String tag) throws Exception {
        HarborManifest harborManifest = null;
        if (StringUtils.isNotEmpty(dataJson)) {
            ActionReturnUtil vulnerabilitySummaryResponse = this.vulnerabilitySummary(harborHost, repoName, tag);
            ActionReturnUtil vulnerabilitiesByPackageResponse = this.vulnerabilitiesByPackage(harborHost, repoName, tag);
            harborManifest = new HarborManifest();
            harborManifest.setTag(tag);
            Map<String, Object> manifestMap = JsonUtil.convertJsonToMap(dataJson);
            if (manifestMap.get("Author") != null) {
                harborManifest.setAuthor(manifestMap.get("Author").toString());
            }
            if (manifestMap.get("config") != null) {
                String manifestTime = manifestMap.get("config").toString();
                Integer start =manifestTime.indexOf("created");
                harborManifest.setCreateTime(manifestTime.substring(start+10,start+29).replace("T", " "));
                //个别特殊情况 created 后有空格，根据位置截取截取的时间不对，需要先去除空格
                if(!harborManifest.getCreateTime().startsWith("2")){
                    manifestTime = manifestTime.replaceAll(" ","");
                    start = manifestTime.indexOf("created");
                    harborManifest.setCreateTime(manifestTime.substring(start+10,start+29).replace("T", " "));
                }
            }
            if (vulnerabilitySummaryResponse.isSuccess()) {
                if (vulnerabilitySummaryResponse.get("data") != null) {
                    harborManifest.setVulnerabilitySummary(JsonUtil.convertJsonToMap(vulnerabilitySummaryResponse.get("data").toString()));
                }
            }
            if (vulnerabilitiesByPackageResponse.isSuccess()) {
                if (vulnerabilitySummaryResponse.get("data") != null) {
                    harborManifest.setVulnerabilitiesByPackage(JsonUtil.convertJsonToMap(vulnerabilitiesByPackageResponse.get("data").toString()));
                }
            }
        }
        return harborManifest;
    }

	@Override
	public ActionReturnUtil refreshImageRepo(String harborHost) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
		String url = HarborClient.getHarborUrl(harborServer) + "/api/internal/syncregistry";
		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
	    return HttpsClientUtil.httpPostRequestForHarbor(url, headers, null);
	}

}
