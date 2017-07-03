package com.harmonycloud.dto.tenant.show;

import java.util.List;
import java.util.Map;

/**
 * Created by andy on 17-2-6.
 */
public class NamespaceShowDto {

    private String name;

    private String time;

    private Map<String, Object> tenant;

    private String annotation;

    private List<String> services;

    private Integer serviceNumber;

    private List<RolebindingShowDto> member;

    private Integer memberNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Map<String, Object> getTenant() {
        return tenant;
    }

    public void setTenant(Map<String, Object> tenant) {
        this.tenant = tenant;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public Integer getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(Integer serviceNumber) {
        this.serviceNumber = serviceNumber;
    }

    public List<RolebindingShowDto> getMember() {
        return member;
    }

    public void setMember(List<RolebindingShowDto> member) {
        this.member = member;
    }

    public Integer getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(Integer memberNumber) {
        this.memberNumber = memberNumber;
    }
}
