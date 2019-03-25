package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.HarborSecurityFlagNameEnum;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.harbor.ImageRepositoryMapper;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.HarborHttpsClientUtil;
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;

import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.squareup.moshi.Json;
import freemarker.ext.beans.HashAdapter;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by zsl on 2017/1/22.
 */
@Service
public class HarborSecurityServiceImpl implements HarborSecurityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarborSecurityServiceImpl.class);
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private RoleBindingService roleBindingService;
    @Autowired
    private HarborUserService harborUserService;
    @Autowired
    private HarborService harborService;
    @Autowired
    private HarborProjectService harborProjectService;
    @Autowired
    private ImageRepositoryMapper imageRepositoryMapper;

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

        return HarborHttpsClientUtil.httpGetRequest(url, headers, params);
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

        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/"+repoName+"/tags/"+tag+"/vulnerability/details";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpGetRequest(url, headers, null);
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
        String repo = repoName + ":" + tag;

        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/getVulnerabilitiesByPackage";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repo);

        return HarborHttpsClientUtil.httpGetRequest(url, headers, params);
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

//        ActionReturnUtil actionReturnUtil = this.clairStatistcs(harborHost,
//                HarborSecurityFlagNameEnum.PROJECT.getFlagName(), projectName);

        String projectId = getIdByRepoName(harborHost,projectName);
        List<ImageRepository> imageRepositoryList = imageRepositoryMapper.selectRepository(projectId,harborHost);
        Integer id = imageRepositoryList.get(0).getId();
        //获取project下的镜像列表
        List<HarborRepositoryMessage> harborRepositoryMessageList = (List<HarborRepositoryMessage>)harborProjectService.listImages(id,null,null).getData();
        int imageNum = harborRepositoryMessageList.size();
        int unsecurityNum = 0,notSupportNum = 0,successNum = 0,abnormalNum = 0,mildNum = 0;
        List<String> severityGrades = new ArrayList<String>(Arrays.asList("1","2","3","4","5"));//漏洞等级数量
        //对每个镜像进行扫描
        for(HarborRepositoryMessage harborRepositoryMessage : harborRepositoryMessageList){
            List<String> tags = harborRepositoryMessage.getTags();
            String repoName = harborRepositoryMessage.getRepository();
            for(String tag : tags){
                ActionReturnUtil vulnerabilitySummaryResponse = this.vulnerabilitySummary(harborHost, repoName, tag);//扫描每个镜像漏洞情况
                String result = vulnerabilitySummaryResponse.getData().toString();
                result = result.replace("package","packageName");
                List<VulnerabilityDetail> vulnerabilityDetailList = JsonUtil.jsonToList(result,VulnerabilityDetail.class);
                if(vulnerabilityDetailList.size() == 0 && vulnerabilityDetailList.isEmpty()){
                    successNum++;//漏洞列表为空则为安全镜像
                    break;
                }else {
                    //遍历单个镜像的漏洞列表
                    for(VulnerabilityDetail vulnerabilityDetail : vulnerabilityDetailList){
                        if(!severityGrades.contains(vulnerabilityDetail.getSeverity())){//若漏洞等级存在1~5之外的数字，为不支持镜像
                            notSupportNum++;
                        }else {
                            if(vulnerabilityDetail.getSeverity().equals("4") || vulnerabilityDetail.getSeverity().equals("5")){
                                unsecurityNum++;
                            }else {
                                mildNum++;//漏洞等级为1、2、3，设置为中低危镜像
                            }
                        }
                        break;
                    }
                }
            }
        }

        ActionReturnUtil actionReturnUMap = new ActionReturnUtil();
        Map<String,Object> data = new HashMap<String,Object>();

        data.put("image_num",imageNum);//镜像总数
        data.put("unsecurity_image_num",unsecurityNum);//不安全镜像
        data.put("clair_not_Support",notSupportNum);//不支持镜像
        data.put("clair_success",successNum);//安全镜像
        data.put("abnormal",abnormalNum);//异常镜像
        data.put("mild",mildNum);//中低危镜像

        actionReturnUMap.put("data", data);
        if(imageRepositoryList != null){
            actionReturnUMap.put("success",true);
        }
        return actionReturnUMap;
    }

    @Override
    public ActionReturnUtil getRepositoryClairStatistcs(Integer repositoryId) throws Exception {
        ImageRepository imageRepository = harborProjectService.findRepositoryById(repositoryId);
        return this.clairStatistcsOfProject(imageRepository.getHarborHost(), imageRepository.getHarborProjectName());
    }

    @Override
    public ActionReturnUtil manifestsOfTag(Integer repositoryId, String imageName, String tag) throws Exception {
        ImageRepository imageRepository = harborProjectService.findRepositoryById(repositoryId);
        return this.getManifestsDetail(imageRepository.getHarborHost(), imageName,tag);
    }

    /**
     * 获取tag manifest信息，漏洞数量以及各个包的漏洞信息
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     */
    @Override
    public ActionReturnUtil getManifestsDetail(String harborHost, String repoName, String tag) throws Exception {
        ActionReturnUtil manifestsResponse = harborService.getManifests(harborHost, repoName, tag);

        if (manifestsResponse.isSuccess()) {
            if (manifestsResponse.get("data") != null) {
                return ActionReturnUtil.returnSuccessWithData(getHarborManifestResp(harborHost,
                        manifestsResponse.get("data").toString(), repoName, tag,true));
            }
        }
        return manifestsResponse;
    }

    /**
     * 获取tag manifest信息以及漏洞数量
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
                        manifestsResponse.get("data").toString(), repoName, tag,false));
            }
        }
        return manifestsResponse;
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
                actionReturnUtil.put("data", (actionReturnUtil.get("data").toString()));
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
    private HarborSecurityClairStatistcs getHarborSecurityClairStgetHarborSecurityClairStatistcsRespatistcsResp(String dataJson) throws Exception{
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
    private HarborManifest getHarborManifestResp(String harborHost, String dataJson, String repoName, String tag, boolean withPackage) throws Exception {
        HarborManifest harborManifest = null;
        if (StringUtils.isNotEmpty(dataJson)) {
            ActionReturnUtil vulnerabilitySummaryResponse = this.vulnerabilitySummary(harborHost, repoName, tag);
            harborManifest = new HarborManifest();
            harborManifest.setTag(tag);
            Map<String, Object> manifestMap = JsonUtil.convertJsonToMap(dataJson);
            if (manifestMap.get("Author") != null) {
                harborManifest.setAuthor(manifestMap.get("Author").toString());
            }
            if (manifestMap.get("manifest") != null && ((Map)manifestMap.get("manifest")).get("config") != null) {
                String digest = ((Map)((Map)manifestMap.get("manifest")).get("config")).get("digest").toString();
                harborManifest.setDigest(digest);
            }else{
                //获取不到digest，用tag代替，digest在镜像清理的使用判断是否同一镜像的时候使用
                harborManifest.setDigest(tag);
            }
            if (manifestMap.get("config") != null) {
                String manifestTime = manifestMap.get("config").toString();
                Integer start =manifestTime.indexOf("created");
                if(start >=0 ) {
                    harborManifest.setCreateTime(manifestTime.substring(start + 10, start + 29).replace("T", " "));
                    //个别特殊情况 created 后有空格，根据位置截取截取的时间不对，需要先去除空格
                    if (!harborManifest.getCreateTime().startsWith("2")) {
                        manifestTime = manifestTime.replaceAll(" ", "");
                        start = manifestTime.indexOf("created");
                        harborManifest.setCreateTime(manifestTime.substring(start + 10, start + 29).replace("T", " "));
                    }
                }
            }
            Integer highNum = 0,patchesSum = 0;
            Integer criticalSum=0,highSum=0,mediumSum=0,lowSum=0,negligibleSum=0,unknownSum=0;
            if (!vulnerabilitySummaryResponse.isSuccess()) {
                LOGGER.error("查询镜像漏洞信息失败， response:{}",
                        com.alibaba.fastjson.JSONObject.toJSONString(vulnerabilitySummaryResponse));
                return harborManifest;
            }
            if (vulnerabilitySummaryResponse.get("data") != null) {
                String result = vulnerabilitySummaryResponse.get("data").toString().replaceAll("package", "packageName");
                Map<String, Object> resultMap = new HashMap<String,Object>();
                List<VulnerabilityDetail> vulnerabilityDetailList = JsonUtil.jsonToList(result,VulnerabilityDetail.class);
                Map<String,Object> vulnerabilitySuminfo = new HashMap<String,Object>();
                for(VulnerabilityDetail vulnerabilityDetail : vulnerabilityDetailList){
                    if(vulnerabilityDetail.getFixedVersion() != null){
                        patchesSum++;
                    }
                    switch (Integer.parseInt(vulnerabilityDetail.getSeverity())){
                        case 1:
                            negligibleSum++;break;
                        case 2:
                            lowSum++;break;
                        case 3:
                            mediumSum++;break;
                        case 4:
                            highNum++;break;
                        case 5:
                            criticalSum++;break;
                        default:
                            unknownSum++;break;
                    }
                }
                if (vulnerabilitySummaryResponse.isSuccess() ) {
                    vulnerabilitySuminfo.put("vulnerability-sum",vulnerabilityDetailList.size());
                    vulnerabilitySuminfo.put("vulnerability-patches-sum",patchesSum);
                    vulnerabilitySuminfo.put("critical-level-sum",criticalSum);
                    vulnerabilitySuminfo.put("high-level-sum",highNum);
                    vulnerabilitySuminfo.put("medium-level-sum",mediumSum);
                    vulnerabilitySuminfo.put("low-level-sum",lowSum);
                    vulnerabilitySuminfo.put("negligible-level",negligibleSum);
                    vulnerabilitySuminfo.put("unknown-level",unknownSum);
//                    resultMap.put("vulnerability-suminfo", vulnerabilitySuminfo);
                    if(vulnerabilityDetailList.size() == unknownSum && unknownSum != 0){
                        resultMap.put("notsupport",true);
                    }else {
                        Map<String,Object> tempMap = new HashMap<String,Object>();
                        tempMap.put("vulnerability-suminfo",vulnerabilitySuminfo);
                        tempMap.put("vulnerability-list", vulnerabilityDetailList);
                        resultMap.put("vulnerability",tempMap);
                    }

                    harborManifest.setVulnerabilitySummary(resultMap);
                }
            }
            //需要获取tag详情的时候查询VulnerabilitiesByPackage
            if(withPackage) {
                String result = vulnerabilitySummaryResponse.get("data").toString();
                result = result.replace("package","packageName");
                Map<String, Object> resultMap = new HashMap<String,Object>();
                Integer packageKindSum = 0;
                List<VulnerabilityDetail> vulnerabilityDetailList = JsonUtil.jsonToList(result,VulnerabilityDetail.class);
                Set<String> packageList = new HashSet<String>();
                for(VulnerabilityDetail vulnerabilityDetail : vulnerabilityDetailList){
                    packageList.add(vulnerabilityDetail.getPackageName());//package种类名
                }
                packageKindSum = packageList.size();
                Map<String,Object> packagesSummary = new HashMap<String,Object>();
                Map<String,Object> vulnerabilityMap = new HashMap<String,Object>();
                vulnerabilityMap.put("critical_level",criticalSum);
                vulnerabilityMap.put("high_level",highNum + criticalSum);
                vulnerabilityMap.put("medium_level",mediumSum);
                vulnerabilityMap.put("low_level",lowSum);
                vulnerabilityMap.put("negligible_level",negligibleSum);
                vulnerabilityMap.put("unknown_level",unknownSum);

                packagesSummary.put("sum",packageKindSum);
                packagesSummary.put("level_summary",vulnerabilityMap);
                resultMap.put("packages_summary",packagesSummary);

                List<ImagePackage> imagePackages = new LinkedList<ImagePackage>();

                for(String packageName : packageList){
                    Integer tempCritical = 0,tempHigh = 0,tempMedium = 0,tempLow = 0,tempNeglible = 0,tempUnknown = 0;
                    String version = null;
                    String fixedVersion = null;
                    ImagePackage imagePackage = new ImagePackage();
                    Map<String,Object> vulnerabilitles = new HashMap<String,Object>();
                    for(VulnerabilityDetail vulnerabilityDetail : vulnerabilityDetailList){
                        if(vulnerabilityDetail.getPackageName().equals(packageName)){
                            if(version == null){
                                version = vulnerabilityDetail.getVersion();
                            }
                            if(fixedVersion == null){
                                fixedVersion = vulnerabilityDetail.getVersion();
                            }
                            if(vulnerabilityDetail.getSeverity().equals("5")){
                                tempCritical++;
                            }else if(vulnerabilityDetail.getSeverity().equals("4")){
                                tempHigh++;
                            }else if(vulnerabilityDetail.getSeverity().equals("3")){
                                tempMedium++;
                            }else if(vulnerabilityDetail.getSeverity().equals("2")){
                                tempLow++;
                            }else if(vulnerabilityDetail.getSeverity().equals("1")){
                                tempNeglible++;
                            }else {
                                tempUnknown++;
                            }
                        }
                    }
                    vulnerabilitles.put("critical_level",tempCritical);
                    vulnerabilitles.put("high_level",tempHigh);
                    vulnerabilitles.put("medium_level",tempMedium);
                    vulnerabilitles.put("low_level",tempLow);
                    vulnerabilitles.put("negligible_level",tempNeglible);
                    vulnerabilitles.put("unknown_level",tempUnknown);
                    imagePackage.setPackageName(packageName);
                    imagePackage.setPackageVersion(version);
                    imagePackage.setFixedVersion(fixedVersion);
                    imagePackage.setVulnerabilitles(vulnerabilitles);

                    imagePackages.add(imagePackage);
                }
                resultMap.put("image_packages", imagePackages);
                harborManifest.setVulnerabilitiesByPackage(resultMap);
            }
        }
        return harborManifest;
    }

    @Override
    public ActionReturnUtil refreshImageRepo(String harborHost) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/internal/syncregistry";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        return HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, null);
    }


    public String getIdByRepoName(String harborHost,String projectName) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("name", projectName);

        ActionReturnUtil projectResult = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
        List<Map<String,Object>> mapList = JsonUtil.JsonToMapList(projectResult.getData().toString());
        String projectId = null;
        for(Map<String,Object> map : mapList){
            if(map.get("name").toString().equals(projectName)){
                projectId = map.get("project_id").toString();
                break;
            }
        }

        return projectId;
    }

}
