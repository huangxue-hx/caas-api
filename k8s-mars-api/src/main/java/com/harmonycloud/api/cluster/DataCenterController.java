package com.harmonycloud.api.cluster;

/**
 * Created by Tony on 17/12/18.
 */

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.DataCenterDto;
import com.harmonycloud.service.application.DataCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/datacenters")
@Controller
public class DataCenterController {

    @Autowired
    DataCenterService dataCenterService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *  获取数据中心列表
     * @return ActionReturnUtil
     * @throws Exception failed to list data
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listDataCenters() throws Exception {
//        logger.info("]get dataCenterlis ");
        return dataCenterService.listDataCenter();
    }

    /**
     * 添加数据中心
     * @param dataCenterDto 数据中心Dto
     * @return ActionReturnUtil
     * @throws Exception failed  to add
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil addDataCenter(@RequestBody DataCenterDto dataCenterDto) throws Exception {
        logger.info("add dataCenter");
        return dataCenterService.addDataCenter(dataCenterDto);
    }

    /**
     * 获取某一数据中心
     * @param datacenter 为数据中心名
     * @return ActionReturnUtil
     * @throws Exception  failed to get
     */
    @ResponseBody
    @RequestMapping(value = "/{datacenter}", method = RequestMethod.GET)
    public ActionReturnUtil getDataCenter(@PathVariable("datacenter") String datacenter) throws Exception {
//        logger.info("get dataCenter, name:{}",datacenter);
        return dataCenterService.getDataCenter(datacenter);
    }

    /**
     * 删除指定的数据中心
     * @param datacenter 为数据中心名
     * @return ActionReturnUtil
     * @throws Exception failed to delete
     */
    @ResponseBody
    @RequestMapping(value = "/{datacenter}",method = RequestMethod.DELETE)
    public ActionReturnUtil deleteDataCenter(@PathVariable("datacenter") String datacenter) throws Exception {
        logger.info("delete dataCenter, name:{}",datacenter);
        return dataCenterService.deleteDataCenter(datacenter);
    }

    /**
     * 更新指定的数据中心
     * @param datacenter
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{datacenter}", method = RequestMethod.PUT)
    public ActionReturnUtil updateDateCneter(@PathVariable("datacenter") String datacenter, @RequestBody DataCenterDto dataCenterDto) throws Exception {
        logger.info("update dateCenter, name:{}", datacenter);
        return  dataCenterService.updateDataCenter(datacenter, dataCenterDto.getAnnotations());
    }
}
