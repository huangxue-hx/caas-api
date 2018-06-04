package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.ci.bean.JobBuild;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.cicd.JobDto;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * Created by anson on 17/5/31.
 */
public interface JobService {

    Integer createJob(JobDto job) throws Exception;

    void deleteJob(Integer id) throws Exception;

    void validateName(String jobName, String projectId, String clusterId) throws Exception;

    List getJobList(String projectId, String clusterId, String type, String name) throws Exception;

    ActionReturnUtil getJobDetail(Integer id) throws Exception;

    Integer build(Integer id, List<Map<String, Object>> parameters, String image, String tag) throws Exception;

    void stopBuild(Integer jobId, String buildNum) throws Exception;

    ActionReturnUtil deleteBuild(Integer id, String buildNum) throws Exception;

    ActionReturnUtil validateCredential(String repositoryType, String repositoryUrl, String username, String password);

    ActionReturnUtil getBuildList(Integer id, Integer pageSize, Integer page) throws Exception;

    ActionReturnUtil getNotification(Integer id) throws Exception;

    ActionReturnUtil updateNotification(JobDto job) throws Exception;

    void postBuild(Integer id, Integer buildNum);

    void stageSync(Integer id, Integer buildNum);

    void deploy(Integer stageId, Integer buildNum) throws Exception;

    void jobStatusWS(WebSocketSession session, Integer id);

    void getJobLogWS(WebSocketSession session, Integer id, String buildNum);

    String getYaml(Integer id) throws Exception;

    void preBuild(Integer id, Integer buildNum, String dateTime) throws Exception;

    String getJobLog(Integer id, Integer buildNum) throws Exception;

    void getJobListWS(WebSocketSession session, String projectId, String clusterId);

    void destroyCicdPod(Cluster cluster) throws Exception;

    List listDeployImage(Integer jobId);

    /**
     * 根据id获取流水线
     * @param id
     * @return
     * @throws Exception
     */
    Job getJobById(Integer id) throws Exception;

    /**
     * 根据uuid获取流水线
     * @param uuid
     * @return
     */
    Job getJobByUuid(String uuid) throws Exception;

    void runStage(Integer stageId, Integer buildNum) throws Exception;

    int deleteByClusterId(String clusterId);

    List getStageBuildResult(Integer id, Integer buildNum, String status) throws Exception;

    ActionReturnUtil updateJenkinsJob(Integer id) throws Exception;

    /**
     * 根据项目删除流水线
     * @param projectId
     * @throws Exception
     */
    void deletePipelineByProject(String projectId) throws Exception;

    /**
     * 流水线重命名
     * @param jobId
     * @param newName
     */
    void rename(Integer jobId, String newName) throws Exception;

    /**
     * 修改流水线信息
     * @param jobDto
     */
    void updateJob(JobDto jobDto) throws Exception;

    void deleteBuildResult() throws Exception;

    JobBuild syncJobStatus(Job job, Integer buildNum) throws Exception;
}
