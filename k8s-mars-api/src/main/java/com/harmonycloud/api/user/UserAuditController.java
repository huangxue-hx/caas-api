package com.harmonycloud.api.user;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.UserAuditSearch;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.UserAuditService;


@Controller
@RequestMapping("/userAudit")
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
    @RequestMapping(value = "/modules/searchModulesByUser", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getModulesByUser() throws Exception {

        //判断是否时超级用户,获取用户名称
        String userName = session.getAttribute("username").toString();
        //判断是否是admin
        String isAdmin = session.getAttribute("isAdmin").toString();

        return userAuditService.serachByUserName(userName, isAdmin.equals("1"));
    }

    /**
     * 查询指定模块下用户日志，如果为管理员显示所有用户该模块下日志，如果为普通成员显示当前成员日志.
     *
     * @param module 模块名称
     * @return 查询结果
     * @throws Exception 异常
     */
    @RequestMapping(value = "/search/searchAllByModule", method = RequestMethod.GET)
    public @ResponseBody
    ActionReturnUtil getAuditLogsByModule(String module) throws Exception {

        String userName = session.getAttribute("username").toString();
        //判断是否是admin
        String isAdmin = session.getAttribute("isAdmin").toString();

        ActionReturnUtil searchResults;


        return userAuditService.serachByModule(userName, module, "1".equals(isAdmin));
    }


    /**
     * 根据组合条件查找当前用户日志.
     *
     * @param userAuditSearch 查询组合条件
     * @return 查询结果列表
     * @throws Exception 异常
     */
    @RequestMapping(value = "/search/searchAllByQuery", method = RequestMethod.POST)
    public @ResponseBody ActionReturnUtil getAuditLogsByQuery(@ModelAttribute UserAuditSearch userAuditSearch) throws Exception {
        if (userAuditSearch == null) {
            return null;
        }
        String isAdmin = session.getAttribute("isAdmin").toString();
        userAuditSearch.setUser(session.getAttribute("username").toString());
        System.out.println(session.getAttribute(""));
        List<String> userList = new ArrayList<>();
        if(StringUtils.isNotBlank(userAuditSearch.getTenantName())&&!"all".equals(userAuditSearch.getTenantName())){
            userList = tenantService.findByTenantName(userAuditSearch.getTenantName());
        }
        if (userList != null && userList.size() > 0) {
            userAuditSearch.setUserList(userList);
        }
        return userAuditService.serachByQuery(userAuditSearch, isAdmin.equals("1"));
    }

    /**
     * 查找当前登录用户的操作日志.
     *
     * @return 查询结果列表
     * @throws Exception 异常
     */
    @RequestMapping(value = "/search/searchAuditsByUser", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getAuditLogsByUser() throws Exception {

        String userName = session.getAttribute("username").toString();
        //判断是否是admin
        String isAdmin = session.getAttribute("isAdmin").toString();

        return userAuditService.serachAuditsByUser(userName, isAdmin.equals("1"));

    }
    
    
    @RequestMapping(value = "/search/count", method = RequestMethod.POST)
    public @ResponseBody ActionReturnUtil getAuditLogsCount(@ModelAttribute UserAuditSearch userAuditSearch) throws Exception {
        if (userAuditSearch == null || userAuditSearch.getSize() == null) {
            return ActionReturnUtil.returnErrorWithMsg("pageSize参数不能为空");
        }
        String isAdmin = session.getAttribute("isAdmin").toString();
        userAuditSearch.setUser(session.getAttribute("username").toString());
        System.out.println(session.getAttribute(""));
        List<String> userList = new ArrayList<>();
        if(StringUtils.isNotBlank(userAuditSearch.getTenantName())&&!"all".equals(userAuditSearch.getTenantName())){
            userList = tenantService.findByTenantName(userAuditSearch.getTenantName());
        }
        if (userList != null && userList.size() > 0) {
            userAuditSearch.setUserList(userList);
        }
        return userAuditService.getAuditCount(userAuditSearch, isAdmin.equals("1"));
    }



}
