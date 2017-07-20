package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dto.cicd.JobDto;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by anson on 17/5/31.
 */
public interface JobService {

    ActionReturnUtil createJob(JobDto job) throws Exception;

    ActionReturnUtil updateJob(Job job);

    ActionReturnUtil deleteJob(Integer id) throws Exception;

    ActionReturnUtil nameValidate(String jobName, String tenantName);

    ActionReturnUtil getJobList(String tenantName);

    ActionReturnUtil getJobDetail(Integer id);

    ActionReturnUtil build(Integer id);

    ActionReturnUtil stopBuild(String jobName, String tenantName, String buildNum);

    ActionReturnUtil deleteBuild(String jobName, String tenantName, String buildNum);

    ActionReturnUtil credentialsValidate(String repositoryType, String repositoryUrl, String username, String password);

    ActionReturnUtil getBuildDetail(String tenantName, String jobName, String buildNum);

    void sendMessage(WebSocketSession session, String jobName, String buildNum);


    ActionReturnUtil getNotification(Integer id) throws Exception;

    ActionReturnUtil updateNotification(JobDto job);

    ActionReturnUtil getTrigger(Integer id) throws Exception;

    ActionReturnUtil updateTrigger(Job jobDto);

    void postBuild(Integer id, Integer buildNum);
}
