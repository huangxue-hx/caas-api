package com.harmonycloud.service.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.TemplateTPRDto;
import com.harmonycloud.k8s.bean.cluster.Template;

public interface ClusterTemplateService {
    public ActionReturnUtil listClusterTemplates() throws Exception ;
    public ActionReturnUtil getClusterTemplate(String name) throws Exception ;
    public ActionReturnUtil deleteClusterTemplate(String name) throws Exception ;
    public ActionReturnUtil addClusterTemplate(TemplateTPRDto clusterTemplateTPRDto) throws Exception ;
    public ActionReturnUtil updateClusterTemplate(String name, TemplateTPRDto clusterTemplateTPRDto) throws Exception ;
}
