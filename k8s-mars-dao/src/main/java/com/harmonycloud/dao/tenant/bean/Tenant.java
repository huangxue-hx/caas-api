package com.harmonycloud.dao.tenant.bean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author admin
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class Tenant {
    
    private String name;
    
    private String time;
    
    private String annotation;
    
    private String tenantid;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * @return the annotation
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * @param annotation the annotation to set
     */
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    /**
     * @return the tenantid
     */
    public String getTenantid() {
        return tenantid;
    }

    /**
     * @param tenantid the tenantid to set
     */
    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

}
