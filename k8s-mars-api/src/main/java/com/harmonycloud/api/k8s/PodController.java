package com.harmonycloud.api.k8s;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.NodeLabel;
import com.harmonycloud.service.platform.bean.PodDto;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.platform.service.PodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pod")
public class PodController {

    @Autowired
    private PodService podService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private HttpSession session;


    /**
     * pod 列表
     * 
     * @return
     */
    @RequestMapping(value = "/listLike", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listLike(@RequestParam(value = "clusterId", required = true) String clusterId, String podName, String namespace) throws Exception {

        Cluster cluster = null;
        if(null != clusterId && !"".equals(clusterId)) {
            cluster = this.clusterService.findClusterById(clusterId);
        } else {
            cluster = (Cluster) session.getAttribute("currentCluster");
        }

        List<PodDto> podList = podService.getPodListByNamespace(cluster, namespace);

        if (podList == null) {
            throw new Exception("Faild to get pod list.");
        }

        List<PodDto> resultList = new ArrayList<PodDto>();
        for (PodDto pod : podList) {
            String[] pArray = podName.split(",");
            for (String p : pArray) {
                if (pod.getName().startsWith(p)) {
                    resultList.add(pod);
                }
            }

        }

        return ActionReturnUtil.returnSuccessWithData(resultList);

    }


}
