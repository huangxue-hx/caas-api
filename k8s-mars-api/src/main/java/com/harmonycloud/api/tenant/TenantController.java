package com.harmonycloud.api.tenant;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.MicroServiceCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dto.tenant.CDPUserDto;
import com.harmonycloud.dto.tenant.ClusterQuotaDto;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.dto.user.UserGroupDto;
import com.harmonycloud.service.platform.bean.NodeDto;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RolePrivilegeService;
import com.harmonycloud.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andy on 17-1-9.
 */
@Api(description = "租户相关查询及操作")
@RequestMapping("/tenants")
@Controller
public class TenantController {

    @Autowired
    TenantService tenantService;
    @Autowired
    NamespaceService namespaceService;
    @Autowired
    RolePrivilegeService rolePrivilegeService;
    @Autowired
    UserService userService;
    @Autowired
    private HttpSession session;

    public static final String CODE = "code";
    public static final String MSG = "msg";
    //新增成功
    public static final String SUCCESS = "108";
    //参数为空
    public static final String EMPTYPARAMITER = "101";
    public static final String EMPTYMSG = "参数为空";
    public static final String SUCCESSMSG = "成功";

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 切换租户在session中设置当前租户的信息
     *
     * @param tenantId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{tenantId}/switchTenant", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil switchTenant( @PathVariable("tenantId") String tenantId) throws Exception {
        Map<String,Object> result = this.tenantService.switchTenant(tenantId);
        return ActionReturnUtil.returnSuccessWithData(result);
    }

    /**
     * 根据租户id查询租户详情
     * 
     * @param tenantId
     * @return
     */
    @RequestMapping(value = "/{tenantId}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTenantDetail(@PathVariable(value = "tenantId") String tenantId) throws Exception {

        TenantDto tenantDto = tenantService.getTenantDetail(tenantId);
        return ActionReturnUtil.returnSuccessWithData(tenantDto);
    }

    /**
     * 创建租户
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createTenant(@ModelAttribute TenantDto tenantDto) throws Exception {
//        logger.info("创建租户");
        //租户名空值判断
        if (StringUtils.isAnyEmpty(tenantDto.getTenantName(),tenantDto.getAliasName())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //策略空值判断
        if(tenantDto.getStrategy() == null){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        tenantService.createTenant(tenantDto);
        return ActionReturnUtil.returnSuccess();

    }

    /**
     * 根据租户id删除租户
     * 
     * @param tenantId
     * @return
     */
    @RequestMapping(value = "/{tenantId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteTenantByTenantId(@PathVariable(value = "tenantId") String tenantId) throws Exception {
//        logger.info("删除租户");
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        tenantService.deleteTenantByTenantId(tenantId);
        return ActionReturnUtil.returnSuccess();
    }
    /**
     * 根据租户id修改租户配额
     * @param tenantId
     * @param tenantDto
     * @return
     */
    @RequestMapping(value = "/{tenantId}", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateTenant(@PathVariable(value = "tenantId") String tenantId,
                                         @ModelAttribute TenantDto tenantDto) throws Exception {
        //空值判断
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        tenantDto.setTenantId(tenantId);
        List<ClusterQuotaDto> clusterQuota = tenantDto.getClusterQuota();
        if (CollectionUtils.isEmpty(clusterQuota)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        tenantService.updateTenant(tenantDto);
        return ActionReturnUtil.returnSuccess();
    }
    @RequestMapping(value = "/{tenantId}/privateNodeList", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTenantPrivateNodeList(@PathVariable(value = "tenantId") String tenantId,
                                                     String namespace,
                                                     String clusterId) throws Exception {
        if (StringUtils.isBlank(namespace) && StringUtils.isBlank(clusterId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        List<NodeDto> nodeList = tenantService.getTenantPrivateNodeList(tenantId,namespace,clusterId);
        return ActionReturnUtil.returnSuccessWithData(nodeList);
    }
    /**
     * 查询该tenant下的所有tm用户
     * 
     * @return
     */
    @RequestMapping(value = "{tenantId}/tms", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantTm(@PathVariable(value = "tenantId") String tenantId) throws Exception {
//        logger.info("查询tenant下的所有tm用户");
        List<UserRoleRelationship> userRoleRelationships = this.tenantService.listTenantTm(tenantId);
        List<String> list = new ArrayList<>();
        if (!userRoleRelationships.isEmpty()){
            for (UserRoleRelationship userRoleRelationship : userRoleRelationships){
                list.add(userRoleRelationship.getUsername());
            }
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    /**
     * 向tenant添加租户管理员
     * 
     * @return
     */
    @RequestMapping(value = "/{tenantId}/tms", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createTm(@PathVariable(value = "tenantId") String tenantId,
                                     @ModelAttribute TenantDto tenantDto)
            throws Exception {
        //租户id空值判断
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
//        logger.info("向tenant增加租户管理员");
        List<String> tmList = tenantDto.getTmList();
        //用户名空值判断
        if (CollectionUtils.isEmpty(tmList)){
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.USERNAME_BLANK);
        }
        this.tenantService.createTm(tenantId, tmList);
        return ActionReturnUtil.returnSuccess();
    }
    /**
     * 向tenant移除租户管理员
     * @param tenantId
     * @param username
     * @return
     */
    @RequestMapping(value = "/{tenantId}/tms/{username}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteTm(@PathVariable(value = "tenantId") String tenantId,
                                     @PathVariable(value = "username") String username) throws Exception {
//        logger.info("向tenant移除租户管理员");
        if (StringUtils.isAnyBlank(tenantId,username)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        this.tenantService.deleteTm(tenantId, username);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 列出tenan下面的资源使用情况
     * 
     * @param tenantId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{tenantId}/quota", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantQuota(@PathVariable(value = "tenantId") String tenantId) throws Exception {

//        logger.info("列出tenan下面的资源使用情况");
        if (StringUtils.isBlank(tenantId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        return ActionReturnUtil.returnSuccessWithData(this.tenantService.listTenantQuota(tenantId));

    }
    /**
     * 获取namespace下pod创建时候的nodeSelector的标签
     * 
     * @param tenantId
     * @param namespaceName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{tenantId}/namespace/{namespaceName}/pod/label", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getPodLabel(@PathVariable(value = "tenantId") String tenantId,
                                        @PathVariable(value = "namespaceName") String namespaceName) throws Exception {

//        logger.info("获取namespace下pod创建时候的nodeSelector的标签");
        if (StringUtils.isAnyBlank(tenantId,namespaceName)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String privatePartitionLabel = this.namespaceService.getPrivatePartitionLabel(tenantId, namespaceName);
        return ActionReturnUtil.returnSuccessWithData(privatePartitionLabel);

    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantList(String username) throws Exception {
        if (StringUtils.isBlank(username)){
            List<TenantDto> tenantDtos = tenantService.tenantList();
            return ActionReturnUtil.returnSuccessWithData(tenantDtos);
        }else {
            List<TenantBinding> tenantBindings = tenantService.listTenantsByUserName(username);
            return ActionReturnUtil.returnSuccessWithData(tenantBindings);
        }

    }
    @RequestMapping(value = "/{tenantId}/members", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantMember(@PathVariable(value = "tenantId") String tenantId) throws Exception {
        List<User> users = tenantService.listTenantMember(tenantId);
        return ActionReturnUtil.returnSuccessWithData(users);
    }
    @RequestMapping(value = "/{tenantId}/members",method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateTenantMember(@PathVariable(value = "tenantId") String tenantId,@ModelAttribute UserGroupDto usergroupdto) throws Exception {
        List<String> addusers = usergroupdto.getAddusers();
        List<String> delusers = usergroupdto.getDelusers();
        tenantService.updateTenantMember(tenantId,addusers,delusers);
        return ActionReturnUtil.returnSuccess();
    }
    /**
     * （石化盈科对接）CDP项目集成容器平台项目导入接口(如果存在就修改)
     * @param tenantDto
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addProject",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil importCdsSystem(@RequestBody TenantDto tenantDto) throws Exception {
        //空值判断
        if (StringUtils.isAnyBlank(tenantDto.getSysId(),
                tenantDto.getSysCode(),
                tenantDto.getSysName(),
                tenantDto.getCategory())){
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, null, null);
        }
        try {
            this.tenantService.importCdsSystem(tenantDto);
        }catch (Exception e){
            String errorMessage = null;
            if (e instanceof MarsRuntimeException){
                errorMessage = ((MarsRuntimeException) e).getErrorMessage();
            }else {
                errorMessage = e.getMessage();
            }
            logger.error(errorMessage,e);
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PROJECT_SYNC_FAILURE, errorMessage, null);
        }

        return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, null, null);
    }

    /**
     * CDP添加人员或者更新用户信息（根据用户账号进行查重判断，当用户账号已存在时，根据用户账号进行更新操作）
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addUser",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil importCdsUserAccount(@RequestBody CDPUserDto cdpUserDto) throws Exception {
        Map<String, Object> result = new HashMap<>();
        //空值判断
        if (StringUtils.isAnyBlank(cdpUserDto.getUserAccount(),
                cdpUserDto.getUserName(),
//                cdpUserDto.getTel(),
                cdpUserDto.getUserId())){
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, null, null);
        }
        try {
            this.tenantService.importCdsUserAccount(cdpUserDto);
        }catch (Exception e){
            String errorMessage = null;
            if (e instanceof MarsRuntimeException){
                errorMessage = ((MarsRuntimeException) e).getErrorMessage();
            }else {
                errorMessage = e.getMessage();
            }
            logger.error(errorMessage,e);
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.USER_SYNC_FAILURE, errorMessage, null);
        }
        return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, null, null);
    }

    /**
     * CDP用于新增项目与用户关系，对应一个项目，一个人一个角色一条记录
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/project/addUser",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil importCdsUserRelationship(@RequestBody CDPUserDto cdpUserDto) throws Exception {
        //空值判断
        if (StringUtils.isAnyBlank(cdpUserDto.getSysId(),
                cdpUserDto.getUserAccount(),
                cdpUserDto.getRoleCode(),
                cdpUserDto.getCategory())){
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, null, null);
        }
        try {
            this.tenantService.importCdsUserRelationship(cdpUserDto);
        }catch (Exception e){
            String errorMessage = null;
            if (e instanceof MarsRuntimeException){
                errorMessage = ((MarsRuntimeException) e).getErrorMessage();
            }else {
                errorMessage = e.getMessage();
            }
            logger.error(errorMessage,e);
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.USER_RELATIONSHIP_ADD_FAILURE, errorMessage, null);
        }
        return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, null, null);
    }

    /**
     * CDP用于删除项目与用户关系，对应一个项目，一个人一个角色一条记录
     * @param cdpUserDto
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/project/removeUser",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil removeCdsUserRelationship(@RequestBody CDPUserDto cdpUserDto) throws Exception {
        //空值判断
        if (StringUtils.isAnyBlank(cdpUserDto.getSysId(),
                cdpUserDto.getUserAccount(),
                cdpUserDto.getRoleCode(),
                cdpUserDto.getCategory())){
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.PARAMS_NULL, null, null);
        }
        try {
            this.tenantService.removeCdsUserRelationship(cdpUserDto);
        }catch (Exception e){
            String errorMessage = null;
            if (e instanceof MarsRuntimeException){
                errorMessage = ((MarsRuntimeException) e).getErrorMessage();
            }else {
                errorMessage = e.getMessage();
            }
            logger.error(errorMessage,e);
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.USER_RELATIONSHIP_REMOVE_FAILURE, errorMessage, null);
        }
        return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SUCCESS, null, null);
    }
    /**
     * 获取租户列表（操作审计使用）
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/audit",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listTenantListForAudit() throws Exception {
        String username = this.userService.getCurrentUsername();
        List<TenantBinding> tenantBindings = tenantService.tenantListByUsernameInner(username);
        return ActionReturnUtil.returnSuccessWithData(tenantBindings);
    }

    /**
     * 修改租户策略
     * @param tenantId
     * @param strategy
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "修改租户策略")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户Id", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "strategy", value = "策略", paramType = "query", dataType = "Integer")})
    @RequestMapping(value="/{tenantId}/strategy",method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateTenantStrategy(@PathVariable("tenantId") String tenantId,
                                                 @RequestParam(value = "strategy") Integer strategy) throws Exception {

        //空值判断
        if (StringUtils.isAnyEmpty(tenantId,strategy.toString())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        tenantService.updateTenantStrategy(tenantId,strategy);
        return ActionReturnUtil.returnSuccess();
    }
}
