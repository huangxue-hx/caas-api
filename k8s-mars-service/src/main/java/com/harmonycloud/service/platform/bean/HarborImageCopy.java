package com.harmonycloud.service.platform.bean;

/**
 * Created by root on 4/7/17.
 */
public class HarborImageCopy {
    private String   src_repo_name;
    private String   src_tag;
    private String   dest_repo_name;
    private String   dest_tag;

    public String getSrc_repo_name() {
        return src_repo_name;
    }

    public void setSrc_repo_name(String src_repo_name) {
        this.src_repo_name = src_repo_name;
    }

    public String getSrc_tag() {
        return src_tag;
    }

    public void setSrc_tag(String src_tag) {
        this.src_tag = src_tag;
    }

    public String getDest_repo_name() {
        return dest_repo_name;
    }

    public void setDest_repo_name(String dest_repo_name) {
        this.dest_repo_name = dest_repo_name;
    }

    public String getDest_tag() {
        return dest_tag;
    }

    public void setDest_tag(String dest_tag) {
        this.dest_tag = dest_tag;
    }
}