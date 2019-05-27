package com.harmonycloud.api.debug;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ZipUtil;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.debug.DebugService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.net.URL;
import java.util.List;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by fengjinliu on 2019/5/5.
 */
@RestController
public class DebugController {

    private final static Logger logger = LoggerFactory.getLogger(DebugController.class);

    @Autowired
    private HttpSession session;

    @Autowired
    private DebugService debugService;

    @Value("#{propertiesReader['upload.path']}")
    private String uploadPath;

    /*
    **建立debug环境。传入参数为分区名称，租户名称，服务名称
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/namespaces/{namespace}/services/{service}/debug/start", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil establishEnvironment(@PathVariable("namespace") String namespace,
        @PathVariable(value = "service") String service, @RequestParam(value = "port", required = false) String port)
        throws Exception {
        String username = session.getAttribute("username").toString();
        try {
            debugService.start(namespace, username, service, port);
        } catch (MarsRuntimeException e) {
            throw e;
        }
        return ActionReturnUtil.returnSuccess();
    }

    /*
    **提供下载接口  system仅有三个值，mac，windows，linux
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/namespaces/{namespace}/services/{service}/debug/download/{system}",
        method = RequestMethod.GET)
    @ResponseBody
    public void downloadCli(@PathVariable(value = "system") String system,
        @PathVariable(value = "namespace") String namespace, HttpServletResponse response) throws Exception {
        // 1.根据分区所在的集群拼装config文件
        // 2.提供下载
        List<File> fileList = debugService.getConfig(namespace, system);
        OutputStream fo = null;
        try {
            fo = new BufferedOutputStream(response.getOutputStream());
            // 压缩zip文件工具类
            ZipUtil.toZip(fileList, fo);
        } catch (Exception e) {
            logger.error("压缩文件失败,请重试");
        }

    }

    /**
     * 获取cli执行命令
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/namespaces/{namespace}/services/{service}/debug/command", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getCommands(@PathVariable("namespace") String namespace,
        @PathVariable(value = "service") String service, @RequestParam(value = "port", required = false) String port,
        @RequestParam(value = "system", required = false) String system) throws Exception {

        // 1. 通过服务拿到端口号。
        // 2. 拼装成命令
        String username = session.getAttribute("username").toString();
        return ActionReturnUtil
            .returnSuccessWithData(debugService.getCommands(namespace, username, service, system).getData());
    }

    /**
     * 测试连接是否可用
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/namespaces/{namespace}/services/{service}/debug/test/link", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkLink(@PathVariable("namespace") String namespace,
        @PathVariable(value = "service") String service) throws Exception {
        String username = session.getAttribute("username").toString();
        if (debugService.checkLink(namespace, username, service)) {
            return ActionReturnUtil.returnSuccess();
        } else {
            return ActionReturnUtil.returnError();
        }
    }

    /**
     * 测试服务是否被占用debug
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/namespaces/{namespace}/services/{service}/debug/test/service", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkService(@PathVariable("namespace") String namespace,
        @PathVariable(value = "service") String service) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(debugService.checkService(namespace, service));
    }

    /**
     * 关闭debug功能
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/namespaces/{namespace}/services/{service}/debug/end", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil endDebug(@PathVariable("namespace") String namespace,
        @PathVariable(value = "service") String service, @RequestParam(value = "port", required = false) String port)
        throws Exception {
        // 1. 恢复service

        // 2. 下线pod

        // 3. 修改用户debug状态
        String username = session.getAttribute("username").toString();
        if (debugService.end(namespace, username, service, port))
            return ActionReturnUtil.returnSuccess();
        else
            return ActionReturnUtil.returnError();

    }

    /**
     * 查询租户是否在debug
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/users/debug/test", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkUser() throws Exception {
        String username = session.getAttribute("username").toString();
        return ActionReturnUtil.returnSuccessWithData(debugService.checkUser(username));
    }

}
