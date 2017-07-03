package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.bean.Job;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by anson on 17/5/31.
 */
public interface JobService {

    ActionReturnUtil createJob(Job job, String username);

    ActionReturnUtil updateJob(Job job);

    ActionReturnUtil deleteJob(String jobName, String tenant);

    ActionReturnUtil nameValidate(String jobName, String tenantName);

    ActionReturnUtil getJobList(String tenantName, String username);

    ActionReturnUtil getJobDetail(String tenantName, String jobName);

    ActionReturnUtil build(String jobName, String tenantName, String tag);

    ActionReturnUtil stopBuild(String jobName, String tenantName, String buildNum);

    ActionReturnUtil deleteBuild(String jobName, String tenantName, String buildNum);

    ActionReturnUtil credentialsValidate(String repositoryType, String repositoryUrl, String username, String password);

    ActionReturnUtil getBuildDetail(String tenantName, String jobName, String buildNum);

    void sendMessage(WebSocketSession session, String jobName, String buildNum);
}
