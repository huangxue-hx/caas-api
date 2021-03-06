package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.harbor.ImageCleanRuleMapper;
import com.harmonycloud.dao.harbor.bean.ImageCleanRule;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.k8s.bean.Container;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cache.ImageCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.harbor.HarborManifest;
import com.harmonycloud.service.platform.bean.harbor.HarborRepositoryMessage;
import com.harmonycloud.service.platform.bean.harbor.ImageCleanRuleDetail;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.platform.service.harbor.HarborImageCleanService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.NUM_TWO;
import static com.harmonycloud.common.Constant.CommonConstant.SLASH;
import static com.harmonycloud.service.platform.constant.Constant.TIME_ZONE_UTC;


/**
 * 镜像清理Service
 *  created by zackchen 2017/12/14
 */
@Service
public class HarborImageCleanServiceImpl implements HarborImageCleanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HarborImageCleanServiceImpl.class);
    private static final int DOCKER_PORT = 2379;
    private static final String CLEAN_IMAGE_REDIS_KEY_PREFIX = "imagegc";

    @Autowired
    private HarborService harborService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private HarborProjectService harborProjectService;

    @Autowired
    private ImageCleanRuleMapper imageCleanRuleMapper;
    @Autowired
    private DeploymentsService deploymentsService;
    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private ImageCacheManager imageCacheManager;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;
    /**
     *  全局镜像版本保留数量，默认为保留200个版本
     */
    @Value("${image.tag.keep:200}")
    private String defaultTagKeepCount;

    private List<ImageCleanRule> listAllCleanRules() {
        List<ImageCleanRule> imageCleanRules = imageCleanRuleMapper.list();
        Map<Integer, ImageCleanRule> imageCleanRuleMap = imageCleanRules.stream()
                .collect(Collectors.toMap(ImageCleanRule::getRepositoryId, rule -> rule));
        try {
            ImageRepository queryRepository = new ImageRepository();
            queryRepository.setIsNormal(Boolean.TRUE);
            List<ImageRepository> imageRepositories = harborProjectService.listRepositories(queryRepository);
            for (ImageRepository imageRepository : imageRepositories) {
                //没有创建清理规则的仓库，使用默认的全局镜像版本保留数量进行清理
                if (imageCleanRuleMap.get(imageRepository.getId()) == null) {
                    ImageCleanRule imageCleanRule = new ImageCleanRule();
                    imageCleanRule.setType(Constant.IMAGE_CLEAN_RULE_TYPE_REPOSITORY);
                    imageCleanRule.setRepositoryId(imageRepository.getId());
                    imageCleanRule.setKeepTagCount(Integer.parseInt(defaultTagKeepCount));
                    imageCleanRules.add(imageCleanRule);
                }
            }
        }catch (Exception e){
            LOGGER.error("添加默认清理规则失败",e);
        }
        return imageCleanRules;
    }

    /**
     * 根据清理规则，获取要清理的仓库镜像
     *
     * @return List<ImageCleanRuleDetail>
     * @throws Exception
     */
    public void cleanRepo() throws Exception{
        List<ImageCleanRuleDetail> details = getReposByRule();
        Set<String> deletedImageHarbor = filterAndCleanRepos(details);
        for(String harborHost : deletedImageHarbor){
            cleanImageGarbage(harborHost);
            //同步harbor的registry和db数据
            harborService.syncRegistry(harborHost);
        }
    }

    /**清理规则CURD
     *
     * @param rule
     * @param flag
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil setCleanRule(ImageCleanRule rule, int flag) throws Exception {
        if(flag == Constant.DB_OPERATION_FLAG_DELETE || flag == Constant.DB_OPERATION_FLAG_UPDATE){
            if(rule.getId() == null || rule.getId() <= 0){
                throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
            }
        }
        //防止规则名称、仓库重复
        if (flag == Constant.DB_OPERATION_FLAG_INSERT
                || flag == Constant.DB_OPERATION_FLAG_SAVE){
            if (!StringUtils.isAnyBlank(rule.getName())){
                List<ImageCleanRule> rules = imageCleanRuleMapper.getByName(rule.getName());
                if(!CollectionUtils.isEmpty(rules)){
                    throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_RULE_NAME_ALREADY_EXIST);
                }
            }
            if (null != rule.getRepositoryId() && rule.getRepositoryId() > 0){
                List<Integer> list = new ArrayList<>();
                list.add(rule.getRepositoryId());
                List<ImageCleanRule> rules = imageCleanRuleMapper.listByIds(list);
                if(!CollectionUtils.isEmpty(rules)){
                    throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_RULE_REPO_ALREADY_EXIST);

                }
            }
            ImageRepository imageRepository = harborProjectService.findRepositoryById(rule.getRepositoryId());
            if(imageRepository.isPublic() && !userService.checkCurrentUserIsAdmin()){
                throw new MarsRuntimeException(ErrorCodeMessage.PUBLIC_HARBOR_PROJECT_CLEAN_ACCESS);
            }
        }

        try{
            Date createdTime = new Date();
            switch (flag){
                case Constant.DB_OPERATION_FLAG_INSERT:
                    rule.setCreateTime(createdTime);
                    imageCleanRuleMapper.insert(rule);
                    return ActionReturnUtil.returnSuccess();
                case Constant.DB_OPERATION_FLAG_QUERY:
                    List<ImageCleanRule> rules = imageCleanRuleMapper.getBySelective(rule);
                    return ActionReturnUtil.returnSuccessWithData(rules);
                case Constant.DB_OPERATION_FLAG_UPDATE:
                    rule.setUpdateTime(createdTime);
                    imageCleanRuleMapper.update(rule);
                    return ActionReturnUtil.returnSuccess();
                case Constant.DB_OPERATION_FLAG_DELETE:
                    imageCleanRuleMapper.delete(rule.getId());
                    return ActionReturnUtil.returnSuccess();
                case Constant.DB_OPERATION_FLAG_SAVE:
                    if (null==rule.getId() || rule.getId()<=0){
                        rule.setCreateTime(createdTime);
                        imageCleanRuleMapper.insert(rule);
                    } else {
                        rule.setUpdateTime(createdTime);
                        imageCleanRuleMapper.update(rule);
                    }
                    return ActionReturnUtil.returnSuccess();
                default: return ActionReturnUtil.returnError();
            }
        }catch (Exception e){
            LOGGER.error("设置清理规则失败", e);
            throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_SET_CLEAN_RULE_FAILED);
        }
    }

    @Override
    public List<ImageCleanRule> listByIds(List<Integer> repositoryIds) {
        if(CollectionUtils.isEmpty(repositoryIds)){
            return Collections.emptyList();
        }
        return imageCleanRuleMapper.listByIds(repositoryIds);
    }

    /**
     * harbor删除镜像之后，需要docker清理磁盘镜像文件释放磁盘空间
     */
    @Override
    public boolean cleanImageGarbage(String harborHost) throws Exception{
        LOGGER.info("开始对harbor清理镜像文件，harborHost:{}",harborHost);
        String redisKey = CLEAN_IMAGE_REDIS_KEY_PREFIX + harborHost;
        boolean setKey = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, harborHost);
        if(!setKey){
            LOGGER.info("清理镜像文件程序已经在运行，harborHost:{}",harborHost);
            return true;
        }
        stringRedisTemplate.expire(redisKey, NUM_TWO, TimeUnit.HOURS);
        String shellPath = this.getClass().getClassLoader().getResource("shell/cleanImage.sh").getPath();
        ProcessBuilder proc = new ProcessBuilder("sh", shellPath, "tcp://" + harborHost + CommonConstant.COLON + DOCKER_PORT);
        Process p = proc.start();
        String res;
        boolean cleanResult = true;
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((res = stdInput.readLine()) != null) {
            res = res.trim();
            LOGGER.info(res);
        }
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((res = stdError.readLine()) != null) {
            LOGGER.error(res);
            cleanResult = false;
        }
        stringRedisTemplate.delete(redisKey);
        LOGGER.info("对harbor清理镜像文件结束，harborHost:{}",harborHost);
        return cleanResult;
    }

    @Override
    public boolean isHarborInGc(String harborHost) throws Exception{
        String value = stringRedisTemplate.opsForValue().get(CLEAN_IMAGE_REDIS_KEY_PREFIX + harborHost);
        if(StringUtils.isBlank(value)){
            return false;
        }
        return true;
    }

    @Override
    public void deleteClusterCleanRule(String clusterId) throws Exception{
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER);
        imageCleanRuleMapper.deleteByClusterId(clusterId);
    }

    private List<ImageCleanRuleDetail> getReposByRule() throws Exception {
        Map<String, ImageCleanRuleDetail> filteredImages = new HashMap<String, ImageCleanRuleDetail>();
        List<ImageCleanRule> rules = listAllCleanRules();
        for (ImageCleanRule rule : rules) {
            //try-catch 防止一个规则运行失败导致其他规则不继续
            try {
                ImageRepository imageRepository = harborProjectService.findRepositoryById(rule.getRepositoryId());
                if (imageRepository == null) {
                    //如果清理规则对应的镜像仓库已经不存在，则删除该清理规则
                    this.setCleanRule(rule,Constant.DB_OPERATION_FLAG_DELETE);
                    LOGGER.error("镜像清理失败，未找到镜像仓库，已删除该规则：{}", JSONObject.toJSONString(rule));
                    continue;
                }
                ActionReturnUtil actionReturnUtil = harborService.repoListById(imageRepository.getHarborHost(),
                        imageRepository.getHarborProjectId());
                if (actionReturnUtil.isSuccess()) {
                    List<String> repoList = (List<String>) actionReturnUtil.get("data");
                    //提前判断配置的镜像名称是否存在
                    if (Constant.IMAGE_CLEAN_RULE_TYPE_IMAGE == rule.getType()) {
                        if (StringUtils.isEmpty(rule.getRepoName()) || !repoList.contains(rule.getRepoName())) {
                            LOGGER.warn("规则配置错误，未找到配置的镜像,harborHost:{},harborProjectId:{}",
                                    imageRepository.getHarborHost(), imageRepository.getHarborProjectId());
                            continue;
                        }
                    }
                    String key = imageRepository.getHarborHost() + SLASH + imageRepository.getHarborProjectName();
                    if (!filteredImages.containsKey(key)) {
                        ImageCleanRuleDetail imageCleanRuleDetail = new ImageCleanRuleDetail();
                        if (Constant.IMAGE_CLEAN_RULE_TYPE_REPOSITORY == rule.getType()) {
                            imageCleanRuleDetail.setRepoList(repoList);
                        } else {
                            //镜像级别只保存单个镜像名称
                            List<String> tempList = new ArrayList<String>();
                            tempList.add(rule.getRepoName());
                            imageCleanRuleDetail.setRepoList(tempList);
                        }
                        imageCleanRuleDetail.setImageRepository(imageRepository);
                        imageCleanRuleDetail.setRule(rule);
                        imageCleanRuleDetail.setProjectKey(key);
                        filteredImages.put(key, imageCleanRuleDetail);
                    } else {
                        //如果是镜像级别，则需要处理就的镜像列表；如果是repository级别，则不需要操作，因为原来已经有了
                        if (Constant.IMAGE_CLEAN_RULE_TYPE_IMAGE == rule.getType()) {
                            ImageCleanRuleDetail imageCleanRuleDetail = filteredImages.get(key);
                            if (Constant.IMAGE_CLEAN_RULE_TYPE_REPOSITORY == imageCleanRuleDetail.getRule().getType()) {
                                List<String> replacedRepoList = new ArrayList<String>();
                                replacedRepoList.add(rule.getRepoName());
                                //冲掉了就的repository级别的镜像列表，因为以镜像级别为准
                                imageCleanRuleDetail.setRepoList(replacedRepoList);
                            } else {
                                //如果原来已经有镜像级别的，则把现有镜像加进去
                                imageCleanRuleDetail.getRepoList().add(rule.getRepoName());
                            }
                        }
                    }

                }
            }catch (Exception e){
                LOGGER.error("镜像清理失败，rule：{}", JSONObject.toJSONString(rule));
            }
        }

        return new ArrayList<>(filteredImages.values());

    }

    private Set<String> filterAndCleanRepos(List<ImageCleanRuleDetail> filteredImages) throws Exception{
        Set<String> usingImages = this.getUsingImages();
        Set<String> deletedImageHarbor = new HashSet<>();
        for (ImageCleanRuleDetail detail: filteredImages) {
            for (String repoName: detail.getRepoList()) {
                HarborRepositoryMessage repositoryMessage = imageCacheManager.getRepoMessage(detail.getImageRepository().getHarborHost(),repoName);
                if(repositoryMessage == null || CollectionUtils.isEmpty(repositoryMessage.getRepositoryDetial())){
                    continue;
                }
                List<HarborManifest> harborManifests = repositoryMessage.getRepositoryDetial();
                Set<String> keepDigest = new HashSet<>();
                //清除这个时间点之前创建的镜像
                if (detail.getRule().getTimeBefore() != null && detail.getRule().getTimeBefore() > 0){
                    Date startDate = DateUtils.addDays(DateUtil.getCurrentUtcTime(), -detail.getRule().getTimeBefore());
                    for(HarborManifest harborManifest : harborManifests) {
                        if(harborManifest.getCreateTime() == null){
                            continue;
                        }
                        Date tagCreatedTime = DateUtil.stringToDate(harborManifest.getCreateTime(),
                                DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), TIME_ZONE_UTC);
                        if (tagCreatedTime.after(startDate)) {
                            keepDigest.add(harborManifest.getDigest());
                            continue;
                        }
                        //如果镜像正在使用，不清理该镜像
                        String imageFullName = repositoryMessage.getFullNameRepo()+ CommonConstant.COLON + harborManifest.getTag();
                        if (usingImages.contains(imageFullName)) {
                            LOGGER.info("镜像清理，镜像正在使用，不清理,repo:{},tag:{}",repositoryMessage.getRepository(), harborManifest.getTag());
                            keepDigest.add(harborManifest.getDigest());
                            continue;
                        }
                        //版本号包含此文本的排除，不会被清理
                        if(isTagContain(detail.getRule().getTagNameExclude(), harborManifest.getTag())){
                            keepDigest.add(harborManifest.getDigest());
                            continue;
                        }
                        //对于同一个digest的镜像的不同标签，docker不支持只删除指定tag，只能根据digest删除，如果digest相同，
                        // 其中一个标签需要保留，那该镜像不会删除，其他标签也不能删除
                        if(keepDigest.contains(harborManifest.getDigest())){
                            continue;
                        }
                        LOGGER.info("镜像清理，删除镜像,repo:{},tag:{}",repoName,harborManifest.getTag());
                        harborService.deleteRepo(detail.getImageRepository().getHarborHost(), repoName, harborManifest.getTag());
                        deletedImageHarbor.add(detail.getImageRepository().getHarborHost());
                        continue;
                    }
                }
                //保留多少个最近更新的版本数量，将历史版本删除
                if(detail.getRule().getKeepTagCount() != null && detail.getRule().getKeepTagCount()>0
                        && harborManifests.size() > detail.getRule().getKeepTagCount()){
                    int keepCount = 0;
                    for(int i=0; i<harborManifests.size(); i++){
                        HarborManifest harborManifest = harborManifests.get(i);
                        //版本号包含此文本的排除，不会被清理
                        if(isTagContain(detail.getRule().getTagNameExclude(), harborManifest.getTag())){
                            keepDigest.add(harborManifest.getDigest());
                            continue;
                        }
                        if(keepCount < detail.getRule().getKeepTagCount()){
                            keepCount++;
                            keepDigest.add(harborManifest.getDigest());
                            continue;
                        }
                        String imageFullName = repositoryMessage.getFullNameRepo()+ CommonConstant.COLON + harborManifest.getTag();
                        if(usingImages.contains(imageFullName)){
                            LOGGER.info("镜像清理，镜像正在使用，不清理,repo:{},tag:{}",repositoryMessage.getRepository(), harborManifest.getTag());
                            keepDigest.add(harborManifest.getDigest());
                            continue;
                        }
                        if(keepDigest.contains(harborManifest.getDigest())){
                            continue;
                        }
                        LOGGER.info("镜像清理，删除镜像,repo:{},tag:{}",repositoryMessage.getRepository(), harborManifest.getTag());
                        harborService.deleteRepo(detail.getImageRepository().getHarborHost(),
                                repositoryMessage.getRepository(), harborManifest.getTag());
                        deletedImageHarbor.add(detail.getImageRepository().getHarborHost());
                    }
                }
            }
        }
        return deletedImageHarbor;
    }

    /**
     * 获取正在使用的镜像列表
     * @return
     * @throws Exception
     */
    private Set<String> getUsingImages() throws Exception {
        Set<String> usingImages = new HashSet<>();
        List<NamespaceLocal> namespaceData = new ArrayList<>();
        List<Cluster> clusters = clusterService.listCluster();
        for(Cluster cluster : clusters){
            namespaceData.addAll(namespaceLocalService.getNamespaceListByClusterId(cluster.getId()));
        }
        for (NamespaceLocal oneNamespace : namespaceData ){
            String namespaceName = oneNamespace.getNamespaceName();
            if(StringUtils.isBlank(namespaceName)){
                continue;
            }
            DeploymentList deploymentList = deploymentsService.listDeployments(namespaceName,null);
            if (deploymentList == null || CollectionUtils.isEmpty(deploymentList.getItems())) {
                continue;
            }
            for (Deployment deployment : deploymentList.getItems()) {
                List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
                usingImages.addAll(containers.stream().map(Container::getImage).collect(Collectors.toSet()));
            }
        }
        return usingImages;
    }

    private boolean isTagContain(String exp, String tagName){
        if (StringUtils.isEmpty(exp)){
            return false;
        }
        String[] tagExps = exp.split(",");
        for (String tempExp:tagExps) {
            if (tagName.indexOf(tempExp) > -1 ){
                return true;
            }
        }
        return false;
    }


}
