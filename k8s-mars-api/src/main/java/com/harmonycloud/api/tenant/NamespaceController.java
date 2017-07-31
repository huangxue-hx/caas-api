package com.harmonycloud.api.tenant;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.service.tenant.NamespaceService;

import javax.servlet.http.HttpSession;


@Controller
public class NamespaceController {

    @Autowired
    NamespaceService namespaceService;

    @Autowired

    HttpSession session;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建namespace
     * 
     * @param namespaceDto
     * @return
     */
    @RequestMapping(value = "/namespace", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createNamespace(@RequestBody NamespaceDto namespaceDto) throws Exception {

        logger.info("创建namespace");
        return namespaceService.createNamespace(namespaceDto);
    }

    /**
     * 编辑namespace下的resource quato
     * 
     * @return
     */
    @RequestMapping(value = "/namespace/update", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil updateNamespace(NamespaceDto namespaceDto) throws Exception {

        logger.info("编辑resource quato");
        return namespaceService.updateNamespace(namespaceDto);

    }

    /**
     * 删除namespace
     * 
     * @param tenantid
     * @param name
     * @return
     */
    @RequestMapping(value = "/namespace", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteNamespace(@RequestParam(value = "tenantid", required = true) String tenantid, @RequestParam(value = "name", required = true) String name)
            throws Exception {

        logger.info("删除namespace");
        return namespaceService.deleteNamespace(tenantid, name);

    }

    /**
     * 查询namespace列表
     * 
     * @param tenantid
     * @param tenantname
     * @return
     */
    @RequestMapping(value = "/namespace", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNamespaceList(@RequestParam(value = "tenantid", required = true) String tenantid,
            @RequestParam(value = "tenantname", required = true) String tenantname) throws Exception {

        logger.info("查询namespace列表");
        return namespaceService.getNamespaceList(tenantid, tenantname);

    }
    /**
     * 查询namespace列表
     * 
     * @param tenantid
     * @param tenantname
     * @return
     */
    @RequestMapping(value = "/namespace/listByTenantid", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNamespaceListByTenantid(@RequestParam(value = "tenantid", required = true) String tenantid) throws Exception {

        return namespaceService.getNamespaceListByTenantid(tenantid);

    }
    /**
     * 查询namespace详情
     * 
     * @param tenantid
     * @param name
     * @return
     */
    @RequestMapping(value = "/namespace/detail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNamespaceDetail( @RequestParam(value = "name", required = true) String name, @RequestParam(value = "tenantid", required = true) String tenantid)
            throws Exception {

        logger.info("查询namespace详情");
        return namespaceService.getNamespaceDetail(name,tenantid);

    }

    /**
     * 查询namespace配额
     *
     * @param tenantid
     * @param name
     * @return
     */
    @RequestMapping(value = "/namespace/quota", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNamespaceQuota( @RequestParam(value = "namespace", required = true) String namespace)
            throws Exception {

        logger.info("查询namespacequota");
        String userName = (String) session.getAttribute("username");
        if(userName == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return ActionReturnUtil.returnSuccessWithData(namespaceService.getNamespaceQuota(namespace, cluster));

    }
}
