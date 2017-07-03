package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.service.application.TerminalService;
import com.pty4j.PtyProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by czm on 2017/2/14.
 */
@RequestMapping("/terminal")
@Controller
public class TerminalController {
    @Autowired
    private TerminalService terminalService;

    @RequestMapping(value = "/getTerminal", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil getTerminal(HttpServletRequest req,String pod, String container, String namespace) {

        try {
            K8SClient k8SClient = new K8SClient();

             return terminalService.getTerminal(pod,container,namespace);

        } catch (Exception e) {
            return ActionReturnUtil.returnError();
        }
    }


    @RequestMapping(value = "/getTerminalMassage", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTerminalMassage(String sn) {

        try {
            return terminalService.getTerminalMassage(sn);

        } catch (Exception e) {
            return ActionReturnUtil.returnError();
        }
    }

    /*@RequestMapping(value = "/openTerminal", method = RequestMethod.POST)
    public void getTerminal(String sn) throws Exception {
        //2.跟据sn获取参数terminalService.openTerminal(sn)
        Map<String,String> param = terminalService.openTerminal(sn);

        String container = param.get("container");
        String pod = param.get("pod");
        String namespace = param.get("namespace");

        //3.从session中获取配置信息
        String protocol = "http";
        String host = "10.10.102.20";
        String port="8080";
        String token = "";

        //4. 生成终端(登陆到容器当中)
        String[] cmd = { "./kubectl" };
        String[] env = {"exec",pod,"--container="+container,"--namespace="+namespace,"-it","bash",
                    "--server="+protocol+"://"+host+":"+port,"--token="+token,"--insecure-skip-tls-verify=true"};

        term = PtyProcess.exec(cmd, env);//少传入了一个关于窗口尺寸的参数


    }*/

    public static void main(String[] args) throws IOException {
        String container = "testc";
        String pod = "testnginx-3643425683-xehnx";
        String namespace = "yj-yjp";
        String protocol = "http";
        String host = "10.10.102.20";
        String port="8080";
        String token = "233e5125ddf04c24a9dbcd24d99aeb62";

        String[] cmd = { "./kubectl"};
        String[] env = {"exec",pod,"--container="+container,"--namespace="+namespace,"-it","bash",
                    "--server="+protocol+"://"+host+":"+port,"--token="+token,"--insecure-skip-tls-verify=true"};

        PtyProcess term = PtyProcess.exec(cmd, env);//少传入了一个关于窗口尺寸的参数

        //System.out.println(term.isAlive());

    }


}