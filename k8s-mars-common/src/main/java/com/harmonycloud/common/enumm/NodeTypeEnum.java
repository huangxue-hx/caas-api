package com.harmonycloud.common.enumm;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.harmonycloud.common.Constant.CommonConstant.LANGUAGE_ENGLISH;
import static com.harmonycloud.common.Constant.CommonConstant.NUM_TEN;

public enum NodeTypeEnum {

    MASTER("主控","node-role.kubernetes.io/master","",1),
    SYSTEM("系统","HarmonyCloud_Status","A",2),
    ISTIO("微服务","istio","istio",3),
    SLB("负载均衡","lb","nginx",4),//集群全局负载均衡
    SLB_CUSTOM("自定义负载均衡","lb","nginx-custom",5),//自定义负载均衡
    BUILDING("构建","HarmonyCloud_Status","E",6),
    PRIVATE("独占","HarmonyCloud_Status","D",7),
    PUBLIC("共享","HarmonyCloud_Status","C",8),
    IDLE("闲置","HarmonyCloud_Status","B",9);


    private static final Set<String> NODE_TYPE_LABEL_KEYS = new HashSet<>();

    /**
     * key为主机类型的标签，HarmonyCloud_Status=A
     */
    private static final Map<String, NodeTypeEnum> NODE_TYPE = new ConcurrentHashMap<>(
            NodeTypeEnum.values().length);

    /**
     * key为主机类型的英文名称
     */
    private static final Map<String, NodeTypeEnum> NODE_TYPE_NAME = new ConcurrentHashMap<>(
            NodeTypeEnum.values().length);

    static {
        for (NodeTypeEnum type : EnumSet.allOf(NodeTypeEnum.class)) {
            NODE_TYPE_LABEL_KEYS.add(type.labelKey);
            NODE_TYPE.put(type.getLabelKey() + "=" + type.getLabelValue(), type);
            NODE_TYPE_NAME.put(type.name(),type);
        }
    }

    private final String name;
    /**
     * 节点类型标签的key
     */
    private final String labelKey;
    /**
     * 节点类型标签的值
     */
    private final String labelValue;
    /**
     * 节点类型排序权重
     */
    private final int weight;

    NodeTypeEnum(String name, String labelKey, String labelValue, int weight) {
        this.name = name;
        this.labelKey = labelKey;
        this.labelValue = labelValue;
        this.weight = weight;
    }

    public static Set<String> getNodeTypeLabelKeys(){
        return NODE_TYPE_LABEL_KEYS;
    }

    public static String getNodeType(String labelKey, String labelValue){
        NodeTypeEnum nodeTypeEnum = NODE_TYPE.get(labelKey + "=" + labelValue);
        if(nodeTypeEnum != null){
            if(LANGUAGE_ENGLISH.equalsIgnoreCase(DictEnum.getCurrentLanguage())){
                return nodeTypeEnum.name();
            }else{
                return nodeTypeEnum.getName();
            }
        }
        return "";
    }

    public static NodeTypeEnum getNodeTypeByEnumName(String enumName){
        if(enumName == null){
            return null;
        }
        return NODE_TYPE_NAME.get(enumName);
    }

    public static String getNodeTypeLabel(NodeTypeEnum nodeTypeEnum){
        return nodeTypeEnum.getLabelKey() + "=" + nodeTypeEnum.getLabelValue();
    }

    public static int getWeight(String nodeType){
        for(NodeTypeEnum nodeTypeEnum : NodeTypeEnum.values()){
            if(nodeType.contains(nodeTypeEnum.name()) || nodeType.contains(nodeTypeEnum.getName())){
                return nodeTypeEnum.getWeight();
            }
        }
        return NUM_TEN;
    }

    public static boolean matchNodeType(String nodeType, NodeTypeEnum nodeTypeEnum){
        if(nodeTypeEnum.getName().equals(nodeType) || nodeTypeEnum.name().equalsIgnoreCase(nodeType)){
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getLabelValue() {
        return labelValue;
    }

    public int getWeight() {
        return weight;
    }
}
