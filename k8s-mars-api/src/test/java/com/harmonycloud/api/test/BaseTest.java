package com.harmonycloud.api.test;

import com.harmonycloud.common.enumm.ClusterLevelEnum;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.tenant.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by zhangkui on 2018/6/19.
 */
@Test
@WebAppConfiguration
@ContextConfiguration(locations={"classpath:applicationContext-test.xml"})
public class BaseTest extends AbstractTransactionalTestNGSpringContextTests {
    protected Logger logger= LoggerFactory.getLogger(BaseTest.class);
    protected MockMvc mockMvc;
    protected List<Cluster> clusters;
    protected String devClusterId;
    protected String qasClusterId;
    protected String tenantId;
    protected String projectId;
    protected String pmUsername;
    protected String adminUserName = "admin";


    @Autowired
    ClusterService clusterService;
    @Autowired
    ProjectService projectService;
    @Autowired
    WebApplicationContext wac;

    @BeforeClass
    public void initTestData() throws Exception{
        if(clusters != null){
            return;
        }
        logger.info("初始化测试数据......");
        //初始化集群数据
        clusters = clusterService.listAllCluster(null);
        if(CollectionUtils.isEmpty(clusters)){
            throw new Exception("cluster list is empty.");
        }
        for(Cluster cluster: clusters){
            if(cluster.getLevel() == ClusterLevelEnum.DEV.getLevel()){
                devClusterId = cluster.getId();
            }else if(cluster.getLevel() == ClusterLevelEnum.QAS.getLevel()){
                qasClusterId = cluster.getId();
            }
        }
        //初始化项目数据
        List<Project> projects = projectService.listAllProject();
        if(CollectionUtils.isEmpty(projects)){

        }else{
            projectId = projects.get(0).getProjectId();
            tenantId = projects.get(0).getTenantId();
            //获取系统中的某个PmUsername
            for (Project project : projects) {
                if (project.getPmUsernames() == null) {
                    continue;
                }
                pmUsername = project.getPmUsernames().split(",")[0];
                break;
            }
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

}
