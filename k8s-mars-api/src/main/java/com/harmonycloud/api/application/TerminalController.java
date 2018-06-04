package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.application.TerminalService;
import com.pty4j.PtyProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Created by czm on 2017/2/14.
 */
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys/{deployName}/terminal")
@Controller
public class TerminalController {
    @Autowired
    private TerminalService terminalService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTerminal(@RequestParam(value = "pod") String pod,
                                        @RequestParam(value = "container") String container,
                                        @RequestParam(value = "namespace") String namespace) throws Exception{
        return terminalService.getTerminal(pod,container,namespace);
    }


    @RequestMapping(value = "/terminalmessage", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTerminalMessage(String sn) throws Exception{
        return terminalService.getTerminalMassage(sn);
    }


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