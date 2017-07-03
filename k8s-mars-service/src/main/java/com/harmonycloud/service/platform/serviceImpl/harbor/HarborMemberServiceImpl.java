package com.harmonycloud.service.platform.serviceImpl.harbor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HarborUtil;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.service.platform.bean.HarborRole;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.harbor.HarborMemberService;

/**
 * Created by zsl on 2017/1/19.
 */
@Service
public class HarborMemberServiceImpl implements HarborMemberService {
	
	@Autowired
	private HarborUtil harborUtil;

    /**
     * 根据projectId获取project下的成员列表
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil usersOfProject(Integer projectId) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }

        String url = HarborClient.getPrefix() + "/api/projects/" + projectId + "/members/";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpGetRequest(url, headers, null);

    }

    /**
     * 创建project下的role
     *
     * @param projectId  projectId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createRole(Integer projectId, HarborRole harborRole) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }
        if (harborRole == null) {
            return ActionReturnUtil.returnErrorWithMsg("role is invalid");
        }

        String url = HarborClient.getPrefix() + "/api/projects/" + projectId + "/members/";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        //check if the user-project pair is existed
        //return HttpClientUtil.httpPostRequestForHarbor(url, headers, convertHarborRoleBeanToMap(harborRole));
         ActionReturnUtil aru = HttpClientUtil.httpPostRequestForHarbor(url, headers, convertHarborRoleBeanToMap(harborRole));
        if(((boolean)aru.get("success")==false)&&((String)aru.get("data")=="user is ready in project")){
            return ActionReturnUtil.returnSuccess();
        }else{
            return aru;
        }
    }

    /**
     * 更新project下的role
     *
     * @param projectId  projectId
     * @param userId     userId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil updateRole(Integer projectId, Integer userId, HarborRole harborRole) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }
        if (userId == null || userId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("userId is invalid");
        }
        if (harborRole == null) {
            return ActionReturnUtil.returnErrorWithMsg("role is invalid");
        }

        String url = HarborClient.getPrefix() + "/api/projects/" + projectId + "/members/" + userId;

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpPutRequestForHarbor(url, headers, convertHarborRoleBeanToMap(harborRole));

    }

    /**
     * 删除project下的role
     *
     * @param projectId projectId
     * @param userId    userId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil deleteRole(Integer projectId, Integer userId) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }
        if (userId == null || userId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("userId is invalid");
        }

        String url = HarborClient.getPrefix() + "/api/projects/" + projectId + "/members/" + userId;

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpDoDelete(url, null, headers);

    }

    /**
     * harborRole bean 转换为 map
     *
     * @param harborRole harborRole bean
     * @return map
     */
    private Map<String, Object> convertHarborRoleBeanToMap(HarborRole harborRole) throws Exception{
        Map<String, Object> map = new HashMap<>();
        if (harborRole != null) {
            if (!CollectionUtils.isEmpty(harborRole.getRoleList())) {
                Object[] roleArr = harborRole.getRoleList().toArray();
                map.put("roles", roleArr);
            }
            if (StringUtils.isNotEmpty(harborRole.getUsername())) {
                map.put("username", harborRole.getUsername());
            }
        }
        return map;
    }

    /*public static void main(String[] args) {

        try {
            HarborMemberServiceImpl harborMemberService = new HarborMemberServiceImpl();
            ActionReturnUtil actionReturnUtil = harborMemberService.usersOfProject(77);
            List<Map<String, Object>> map = new ArrayList<>();
            String json;
            if(actionReturnUtil.get("data") != null){
                json = actionReturnUtil.get("data").toString();
//                json = json.replaceAll("\n","");
                map = JsonUtil.JsonToMapList(json);
            }
            HarborRole harborRole = new HarborRole();
            List<Integer> roleList = new ArrayList<>();
            roleList.add(2);
            harborRole.setUsername("gywtest");
            harborRole.setRoleList(roleList);

            ActionReturnUtil actionReturnUtil1 = harborMemberService.createRole(77, harborRole);
//            ActionReturnUtil actionReturnUtil2 = harborMemberService.updateRole(77, 158,harborRole);

//            ActionReturnUtil actionReturnUtil3 = harborMemberService.deleteRole(77, 158);

            System.out.print(actionReturnUtil.get("data").toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
