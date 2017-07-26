package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;

import com.harmonycloud.service.application.BusinessService;

import com.harmonycloud.dto.business.BusinessTemplateDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by root on 3/29/17.
 */
@RequestMapping("/businessTemplate")
@Controller
public class BusinessController {

    @Autowired
    HttpSession session;

    @Autowired
    BusinessService businessService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * create application template on 17/05/05.
     * 
     * @param businessTemplate
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)

    public ActionReturnUtil saveBusinessTemplate(@ModelAttribute BusinessTemplateDto businessTemplate) throws Exception {
        logger.info("create business template");

        String userName = (String) session.getAttribute("username");
        return businessService.saveBusinessTemplate(businessTemplate, userName);
    }
    
    /**
     * update application template on 17/05/05.
     * 
     * @param businessTemplate
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)

    public ActionReturnUtil updateBusinessTemplate(@ModelAttribute BusinessTemplateDto businessTemplate) throws Exception {
        logger.info("update business template");
        String userName = (String) session.getAttribute("username");
        return businessService.updateBusinessTemplate(businessTemplate, userName);
    }

    /**
     * get application template by tenant and name or image on 17/05/05.
     * 
     * @param searchkey
     * 
     * @param searchValue
     * 
     * @param tenant
     *            required
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listBusinessTemplate(@RequestParam(value = "searchkey", required = false) String searchKey,
            @RequestParam(value = "searchvalue", required = false) String searchValue, @RequestParam(value = "tenant", required = false) String tenant) throws Exception {
        logger.info("get application template");
        return businessService.listBusinessTemplateByTenant(searchKey, searchValue, tenant);
    }

    /**
     * get application template by name and tag (and tenant) on 17/05/05 .
     * 
     * @param businessTemplate
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ActionReturnUtil getBusinessTemplate(@RequestParam(value = "name", required = true) String name, @RequestParam(value = "tag", required = true) String tag)
            throws Exception {
        logger.info("get application template");
        return businessService.getBusinessTemplate(name, tag);
    }

    /**
     * delete application template by name on 17/05/05.
     * 
     * @param name
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteBusinessTemplate(@RequestParam(value = "name", required = true) String name) throws Exception {
        logger.info("get application template");
        return businessService.deleteBusinessTemplate(name);
    }
    
    /**
     * get application template by name and tag (and tenant) on 17/05/05 .
     * 
     * @param businessTemplate
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/list/tag", method = RequestMethod.GET)
    public ActionReturnUtil getBusinessTemplateByName(@RequestParam(value = "name", required = true) String name, @RequestParam(value = "tenant", required = true) String tenant)
            throws Exception {
        logger.info("get application template");
        return businessService.getBusinessTemplateByName(name, tenant);
    }
}
