package com.harmonycloud.service.platform.bean.harbor;

/**
 * Created by root on 4/7/17.
 */
public class HarborImageCopy {
    private String  harborHost;
    private String   srcRepoName;
    private String   srcTag;
    private String   destRepoName;
    private String   destTag;

    public HarborImageCopy() {
    }

    public HarborImageCopy(String harborHost, String srcRepoName, String srcTag, String destRepoName, String destTag) {
        this.harborHost = harborHost;
        this.srcRepoName = srcRepoName;
        this.srcTag = srcTag;
        this.destRepoName = destRepoName;
        this.destTag = destTag;
    }

    public String getSrcRepoName() {
        return srcRepoName;
    }

    public void setSrcRepoName(String srcRepoName) {
        this.srcRepoName = srcRepoName;
    }

    public String getSrcTag() {
        return srcTag;
    }

    public void setSrcTag(String srcTag) {
        this.srcTag = srcTag;
    }

    public String getDestRepoName() {
        return destRepoName;
    }

    public void setDestRepoName(String destRepoName) {
        this.destRepoName = destRepoName;
    }

    public String getDestTag() {
        return destTag;
    }

    public void setDestTag(String destTag) {
        this.destTag = destTag;
    }

    public String getHarborHost() {
        return harborHost;
    }

    public void setHarborHost(String harborHost) {
        this.harborHost = harborHost;
    }
}