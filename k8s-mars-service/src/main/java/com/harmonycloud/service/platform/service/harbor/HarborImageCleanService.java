package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.harbor.bean.ImageCleanRule;

import java.util.List;

/**
 * 镜像清理接口
 *  created by zackchen 2017/12/14
 */

public interface HarborImageCleanService {

    void cleanRepo() throws Exception;
    ActionReturnUtil setCleanRule(ImageCleanRule rule, int flag) throws Exception;
    List<ImageCleanRule> listByIds(List<Integer> repositoryIds);

    /**
     * 对某个harbor做镜像垃圾文件清理
     * @param harborHost
     * @return
     * @throws Exception
     */
    boolean cleanImageGarbage(String harborHost) throws Exception;

    /**
     * 判断某个harbor是否在镜像垃圾收集
     * @param harborHost
     * @return
     * @throws Exception
     */
    boolean isHarborInGc(String harborHost) throws Exception;

    /**
     * 删除集群时将该集群的镜像仓库的清理规则删除
     * @return
     * @throws Exception
     */
    void deleteClusterCleanRule(String clusterId) throws Exception;
}
