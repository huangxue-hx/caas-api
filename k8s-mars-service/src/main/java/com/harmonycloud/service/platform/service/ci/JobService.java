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

    ActionReturnUtil getJobList(String tenantName, String name);

    ActionReturnUtil getJobDetail(Integer id) throws Exception;

    ActionReturnUtil build(Integer id) throws Exception;

    ActionReturnUtil stopBuild(String jobName, String tenantName, String buildNum);

    ActionReturnUtil deleteBuild(Integer id, String buildNum) throws Exception;

    ActionReturnUtil credentialsValidate(String repositoryType, String repositoryUrl, String username, String password);

    ActionReturnUtil getBuildList(Integer id) throws Exception;

    ActionReturnUtil getNotification(Integer id) throws Exception;

    ActionReturnUtil updateNotification(JobDto job) throws Exception;

    ActionReturnUtil getTrigger(Integer id) throws Exception;

    ActionReturnUtil updateTrigger(JobDto jobDto);

    void postBuild(Integer id, Integer buildNum);

    void stageSync(Integer id, Integer buildNum);

    void deploy(Integer stageId, Integer buildNum) throws Exception;

    void jobStatusWS(WebSocketSession session, Integer id);

    void getJobLogWS(WebSocketSession session, Integer id, String buildNum);

    ActionReturnUtil getYaml(Integer id);

    void preBuild(Integer id, Integer buildNum, String dateTime);
}
