package com.harmonycloud.service.platform.convert;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dto.application.AffinityDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author jiangmi
 * @Description 应用亲和度数据转换类
 * @Date created in 2017-12-20
 * @Modified
 */
public class KubeAffinityConvert {

    /**
     * 组装 node反亲和 返回前端
     *
     * @param nodeAffinity 节点反亲和对象
     * @return List<AffinityDto>
     */
    public static List<AffinityDto> convertNodeAffinityDto(NodeAffinity nodeAffinity) throws Exception {
        List<AffinityDto> nas = new ArrayList<>();

        //非强制亲和
        if (CollectionUtils.isNotEmpty(nodeAffinity.getPreferredDuringSchedulingIgnoredDuringExecution())) {
            List<PreferredSchedulingTerm> psts = nodeAffinity.getPreferredDuringSchedulingIgnoredDuringExecution();
            for (PreferredSchedulingTerm pst : psts) {
                List<NodeSelectorRequirement> nsqs =pst.getPreference().getMatchExpressions();
                if(nsqs != null && nsqs.size() > 0) {
                    for(NodeSelectorRequirement nsq : nsqs ) {
                        AffinityDto na = new AffinityDto();
                        na.setRequired(false);
                        if(nsq.getKey().contains(Constant.NODESELECTOR_LABELS_PRE)) {
                            na.setLabel(nsq.getKey().split(Constant.NODESELECTOR_LABELS_PRE)[1] + "=" + nsq.getValues().get(0));
                        }else {
                            na.setLabel(nsq.getKey() + "=" + nsq.getValues().get(0));
                        }
                        nas.add(na);
                    }
                }
            }
        }

        //强制亲和
        if (Objects.nonNull(nodeAffinity.getRequiredDuringSchedulingIgnoredDuringExecution())) {
            NodeSelector r = nodeAffinity.getRequiredDuringSchedulingIgnoredDuringExecution();
            if (CollectionUtils.isNotEmpty(r.getNodeSelectorTerms())){
                for (NodeSelectorTerm nst : r.getNodeSelectorTerms()) {
                    if (Objects.nonNull(nst) && CollectionUtils.isNotEmpty(nst.getMatchExpressions())) {
                        List<NodeSelectorRequirement> nsr = nst.getMatchExpressions();
                        for(NodeSelectorRequirement ns : nsr) {
                            AffinityDto na = new AffinityDto();
                            na.setRequired(true);
                            if(ns.getKey().contains(Constant.NODESELECTOR_LABELS_PRE)) {
                                na.setLabel(ns.getKey().split(Constant.NODESELECTOR_LABELS_PRE)[1] + "=" + ns.getValues().get(0));
                            }else {
                                na.setLabel(ns.getKey() + "=" + ns.getValues().get(0));
                            }
                            nas.add(na);
                        }
                    }
                }
            }
        }
        return nas;
    }

    /**
     * 单个带权重的pod亲和与反亲和数据组装 前端
     *
     * @param wpat 权重pod亲和
     * @return AffinityDto
     * @throws Exception 异常
     */
    private static AffinityDto convertPodAntiOrAffinityWithWeight(WeightedPodAffinityTerm wpat) throws Exception {
        AffinityDto pad = new AffinityDto();
        pad.setRequired(false);
        if (Objects.nonNull(wpat.getPodAffinityTerm()) && Objects.nonNull(wpat.getPodAffinityTerm().getLabelSelector()) &&
                CollectionUtils.isNotEmpty(wpat.getPodAffinityTerm().getLabelSelector().getMatchExpressions())) {
            if(CollectionUtils.isNotEmpty(wpat.getPodAffinityTerm().getNamespaces())) {
                pad.setNamespace(wpat.getPodAffinityTerm().getNamespaces().get(0));
            }
            for (LabelSelectorRequirement lsq : wpat.getPodAffinityTerm().getLabelSelector().getMatchExpressions()) {
                pad.setLabel(lsq.getKey() + "=" + lsq.getValues().get(0));
            }
        }
        return pad;
    }

    /**
     * 强制亲和与反亲和的数据组装 前端
     * @param podAffinityTerms
     * @return List<AffinityDto>
     * @throws Exception 异常
     */
    private static List<AffinityDto> convertPodAntiOrAffinityWithRequired(List<PodAffinityTerm> podAffinityTerms) throws Exception {
        List<AffinityDto> podAffinityDtos = new ArrayList<>();
        for (PodAffinityTerm podAffinityTerm : podAffinityTerms) {
            AffinityDto podAffinityDto = new AffinityDto();
            podAffinityDto.setRequired(true);
            if(CollectionUtils.isNotEmpty(podAffinityTerm.getNamespaces())) {
                podAffinityDto.setNamespace(podAffinityTerm.getNamespaces().get(0));
            }
            LabelSelector labelSelector = podAffinityTerm.getLabelSelector();
            List<LabelSelectorRequirement> labelSelectorRequirements = labelSelector.getMatchExpressions();
            for (LabelSelectorRequirement labelSelectorRequirement : labelSelectorRequirements) {
                podAffinityDto.setLabel(labelSelectorRequirement.getKey() + "=" + labelSelectorRequirement.getValues().get(0));
            }
            podAffinityDtos.add(podAffinityDto);
        }
        return podAffinityDtos;
    }

    /**
     * 组装 pod 反亲和 返回前端
     *
     * @param podAntiAffinity
     * @return List<PodAffinityDto>
     * @throws Exception 异常
     */
    public static List<AffinityDto> convertPodAntiAffinityDto(PodAntiAffinity podAntiAffinity) throws Exception {
        List<AffinityDto> podAffinityDtos = new ArrayList<>();

        //非强制
        if (CollectionUtils.isNotEmpty(podAntiAffinity.getPreferredDuringSchedulingIgnoredDuringExecution())) {
            for (WeightedPodAffinityTerm wpat : podAntiAffinity.getPreferredDuringSchedulingIgnoredDuringExecution()) {
                podAffinityDtos.add(convertPodAntiOrAffinityWithWeight(wpat));
            }
        }
        if (CollectionUtils.isNotEmpty(podAntiAffinity.getRequiredDuringSchedulingIgnoredDuringExecution())) {
            //强制
            List<PodAffinityTerm> podAffinityTerms = podAntiAffinity.getRequiredDuringSchedulingIgnoredDuringExecution();
            podAffinityDtos.addAll(convertPodAntiOrAffinityWithRequired(podAffinityTerms));
        }
        return podAffinityDtos;
    }

    /**
     * 组装 pod亲和 返回前端
     *
     * @param podAffinity
     * @return List<PodAffinityDto>
     * @throws Exception 异常
     */
    public static List<AffinityDto> convertPodAffinityDto(PodAffinity podAffinity) throws Exception {
        List<AffinityDto> podAffinityDtos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(podAffinity.getPreferredDuringSchedulingIgnoredDuringExecution())) {
            //非强制
            for (WeightedPodAffinityTerm wpat : podAffinity.getPreferredDuringSchedulingIgnoredDuringExecution()) {
                podAffinityDtos.add(convertPodAntiOrAffinityWithWeight(wpat));
            }
        }
        if (podAffinity.getRequiredDuringSchedulingIgnoredDuringExecution() != null && podAffinity.getRequiredDuringSchedulingIgnoredDuringExecution().size() > 0) {
            //强制
            List<PodAffinityTerm> podAffinityTerms = podAffinity.getRequiredDuringSchedulingIgnoredDuringExecution();
            podAffinityDtos.addAll(convertPodAntiOrAffinityWithRequired(podAffinityTerms));
        }
        return podAffinityDtos;
    }

    /**
     * 组装数据——Pod亲和（PodAffinityTerm）非强制
     *
     * @param podAffinityDto required
     * @return WeightedPodAffinityTerm
     */
    private static WeightedPodAffinityTerm convertWeightedPodAffinityTerm(AffinityDto podAffinityDto) throws Exception {
        WeightedPodAffinityTerm weightedPodAffinityTerm = new WeightedPodAffinityTerm();
        weightedPodAffinityTerm.setWeight(Constant.WEIGHT);
        PodAffinityTerm podAffinityTerm = convertPodAffinityTerm(podAffinityDto);
        //组装podAffinityTerm
        weightedPodAffinityTerm.setPodAffinityTerm(podAffinityTerm);
        return weightedPodAffinityTerm;
    }

    /**
     * 组装数据——Pod亲和（PodAffinityTerm）强制
     *
     * @param podAffinityDto required
     * @return PodAffinityTerm
     */
     private static PodAffinityTerm convertPodAffinityTerm(AffinityDto podAffinityDto) throws Exception {
         PodAffinityTerm podAffinityTerm = new PodAffinityTerm();
         if(StringUtils.isNotEmpty(podAffinityDto.getNamespace())) {
             List<String> namespaces = new ArrayList<>();
             namespaces.add(podAffinityDto.getNamespace());
             podAffinityTerm.setNamespaces(namespaces);
         }
         //标签选择器
         LabelSelector labelSelector =new LabelSelector();
         List<LabelSelectorRequirement> matchExpressions = new ArrayList<>();
         String[] labels = podAffinityDto.getLabel().split(Constant.EQUAL);
         if(labels.length == 2){
             List<String> values = new ArrayList<>();
             LabelSelectorRequirement labelSelectorRequirement = new LabelSelectorRequirement();
             labelSelectorRequirement.setKey(labels[0]);
             labelSelectorRequirement.setOperator(Constant.AFFINITY_OPERATOR);
             values.add(labels[1]);
             labelSelectorRequirement.setValues(values);
             matchExpressions.add(labelSelectorRequirement);
             labelSelector.setMatchExpressions(matchExpressions);
             podAffinityTerm.setLabelSelector(labelSelector);
             //topologyKey
             podAffinityTerm.setTopologyKey(Constant.AFFINITY_TOPOLOGYKEY);
         }
         return podAffinityTerm;
    }

    /**
     * 封装亲和 Affinity
     *
     * @param nodeAffinityList
     * @param podAffinity
     * @param podAntiAffinitys
     * @return Affinity
     * @throws Exception
     */
    public static Affinity convertAffinity(List<AffinityDto> nodeAffinityList, AffinityDto podAffinity, List<AffinityDto> podAntiAffinitys) throws Exception {
        Affinity affinity = new Affinity();

        //组装节点亲和
        if(CollectionUtils.isNotEmpty(nodeAffinityList)) {
            NodeAffinity na = new NodeAffinity();
            List<NodeSelectorRequirement> preference = new ArrayList<>();
            List<NodeSelectorRequirement> nodeSelectorTerms = new ArrayList<>();
            for(AffinityDto nodeAffinity : nodeAffinityList) {
                if(nodeAffinity != null && !StringUtils.isEmpty(nodeAffinity.getLabel())) {
                    String nodeLabel = nodeAffinity.getLabel();
                    if (!nodeAffinity.isRequired()) {
                        preference.add(convertNodeSelectorTerm(nodeLabel));
                    } else {
                        nodeSelectorTerms.add(convertNodeSelectorTerm(nodeLabel));
                    }
                }
            }
            if(preference.size() > 0 ) {
                List<PreferredSchedulingTerm> pstList = new ArrayList<>();
                //权重
                PreferredSchedulingTerm p = new PreferredSchedulingTerm();
                p.setWeight(Constant.WEIGHT);
                NodeSelectorTerm nodeSelectorTerm = new NodeSelectorTerm();
                nodeSelectorTerm.setMatchExpressions(preference);
                p.setPreference(nodeSelectorTerm);
                pstList.add(p);
                na.setPreferredDuringSchedulingIgnoredDuringExecution(pstList);
            }
            if(nodeSelectorTerms.size() > 0 ) {
                NodeSelector ns = new NodeSelector();
                List<NodeSelectorTerm> nstList = new ArrayList<>();
                NodeSelectorTerm nodeSelectors = new NodeSelectorTerm();
                nodeSelectors.setMatchExpressions(nodeSelectorTerms);
                nstList.add(nodeSelectors);
                ns.setNodeSelectorTerms(nstList);
                na.setRequiredDuringSchedulingIgnoredDuringExecution(ns);
            }
            affinity.setNodeAffinity(na);
        }

        //PodAffinity pod 亲和
        if (podAffinity != null && !StringUtils.isEmpty(podAffinity.getLabel())) {
            PodAffinity podAff = new PodAffinity();
            //强制性亲和
            List<PodAffinityTerm> requiredDuringSchedulingIgnoredDuringExecution = new ArrayList<>();
            //非强制亲和（权重）
            List<WeightedPodAffinityTerm> preferredDuringSchedulingIgnoredDuringExecution = new ArrayList<>();
            if (!podAffinity.isRequired()) {
                //权重 亲和
                WeightedPodAffinityTerm weightedPodAffinityTerm = convertWeightedPodAffinityTerm(podAffinity);
                //组装 weightedPodAffinityTerm
                preferredDuringSchedulingIgnoredDuringExecution.add(weightedPodAffinityTerm);
                podAff.setPreferredDuringSchedulingIgnoredDuringExecution(preferredDuringSchedulingIgnoredDuringExecution);
            } else {
                //强制亲和
                PodAffinityTerm podAffinityTerm = convertPodAffinityTerm(podAffinity);
                //组装PodAffinityTerm
                requiredDuringSchedulingIgnoredDuringExecution.add(podAffinityTerm);
                podAff.setRequiredDuringSchedulingIgnoredDuringExecution(requiredDuringSchedulingIgnoredDuringExecution);
            }
            affinity.setPodAffinity(podAff);
        }
        //PodAntiAffinity
        if (CollectionUtils.isNotEmpty(podAntiAffinitys)) {
            PodAntiAffinity podAntiAffinity = new PodAntiAffinity();
            List<WeightedPodAffinityTerm> preferredDuringSchedulingIgnoredDuringExecution = new ArrayList<>();
            List<PodAffinityTerm> requiredDuringSchedulingIgnoredDuringExecution = new ArrayList<>();
            for (AffinityDto podAffinityDto : podAntiAffinitys) {
                if (podAffinityDto != null) {
                    if (!podAffinityDto.isRequired()) {
                        WeightedPodAffinityTerm weightedPodAffinityTerm = convertWeightedPodAffinityTerm(podAffinityDto);
                        //组装 weightedPodAffinityTerm
                        preferredDuringSchedulingIgnoredDuringExecution.add(weightedPodAffinityTerm);
                    } else {
                        // 非权重
                        PodAffinityTerm podAffinityTerm = convertPodAffinityTerm(podAffinityDto);
                        //组装PodAffinityTerm
                        requiredDuringSchedulingIgnoredDuringExecution.add(podAffinityTerm);
                    }
                }
            }
            podAntiAffinity.setPreferredDuringSchedulingIgnoredDuringExecution(preferredDuringSchedulingIgnoredDuringExecution);
            podAntiAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(requiredDuringSchedulingIgnoredDuringExecution);
            affinity.setPodAntiAffinity(podAntiAffinity);
        }
        return affinity;
    }

    public static NodeSelectorRequirement convertNodeSelectorTerm(String nodeLabel){
        NodeSelectorRequirement nsr = new NodeSelectorRequirement();
        String[] labels = nodeLabel.split(Constant.EQUAL);
        if (labels != null && labels.length == 2) {
            List<String> values = new ArrayList<>();
            if (CommonConstant.HARMONYCLOUD_STATUS.equals(labels[0]) || CommonConstant.HARMONYCLOUD_TENANTNAME_NS.equals(labels[0])) {
                nsr.setKey(labels[0]);
            } else {
                nsr.setKey(Constant.NODESELECTOR_LABELS_PRE + labels[0]);
            }
            nsr.setOperator(Constant.AFFINITY_OPERATOR);
            values.add(labels[1]);
            nsr.setValues(values);
        }
        return nsr;
    }
}
