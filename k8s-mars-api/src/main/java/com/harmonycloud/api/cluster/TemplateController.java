package com.harmonycloud.api.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.TemplateTPRDto;
import com.harmonycloud.service.cluster.ClusterTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/clusterstemplates")
@Controller
public class TemplateController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ClusterTemplateService clusterTemplateService;

    /**
     * 获取 集群模板 列表
     * @return ActionReturnUtil
     * @throws Exception failed to list
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listTemplate() throws Exception{
            logger.info("list clustersTemplate");
            return clusterTemplateService.listClusterTemplates();
    }

    /**
     * 根据 集群模板名字 获取该集群模板
     * @param template 集群模板名字
     * @return ActionReturnUtil
     * @throws Exception failed to get
     */
    @ResponseBody
    @RequestMapping(value = "/{template}", method = RequestMethod.GET)
    public ActionReturnUtil getTemplate(@PathVariable String template) throws Exception {
        logger.info("get clustersTemplate, name:{}",template);
        return clusterTemplateService.getClusterTemplate(template);
    }


    /**
     * 根据集群模板名字， 删除集群模板
     * @param template  集群模板名字
     * @return ActionReturnUtil
     * @throws Exception failed to delete
     */
    @ResponseBody
    @RequestMapping(value = "/{template}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteTemplate(@PathVariable String template) throws Exception {
        logger.info("delete clustersTemplate, name:{}",template);
        return clusterTemplateService.deleteClusterTemplate(template);
    }

    /**
     * 添加集群模板
     * @param templateTPRDto body data
     * @return ActionReturnUtil
     * @throws Exception Failed to add clustersTemplate
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil addTemplate(@RequestBody TemplateTPRDto templateTPRDto) throws Exception {
        logger.info("add clustersTemplate");
        return clusterTemplateService.addClusterTemplate(templateTPRDto);

    }

    /**
     *  更新集群模板数据
     * @param template 集群模板名字
     * @param templateTPRDto body data
     * @return ActionReturnUtil
     * @throws Exception failed to update
     */
    @ResponseBody
    @RequestMapping(value = "/{template}",method = RequestMethod.PUT)
    public ActionReturnUtil updateTemplate(@PathVariable String template,@RequestBody TemplateTPRDto templateTPRDto) throws Exception {
        logger.info("update clustersTemplate");
        return clusterTemplateService.updateClusterTemplate(template,templateTPRDto);
    }
}
