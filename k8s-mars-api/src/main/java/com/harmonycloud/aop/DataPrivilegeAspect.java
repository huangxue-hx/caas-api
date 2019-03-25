package com.harmonycloud.aop;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dao.dataprivilege.bean.DataResourceUrl;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataResourceUrlService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *  数据权限拦截AOP
 * @author zyj
 *
 */
@Aspect
@Order(CommonConstant.NUM_TWO)
@Component
public class DataPrivilegeAspect {
    private static final String LEFT_BRACKET = "{";
    private static final String RIGHT_BRACKET = "}";
    private static final String BACKSLASH = "\\";
    private static final String STAR = "*";
    private static final String POINTSTAR = ".*";
    private static final int NUMONE = 1;
    private static final String GET = "GET";
    private static final int RO = 1;
    private static final int RW = 2;
    private static final int DATA_RESOURCE_TYPE_APP = 1;
    private static final int DATA_RESOURCE_TYPE_DEPLOY = 2;

    @Autowired
    private DataResourceUrlService dataResourceUrlService;
    @Autowired
    private UserService userService;
    @Autowired
    private DataPrivilegeGroupMappingService dataPrivilegeGroupMappingService;
    @Autowired
    private DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static Map<String, DataResourceUrl> dataResourceUrlMap = null;

    @Pointcut("execution(public * com.harmonycloud.api.application.*.*(..)) || execution(public * com.harmonycloud.api.ci.*.*(..))")
    public void dataPrivilegeAspect() {
    }

    @SuppressWarnings("unchecked")
    @Before("dataPrivilegeAspect()")
    public void doBefore(JoinPoint joinPoint) throws Exception{

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession();
        //如果是管理员，则不进行权限检测
        Integer currentRoleId = userService.getCurrentRoleId();
        if(currentRoleId != null && currentRoleId <= CommonConstant.NUM_ROLE_PM){
            return;
        }

        request.setAttribute("projectId",request.getParameter("projectId"));
        request.setAttribute("clusterId",request.getParameter("clusterId"));
        Map<String,String> attribute = (Map<String,String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String url = (String)request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        //处理url
        while (url.contains(LEFT_BRACKET)){
            int start = url.indexOf(LEFT_BRACKET);
            int end = url.indexOf(RIGHT_BRACKET);
            url = url.replaceAll(BACKSLASH + url.substring(start,end + NUMONE), STAR);
        }
        if (url.endsWith(POINTSTAR)){
            int i = url.lastIndexOf(POINTSTAR);
            url = url.substring(0, i);
        }
        //加载权限字典
        if (CollectionUtils.isEmpty(dataResourceUrlMap)){
            dataResourceUrlMap = this.dataResourceUrlService.getDataResourceUrlMap();
        }
        DataResourceUrl dataResourceUrl = dataResourceUrlMap.get(url+":"+request.getMethod());
        if (!Objects.isNull(dataResourceUrl) && request.getMethod().equals(dataResourceUrl.getMethod())){

            Integer resourceTypeId = dataResourceUrl.getResourceTypeId();
            checkPrivilege(resourceTypeId,attribute,request,session,url);
        }

    }

    private void checkPrivilege(Integer resourceTypeId,Map<String,String> attribute,HttpServletRequest request,HttpSession session,String url) throws Exception {
        String username = this.userService.getCurrentUsername();
        String targetName = null;
        String clusterId = null;
        int dataResourceTypeId = resourceTypeId;
        switch (DataResourceTypeEnum.valueOf(dataResourceTypeId)){
            case APPLICATION: //应用
                targetName = attribute.get("appName");
                break;
            case SERVICE: //服务
                targetName = attribute.get("deployName");
                break;
            case CONFIGFILE: //配置文件
                targetName = attribute.get("configMapName");
                if(StringUtils.isEmpty(targetName)){
                    targetName = request.getParameter("name");
                }
                clusterId = request.getParameter("clusterId");
                break;
//            case EXTERNALSERVICE : targetName = attribute.get("serviceName");   //外部服务
//                break;
//            case STORAGE : targetName = attribute.get("pvName");        //存储
//                break;
            case PIPELINE: //流水线
                targetName = attribute.get("jobId");
                break;
        }
        if("/tenants/*/projects/*/deploys/rules".equals(url)){
            if(request.getParameterMap().containsKey("appName")){  //应用资源
                targetName = (String)request.getParameterMap().get("appName")[0];
                dataResourceTypeId = DATA_RESOURCE_TYPE_APP;
            }else{                                     //服务资源
                Map<String, String[]> parameterMap = request.getParameterMap();
                targetName = parameterMap.get("nameList")[0];
                dataResourceTypeId = DATA_RESOURCE_TYPE_DEPLOY;
            }
        }

        DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
        dataPrivilegeDto.setData(targetName);
        dataPrivilegeDto.setProjectId(attribute.get("projectId"));
        dataPrivilegeDto.setClusterId(clusterId);
        dataPrivilegeDto.setDataResourceType(dataResourceTypeId);
        if(!Objects.isNull(request.getParameterMap().get("namespace"))) {
            dataPrivilegeDto.setNamespace(request.getParameterMap().get("namespace")[0]);
        }

        if(GET.equals(request.getMethod())){
            //根据用户，资源，操作权限类型，获得只读组ID
            dataPrivilegeDto.setPrivilegeType(RO);
            List<DataPrivilegeGroupMapping>  dataPGMList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);
            if(dataPGMList.size() > 0){
                //根据只读组ID，获得只读用户列表
                List<DataPrivilegeGroupMember> dataPGMemberList = dataPrivilegeGroupMemberService.listMemberInGroup(dataPGMList.get(0).getGroupId()) ;
                if(dataPGMemberList.size() > 0){
                    Long userId = (Long) (session.getAttribute("userId"));
                    //检查用户是否有只读权限(用户在读写列表中，则有权限，反之没有)
                    for (int i=0; i<dataPGMemberList.size(); i++) {
                        if((dataPGMemberList.get(i).getMemberId().longValue() == userId)){
                            return;
                        }
                    }
                }
            }
            //根据用户，资源，操作权限类型，获得读写组ID
            dataPrivilegeDto.setPrivilegeType(RW);
            List<DataPrivilegeGroupMapping>  dataPGMapList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);
            if(dataPGMList.size() > 0){
                //根据组ID，获得读写用户列表
                List<DataPrivilegeGroupMember> dataPGMemberList = dataPrivilegeGroupMemberService.listMemberInGroup(dataPGMapList.get(0).getGroupId()) ;
                Long userId = (Long) (session.getAttribute("userId"));
                //检查用户是否有读写权限(用户在读写列表中，则有权限，反之没有)
                for (int i=0; i<dataPGMemberList.size(); i++) {
                    if((dataPGMemberList.get(i).getMemberId().longValue() == userId)){
                        return;
                    }
                    if(i == (dataPGMemberList.size()-1)){
                        logger.info(username+"用户没有读权限！");
                        throw new MarsRuntimeException(ErrorCodeMessage.USER_PERMISSION_DENIED);
                    }
                }
            }else{
                return;
            }
        }else{
            //根据用户，资源，操作权限类型，获得读写组ID
            dataPrivilegeDto.setPrivilegeType(RW);
            List<DataPrivilegeGroupMapping>  dataPGMList = dataPrivilegeGroupMappingService.listDataPrivilegeGroupMapping(dataPrivilegeDto);
            if(dataPGMList.size() > 0){
                //根据读写组ID，获得读写用户列表
                List<DataPrivilegeGroupMember> dataPGMemberList = dataPrivilegeGroupMemberService.listMemberInGroup(dataPGMList.get(0).getGroupId()) ;
                Long userId = (Long) (session.getAttribute("userId"));
                //检查用户是否有读写权限(用户在读写列表中，则有权限，反之没有)
                for (int i=0; i<dataPGMemberList.size(); i++) {
                    if((dataPGMemberList.get(i).getMemberId().longValue() == userId)){
                        return;
                    }
                    if(i == (dataPGMemberList.size()-1)){
                        logger.info(username+"用户没有读写权限！！");
                        throw new MarsRuntimeException(ErrorCodeMessage.USER_PERMISSION_DENIED);
                    }
                }
            }else{
                return;
            }
        }
    }

}