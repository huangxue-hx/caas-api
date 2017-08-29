package com.harmonycloud.api.tenant;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.tenant.PersistentVolumeDto;
import com.harmonycloud.service.tenant.PersistentVolumeService;



@Controller
public class PersistentVolumeController {

    @Autowired
    PersistentVolumeService persistentvolumeService;

    @Autowired
    HttpSession session;

    
    /**
     * 获取存储类型及服务提供地址
     * 
     * @param 
     * @return
     */
    @RequestMapping(value = "/pv/listProvider", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listProvider() throws Exception {

        return persistentvolumeService.listProvider();
    }
    /**
     * 创建pv
     * @param persistentVolume
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/pv/createPv", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createPv(PersistentVolumeDto persistentVolume) throws Exception {
        //初始化判断
        if(StringUtils.isBlank(persistentVolume.getName())||StringUtils.isBlank(persistentVolume.getTenantid())||StringUtils.isBlank(persistentVolume.getCapacity())){
            ActionReturnUtil.returnErrorWithMsg("pv名字,租户id或者容量不能为空！");
        }
        return persistentvolumeService.createPv(persistentVolume);
    }
    /**
     * 根据tenantid查询pv列表 当tenantid为空的时候查询所有的pv
     * @param tenantid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/pv/listPvBytenantid", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listPvBytenant(@RequestParam(value = "tenantid", required = true) String tenantid) throws Exception {
//        String username = (String)this.session.getAttribute("username");
        if(StringUtils.isBlank(tenantid)){
            return persistentvolumeService.listAllPv(null);
        }else{
            return persistentvolumeService.listPvBytenant(tenantid);
        }
        
    }
    /**
     * 根据name删除pv
     * @param name
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/pv/deletePvByName", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deletePvByName(@RequestParam(value = "name", required = true) String name) throws Exception {

        if(StringUtils.isBlank(name)){
            return ActionReturnUtil.returnErrorWithMsg("pv名字不能为空");
        }
        return persistentvolumeService.deletePvByName(name);
    }
    /**
     * 根据name修改pv
     * @param name
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/pv/updatePvByName", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updatePvByName(@RequestParam(value = "name", required = true) String name,@RequestParam(value = "capacity", required = true) String capacity,Boolean readOnly,Boolean multiple) throws Exception {

        if(StringUtils.isBlank(name)||StringUtils.isBlank(capacity)){
            return ActionReturnUtil.returnErrorWithMsg("pv名字,capacity 不能为空");
        }
        return persistentvolumeService.updatePvByName(name,capacity,readOnly,multiple);
    }
    /**
     * 根据name查询pv详情
     * @param name
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/pv/getPVByName", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil getPVByName(@RequestParam(value = "name", required = true) String name) throws Exception {

        if(StringUtils.isBlank(name)){
            return ActionReturnUtil.returnErrorWithMsg("pv名字 不能为空");
        }
        return persistentvolumeService.getPVByName(name);
    }
    
    /**
     * 根据name回收数据pv
     * @param name
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/pv/recyclePvByName", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil recyclePvByName(@RequestParam(value = "name", required = true) String name) throws Exception {

        if(StringUtils.isBlank(name)){
            return ActionReturnUtil.returnErrorWithMsg("pv名字不能为空");
        }
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        return persistentvolumeService.recyclePvByName(name, cluster);
    }
}
