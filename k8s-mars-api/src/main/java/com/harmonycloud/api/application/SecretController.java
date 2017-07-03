package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.SecretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 
 * @author jmi
 *
 */
@Controller
@RequestMapping(value = "/secret")
public class SecretController {

    @Autowired
    private HttpSession session;

    @Autowired
    private SecretService secretService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(value = "/checked", method = RequestMethod.GET)
    public ActionReturnUtil checkedSecret() throws Exception {
        if (session.getAttribute("username") == null) {
            throw new K8sAuthException(Constant.HTTP_401);
        }
        String userName = session.getAttribute("username").toString();
        String password = session.getAttribute("password").toString();
        return secretService.checkedSecret(userName, password);

    }

}
