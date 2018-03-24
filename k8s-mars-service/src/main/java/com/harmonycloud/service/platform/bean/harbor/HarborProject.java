package com.harmonycloud.service.platform.bean.harbor;


import java.io.Serializable;

/**
 * Created by zsl on 2017/1/19.
 * harbor project bean
 */
public class HarborProject implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer projectId;                  //id
    private String projectName;                 //名称
    private Integer userId;                     //相关userId
    private String ownerName;                   //工程所属人员名称
    private Integer isPublic = 0;               //是否公开的 0私有的，1公开的
    private Integer deleted = 0;                //是否删除的 0否，1是
    private String createTime;                  //创建时间
    private Float quotaSize = 0.0f;
    private Float useSize;
    private Float useRate;
    private Integer repoCount;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Float getQuotaSize() {
        return quotaSize;
    }

    public void setQuotaSize(Float quotaSize) {
        this.quotaSize = quotaSize;
    }

    public Integer getRepoCount() {
        return repoCount;
    }

    public void setRepoCount(Integer repoCount) {
        this.repoCount = repoCount;
    }

    public Float getUseSize() {
        return useSize;
    }

    public void setUseSize(Float useSize) {
        this.useSize = useSize;
    }

    public Float getUseRate() {
        return useRate;
    }

    public void setUseRate(Float useRate) {
        this.useRate = useRate;
    }
}
