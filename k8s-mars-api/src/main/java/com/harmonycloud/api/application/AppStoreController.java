package com.harmonycloud.api.application;

import javax.servlet.http.HttpSession;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.AppStoreDto;
import com.harmonycloud.service.application.AppStoreService;
import org.springframework.web.multipart.MultipartFile;


@RequestMapping("/tenants/projects/apptemplates")
@Controller
public class AppStoreController {

    @Autowired
    private HttpSession session;
    
    @Autowired
    private AppStoreService appStoreService;

    @Autowired
    private UserService userService;
    
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil saveAppStore(@ModelAttribute AppStoreDto detail) throws Exception {
        if(detail == null || StringUtils.isEmpty(detail.getName()) || StringUtils.isEmpty(detail.getTag()) || StringUtils.isEmpty(detail.getImage()) || detail.getServiceList() == null || detail.getServiceList().size() <= 0 || StringUtils.isEmpty(detail.getType())) {
           throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String username = userService.getCurrentUsername();
        return appStoreService.addAppStore(detail, username);
    }
    
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listAppStore(@RequestParam(value = "name", required = false) String name) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(appStoreService.listAppStore(name));
    }
    
    @ResponseBody
    @RequestMapping(value="/{name}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteAppStore(@PathVariable(value = "name") String name,
                                           @RequestParam(value = "tag") String tag) throws Exception {
        return appStoreService.deleteAppStore(name, tag);
    }
    
    @ResponseBody
    @RequestMapping(value="/{name}", method = RequestMethod.PUT)
    public ActionReturnUtil updateAppStore(@PathVariable(value = "name") String name, @ModelAttribute AppStoreDto detail) throws Exception {
    	detail.setName(name);
        String userName = userService.getCurrentUsername();
        if(detail == null || StringUtils.isEmpty(detail.getName()) || StringUtils.isEmpty(detail.getTag()) || StringUtils.isEmpty(detail.getImage()) || detail.getServiceList() == null || detail.getServiceList().size() <= 0 || StringUtils.isEmpty(detail.getType())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        return appStoreService.updateAppStore(detail, userName);
    }
    
    @ResponseBody
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public ActionReturnUtil getAppStore(@PathVariable(value = "name") String name, @RequestParam(value = "tag") String tag) throws Exception {

        return ActionReturnUtil.returnSuccessWithData(appStoreService.getAppStore(name, tag));
    }

    @ResponseBody
    @RequestMapping(value = "/validate/{name}", method = RequestMethod.GET)
    public ActionReturnUtil getAppStore(@PathVariable(value = "name") String name) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(appStoreService.checkName(name));
    }
    
    @ResponseBody
    @RequestMapping(value = "/{name}/tags", method = RequestMethod.GET)
    public ActionReturnUtil listAppStoreTags(@PathVariable(value = "name") String name) throws Exception {
        return appStoreService.listAppStoreTags(name);
    }

    @ResponseBody
    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public ActionReturnUtil upload(@RequestParam(value="file") MultipartFile file) throws Exception {
        String fileName =  appStoreService.uploadImage(file);
        return ActionReturnUtil.returnSuccessWithData(fileName);
    }
}
