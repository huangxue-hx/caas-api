package com.harmonycloud.api.debug;

import com.harmonycloud.api.debug.Utils.ZipUtils;
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

import javax.servlet.http.HttpServletResponse;
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
@RequestMapping("/debug/namespace/{namespace}/username/{username}/service/{service}")
@RestController
public class DebugController {

    private final static Logger logger= LoggerFactory.getLogger(DebugController.class);

    @Autowired
    private DebugService debugService;
    /*
    **建立debug环境。传入参数为分区名称，租户名称，服务名称
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/start",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil establishEnvironment (@PathVariable("namespace")String namespace
            , @PathVariable(value="username")String username, @PathVariable(value="service")String service
            , @RequestParam(value = "port",required = false)String port)throws Exception{

        return debugService.start(namespace,username,service,port)? ActionReturnUtil.returnSuccess():ActionReturnUtil.returnError();
    }

    /*
    **提供下载接口  system仅有三个值，mac，windows，linux
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/download/{system}",method = RequestMethod.GET)
    @ResponseBody
    public void downloadCli(@PathVariable(value="system")String system, HttpServletResponse response) throws Exception{

        // 1.根据分区所在的集群拼装config文件

        // 2.提供下载

        String f2="";
        if(system.equals("windows"))f2="hcdb.exe";
        else f2="hcdb";

        List<File> fileList=new ArrayList<>();

        //从build完的target/classes下查找文件。一个为config。一个为对应系统的文件
        URL sysurl = DebugController.class.getClassLoader().getResource("hcdb/"+system+"/"+f2);
        File sys = new File(sysurl.getFile());

        URL configurl=DebugController.class.getClassLoader().getResource("hcdb/config");
        File config =new File(configurl.getFile());

        fileList.add(config);
        fileList.add(sys);

        OutputStream fo = null;
        try {
            fo = new BufferedOutputStream(response.getOutputStream());
            //压缩zip文件工具类
            ZipUtils.toZip(fileList, fo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取cli执行命令
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/command",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getCommands(@PathVariable("namespace")String namespace
            , @PathVariable(value="username")String username, @PathVariable(value="service")String service
            , @RequestParam(value = "port",required = false)String port )throws Exception{

        //1. 通过服务拿到端口号。
        //2. 拼装成命令
        return ActionReturnUtil.returnSuccessWithData(debugService.getCommands(namespace,username,service).getData());
    }


    /**
     *测试连接是否可用
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/test/link",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkLink(@PathVariable("namespace")String namespace
            , @PathVariable(value="username")String username, @PathVariable(value="service")String service)throws Exception{
        if(debugService.checkLink(namespace,username,service))
            return ActionReturnUtil.returnSuccess();
        else return ActionReturnUtil.returnError();
    }

    /**
     *关闭debug功能
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/end",method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil endDebug(@PathVariable("namespace")String namespace
            , @PathVariable(value="username")String username, @PathVariable(value="service")String service,@RequestParam(value="port",required = false)String port)throws Exception{
        // 1. 恢复service

        // 2. 下线pod

        //3. 修改用户debug状态
        if(debugService.end(namespace,username,service,port))
        return  ActionReturnUtil.returnSuccess();
        else return ActionReturnUtil.returnError();

    }

    /**
     *查询租户是否在debug
     */
    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value="/test/user",method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkUser(
            @PathVariable(value="username")String username)throws Exception{
        return ActionReturnUtil.returnSuccessWithData(debugService.checkUser(username));
    }

}
