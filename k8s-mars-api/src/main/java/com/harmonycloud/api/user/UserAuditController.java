package com.harmonycloud.api.user;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.UserAuditSearch;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.UserAuditService;


@Controller
@RequestMapping("/system")
public class UserAuditController {


    @Autowired
    TenantService tenantService;

    @Autowired
    private UserAuditService userAuditService;

    @Autowired
    private HttpSession session;


    /**
     * 查找当前用户所有模块。管理员显示所有模块，普通成员显示当前模块.
     *
     * @return 模块列表
     * @throws Exception 异常
     */
    @RequestMapping(value = "/auditlogs/module", method = RequestMethod.GET)
    public @ResponseBody
    ActionReturnUtil getModulesByUser() throws Exception {

        //判断是否时超级用户,获取用户名称
        String userName = session.getAttribute("username").toString();

        return userAuditService.searchModule(userName);
    }

    /**
     * 查询指定模块下用户日志，如果为管理员显示所有用户该模块下日志，如果为普通成员显示当前成员日志.
     *
     * @param module 模块名称
     * @return 查询结果
     * @throws Exception 异常
     */
    /*@RequestMapping(value = "/auditlogs", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getAuditLogsByModule(String module) throws Exception {

        String userName = session.getAttribute("username").toString();
        //判断是否是admin
        String isAdmin = session.getAttribute("isAdmin").toString();

        ActionReturnUtil searchResults;


        return userAuditService.serachByModule(userName, module, "1".equals(isAdmin));
    }*/


    /**
     * 根据组合条件查找日志.
     *
     * @param startTime
     * @param endTime
     * @param keyWords
     * @param moduleName
     * @param tenantName
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/auditlogs", method = RequestMethod.GET)
    public ActionReturnUtil getAuditLogsByQuery(@RequestParam(value = "startTime") String startTime,
                                                @RequestParam(value = "endTime") String endTime,
                                                @RequestParam(value = "keyWords", required = false) String keyWords,
                                                @RequestParam(value = "moduleName", required = false) String moduleName,
                                                @RequestParam(value = "tenantName", required = false) String tenantName,
                                                @RequestParam(value = "pageNum", required = false) Integer pageNum,
                                                @RequestParam(value = "size") Integer size) throws Exception {
        UserAuditSearch userAuditSearch = new UserAuditSearch();
        userAuditSearch.setUser(session.getAttribute("username").toString());
        userAuditSearch.setStartTime(startTime);
        userAuditSearch.setEndTime(endTime);
        userAuditSearch.setKeyWords(keyWords);
        userAuditSearch.setModuleName(moduleName);
        userAuditSearch.setTenantName(tenantName);
        userAuditSearch.setPageNum(pageNum);
        userAuditSearch.setSize(size);
        return userAuditService.searchByQuery(userAuditSearch);
    }

    //方法转移到getAuditLogsByModule，若module为null或“”则是查找当前登录用户的操作日志

    /**
     * 查找当前登录用户的操作日志.
     *
     * @return 查询结果列表
     * @throws Exception 异常
     */
//    @RequestMapping(value = "/auditlogs", method = RequestMethod.GET)
//    public @ResponseBody ActionReturnUtil getAuditLogsByUser() throws Exception {
//
//        String userName = session.getAttribute("username").toString();
//        //判断是否是admin
//        String isAdmin = session.getAttribute("isAdmin").toString();
//
//        return userAuditService.serachAuditsByUser(userName, isAdmin.equals("1"));
//
//    }
    @ResponseBody
    @RequestMapping(value = "/auditlogs/count", method = RequestMethod.GET)
    public ActionReturnUtil getAuditLogsCount(@RequestParam(value = "startTime") String startTime,
                                              @RequestParam(value = "endTime") String endTime,
                                              @RequestParam(value = "keyWords", required = false) String keyWords,
                                              @RequestParam(value = "moduleName", required = false) String moduleName,
                                              @RequestParam(value = "tenantName", required = false) String tenantName,
                                              @RequestParam(value = "pageNum", required = false) Integer pageNum) throws Exception {
        UserAuditSearch userAuditSearch = new UserAuditSearch();
        userAuditSearch.setUser(session.getAttribute("username").toString());
        userAuditSearch.setStartTime(startTime);
        userAuditSearch.setEndTime(endTime);
        userAuditSearch.setKeyWords(keyWords);
        userAuditSearch.setModuleName(moduleName);
        userAuditSearch.setTenantName(tenantName);
        userAuditSearch.setPageNum(pageNum);
        return userAuditService.getAuditCount(userAuditSearch);
    }


}
