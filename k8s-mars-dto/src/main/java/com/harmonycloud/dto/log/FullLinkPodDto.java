package com.harmonycloud.dto.log;

public class FullLinkPodDto {
    private String name;
    private String podName;

    public FullLinkPodDto(String name, String podName) {
        this.name = name;
        this.podName = podName;
    }

    public FullLinkPodDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }
}
