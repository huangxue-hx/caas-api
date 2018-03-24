package com.harmonycloud.service.cluster.impl;

import com.harmonycloud.common.enumm.ClusterLevelEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.cluster.TemplateTPRDto;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.cluster.StatusConditions;
import com.harmonycloud.k8s.bean.cluster.Template;
import com.harmonycloud.k8s.bean.cluster.TemplateList;
import com.harmonycloud.k8s.service.ClusterTemplateCRDService;
import com.harmonycloud.k8s.util.DefaultClient;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.k8sUtil;
import com.harmonycloud.service.cluster.ClusterTemplateService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClusterTemplateServiceImpl  implements  ClusterTemplateService{

    public static final String DEFAULT_NAMESAPCE = "cluster-top";


    private ClusterTemplateCRDService clusterTemplaeCRDService = new ClusterTemplateCRDService();

    @Override
    public ActionReturnUtil addClusterTemplate(TemplateTPRDto clusterTemplateTPRDto) throws Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        Template clustertemplate = this.convertTPR(clusterTemplateTPRDto);
        K8SClientResponse response = clusterTemplaeCRDService.addClusterTemplate(clustertemplate, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
        } else {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
    }


    @Override
    public ActionReturnUtil getClusterTemplate(String name) throws Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        K8SClientResponse response = clusterTemplaeCRDService.getClusterTemplate(DEFAULT_NAMESAPCE, name ,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        Template template = JsonUtil.jsonToPojo(response.getBody(), Template.class);
        TemplateTPRDto templateTPRDto = this.convertDto(template);
        return ActionReturnUtil.returnSuccessWithData(templateTPRDto);
    }

    @Override
    public ActionReturnUtil listClusterTemplates() throws Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        TemplateList templateList = clusterTemplaeCRDService.listClusterTemplates(DEFAULT_NAMESAPCE,cluster);
        if (templateList.getItems() != null && templateList.getItems().isEmpty()) {
            return ActionReturnUtil.returnSuccessWithData(new ArrayList<>());
        }
        List<TemplateTPRDto> templateTPRDtoList = new ArrayList<>();
        for (Template template : templateList.getItems()) {
             templateTPRDtoList.add(this.convertDto(template));
        }
        return ActionReturnUtil.returnSuccessWithData(templateTPRDtoList);
    }

    @Override
    public ActionReturnUtil updateClusterTemplate(String name, TemplateTPRDto clusterTemplateTPRDto) throws Exception {
        if(!name.equals(clusterTemplateTPRDto.getName())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.TEMPLATE_NAME_CAN_NOT_UPDATE);
        }
        Cluster cluster = DefaultClient.getDefaultCluster();
        K8SClientResponse getResponse = clusterTemplaeCRDService.getClusterTemplate(DEFAULT_NAMESAPCE, name ,cluster);
        Template template;
        if (HttpStatusUtil.isSuccessStatus(getResponse.getStatus())){
            template = JsonUtil.jsonToPojo(getResponse.getBody(), Template.class);
        }else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.TEMPLATE_NOT_FOUND);
        }
        Template clustertemplate = this.convertTPR(clusterTemplateTPRDto);
        if ( null != clustertemplate.getStatus()){
            List<StatusConditions> newConditions = clustertemplate.getStatus().getConditions();
            List<StatusConditions> oldConditions = template.getStatus().getConditions();
            List<StatusConditions> updateList =  k8sUtil.GetUpdateStatus(newConditions,oldConditions);
            clustertemplate.getStatus().setConditions(updateList);
        } else {
            clustertemplate.setStatus(template.getStatus());
        }
        clustertemplate.setMetadata(template.getMetadata());
        K8SClientResponse response = clusterTemplaeCRDService.updateClusterTemplate(clustertemplate, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
        } else {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
    }

    @Override
    public ActionReturnUtil deleteClusterTemplate(String name) throws Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        K8SClientResponse getResponse = clusterTemplaeCRDService.getClusterTemplate(DEFAULT_NAMESAPCE, name ,cluster);
        if (HttpStatusUtil.isSuccessStatus(getResponse.getStatus())){
            Template template = JsonUtil.jsonToPojo(getResponse.getBody(), Template.class);
            List<StatusConditions> statusConditions = template.getStatus().getConditions();
            Map<String, Boolean> statusMap = statusConditions.stream().collect(Collectors.toMap(StatusConditions::getType,condition -> condition.getStatus()));
            if (statusMap.get("used")) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.TEMPLATE_STATUS_NOT_DELETE);
            }
        }else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.TEMPLATE_NOT_FOUND);
        }
        K8SClientResponse response = clusterTemplaeCRDService.deleteClusterTemplate(DEFAULT_NAMESAPCE, name, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
        } else {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
    }


    private  Template convertTPR(TemplateTPRDto templateTPRDto) throws  Exception{
        Template template = new Template();
        template.setApiVersion("harmonycloud.cn/v1");
        template.setKind("ClusterTemplate");

        ObjectMeta meta = new ObjectMeta();
        meta.setName(templateTPRDto.getName());
        meta.setNamespace(templateTPRDto.getDataCenter());
        template.setMetadata(meta);
        template.setTemplateSpec(templateTPRDto.getTemplate());
        template.setStatus(templateTPRDto.getStatus());

        return template;
    }

    private TemplateTPRDto convertDto(Template template) throws Exception {
        TemplateTPRDto templateTPRDto = new TemplateTPRDto();
        ObjectMeta meta = template.getMetadata();
        templateTPRDto.setLevel(ClusterLevelEnum.getEnvLevel(meta.getName()).getLevel());
        templateTPRDto.setName(meta.getName());
        templateTPRDto.setDataCenter(meta.getNamespace());
        templateTPRDto.setTemplate(template.getTemplateSpec());
        templateTPRDto.setStatus(template.getStatus());
        return templateTPRDto;

    }
}
