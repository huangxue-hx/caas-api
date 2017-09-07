package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeAffinity {
    private NodeSelector requiredDuringSchedulingIgnoredDuringExecution;

    private List<PreferredSchedulingTerm> preferredDuringSchedulingIgnoredDuringExecution;
}
