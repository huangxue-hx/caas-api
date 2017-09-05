package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.business.YamlDto;
import com.harmonycloud.service.application.YamlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by root on 8/11/17.
 */
@RequestMapping("/yaml")
@Controller
public class YamlController {

    @Autowired
    HttpSession session;

    @Autowired
    YamlService yamlService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *add service template to businessTemplate on 17/05/05 .
     *
     * @param yamlDto
     *
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil getBusinessTemplateYaml(@ModelAttribute YamlDto yamlDto)
            throws Exception {
        logger.info("deploy application template by yaml!");
        return yamlService.deployYaml(yamlDto);
    }
}