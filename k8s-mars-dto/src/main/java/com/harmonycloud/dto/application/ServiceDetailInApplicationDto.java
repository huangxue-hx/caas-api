package com.harmonycloud.dto.application;

import com.harmonycloud.k8s.bean.LabelSelector;

import java.util.List;
import java.util.Map;

/**
 * @Author jiangmi
 * @Description 应用详情内的服务列表内容
 * @Date created in 2018-5-15
 * @Modified
 */
public class ServiceDetailInApplicationDto {

    private String isExternal;

    private String name;

    private Map<String, String> labels;

    private String status;

    private String version;

    private List<String> cpu;

    private List<String> memory;

    private boolean isPV;

    private List<String> img;

    private Integer instance;

    private String createTime;

    private String namespace;

    private String aliasNamespace;

    private LabelSelector selector;

    public String getIsExternal() {
        return isExternal;
    }

    public void setIsExternal(String isExternal) {
        this.isExternal = isExternal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getCpu() {
        return cpu;
    }

    public void setCpu(List<String> cpu) {
        this.cpu = cpu;
    }

    public List<String> getMemory() {
        return memory;
    }

    public void setMemory(List<String> memory) {
        this.memory = memory;
    }

    public boolean isPV() {
        return isPV;
    }

    public void setPV(boolean PV) {
        isPV = PV;
    }

    public List<String> getImg() {
        return img;
    }

    public void setImg(List<String> img) {
        this.img = img;
    }

    public Integer getInstance() {
        return instance;
    }

    public void setInstance(Integer instance) {
        this.instance = instance;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAliasNamespace() {
        return aliasNamespace;
    }

    public void setAliasNamespace(String aliasNamespace) {
        this.aliasNamespace = aliasNamespace;
    }

    public LabelSelector getSelector() {
        return selector;
    }

    public void setSelector(LabelSelector selector) {
        this.selector = selector;
    }
}
