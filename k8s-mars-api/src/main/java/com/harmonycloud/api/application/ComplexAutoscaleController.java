package com.harmonycloud.api.application;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.service.application.AutoScaleService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;


/**
 * Created by root on 5/22/17.
 */
@RequestMapping("/complexautoscale")
@Controller
public class ComplexAutoscaleController {

    @Autowired
    DeploymentsService dpService;
    @Autowired
    AutoScaleService autoScaleService;

    @Autowired
    EsService esService;
    @Autowired
    HttpSession session;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 设置自动伸缩
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil createAutoScale(@RequestBody AutoScaleDto autoScaleDto) {

        try {
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            boolean result = autoScaleService.create(autoScaleDto, cluster);
            if(result){
                return ActionReturnUtil.returnSuccess();
            }else{
                return ActionReturnUtil.returnError();
            }
        } catch (MarsRuntimeException mre){
            logger.error("创建自动伸缩失败，autoScaleDto:{}", JSONObject.toJSONString(autoScaleDto), mre);
            return ActionReturnUtil.returnErrorWithData(mre.getMessage());
        }catch (Exception e) {
            logger.error("创建自动伸缩失败，autoScaleDto:{}", JSONObject.toJSONString(autoScaleDto), e);
            return ActionReturnUtil.returnError();
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateAutoScaleApp(@RequestBody AutoScaleDto autoScaleDto) {
        try {
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            boolean result = autoScaleService.update(autoScaleDto, cluster);
            if(result){
                return ActionReturnUtil.returnSuccess();
            }else{
                return ActionReturnUtil.returnError();
            }
        }catch (MarsRuntimeException mre){
            logger.error("修改自动伸缩失败，autoScaleDto:{}", JSONObject.toJSONString(autoScaleDto), mre);
            return ActionReturnUtil.returnErrorWithData(mre.getMessage());
        } catch (Exception e) {
            logger.error("修改自动伸缩失败， autoScaleDto:{}", JSONObject.toJSONString(autoScaleDto), e);
            return ActionReturnUtil.returnError();
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteAutoScale(@RequestParam(value = "deploymentName") String deploymentName,
                                            @RequestParam(value = "namespace") String namespace) throws Exception {
        try {
            logger.info("删除应用自动伸缩，deploymentName:{},namespace:{}",deploymentName,namespace);
            Cluster cluster = (Cluster) session.getAttribute("currentCluster");
            boolean result = autoScaleService.delete(deploymentName, namespace, cluster);
            if(result){
                return ActionReturnUtil.returnSuccess();
            }else{
                return ActionReturnUtil.returnError();
            }
        }catch (Exception e) {
            logger.error("删除自动伸缩失败， deploymentName:{}", deploymentName, e);
            return ActionReturnUtil.returnError();
        }
    }

}
