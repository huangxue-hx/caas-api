package com.harmonycloud.api.debug;

import com.harmonycloud.common.util.ZipUtil;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.debug.DebugService;
import com.harmonycloud.service.platform.socketio.test.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Url;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by fengjinliu on 2019/5/5.
 */
@RestController
public class DebugController {

    private final static Logger logger= LoggerFactory.getLogger(DebugController.class);

    @Autowired
    private HttpSession session;

    @Autowired
    private DebugService debugService;
    /*
    **建立debug环境。传入参数为分区名称，租户名称，服务名称
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/namespaces/{namespace}/services/{service}/debug/start",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil establishEnvironment (@PathVariable("namespace")String namespace
            , @PathVariable(value="service")String service
            , @RequestParam(value = "port",required = false)String port)throws Exception{
        String username=session.getAttribute("username").toString();
        return debugService.start(namespace,username,service,port)? ActionReturnUtil.returnSuccess():ActionReturnUtil.returnError();
    }

    /*
    **提供下载接口  system仅有三个值，mac，windows，linux
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/namespaces/{namespace}/services/{service}/debug/download/{system}",method = RequestMethod.GET)
    @ResponseBody
    public void downloadCli(@PathVariable(value="system")String system, HttpServletResponse response) throws Exception{
        // 1.根据分区所在的集群拼装config文件
        // 2.提供下载

        String systemFile="";
        if(system.equals("windows")) {
            systemFile="hcdb.exe";
        }
        else {
            systemFile="hcdb";
        }

        List<File> fileList=new ArrayList<>();

        //从build完的target/classes下查找文件。一个为config。一个为对应系统的文件
        URL sysurl = DebugController.class.getClassLoader().getResource("hcdb/"+system+"/"+systemFile);
        File sys = new File(sysurl.getFile());

        URL configurl=DebugController.class.getClassLoader().getResource("hcdb/config");
        File config =new File(configurl.getFile());

        fileList.add(config);
        fileList.add(sys);

        OutputStream fo = null;
        try {
            fo = new BufferedOutputStream(response.getOutputStream());
            //压缩zip文件工具类
            ZipUtil.toZip(fileList, fo);
        } catch (Exception e) {
            logger.error("压缩文件失败,请重试");
        }

    }

    /**
     * 获取cli执行命令
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/namespaces/{namespace}/services/{service}/debug/command",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getCommands(@PathVariable("namespace")String namespace
            , @PathVariable(value="service")String service
            , @RequestParam(value = "port",required = false)String port )throws Exception{

        //1. 通过服务拿到端口号。
        //2. 拼装成命令
        String username=session.getAttribute("username").toString();
        return ActionReturnUtil.returnSuccessWithData(debugService.getCommands(namespace,username,service).getData());
    }


    /**
     *测试连接是否可用
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/namespaces/{namespace}/services/{service}/debug/test/link",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkLink(@PathVariable("namespace")String namespace
            , @PathVariable(value="service")String service)throws Exception{
        String username=session.getAttribute("username").toString();
        if(debugService.checkLink(namespace,username,service))
            return ActionReturnUtil.returnSuccess();
        else return ActionReturnUtil.returnError();
    }

    /**
     *测试服务是否被占用debug
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/namespaces/{namespace}/services/{service}/debug/test/service",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkService(@PathVariable("namespace")String namespace
            , @PathVariable(value="service")String service)throws Exception{
        return ActionReturnUtil.returnSuccessWithData(debugService.checkService(namespace,service));
    }

    /**
     *关闭debug功能
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/namespaces/{namespace}/services/{service}/debug/end",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil endDebug(@PathVariable("namespace")String namespace
            , @PathVariable(value="service")String service,@RequestParam(value="port",required = false)String port)throws Exception{
        // 1. 恢复service

        // 2. 下线pod

        //3. 修改用户debug状态
        String username=session.getAttribute("username").toString();
        if(debugService.end(namespace,username,service,port))
        return  ActionReturnUtil.returnSuccess();
        else return ActionReturnUtil.returnError();

    }

    /**
     *查询租户是否在debug
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/users/debug/test",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkUser()throws Exception{
        String username=session.getAttribute("username").toString();
        return ActionReturnUtil.returnSuccessWithData(debugService.checkUser(username));
    }

}
