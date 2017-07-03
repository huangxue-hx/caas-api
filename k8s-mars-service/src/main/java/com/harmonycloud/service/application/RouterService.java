package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.business.ParsedIngressListDto;
import com.harmonycloud.dto.business.ParsedIngressListUpdateDto;
import com.harmonycloud.dto.business.SvcRouterDto;
import com.harmonycloud.dto.business.SvcRouterUpdateDto;
import com.harmonycloud.dto.svc.CheckPort;
import com.harmonycloud.dto.svc.SvcTcpDto;
import com.harmonycloud.service.platform.bean.RouterSvc;

import java.util.List;

/**
 * Created by czm on 2017/1/18.
 */
public interface RouterService {

    public List<ParsedIngressListDto> ingList(String namespace) throws Exception;
    
    public ActionReturnUtil ingCreate(ParsedIngressListDto parsedIngressList) throws Exception;
    
    public ActionReturnUtil ingUpdate(ParsedIngressListUpdateDto parsedIngressList) throws Exception;
    
    public ActionReturnUtil ingDelete(String namespace, String name) throws Exception;
    
    public ActionReturnUtil svcList(String namespace) throws Exception;
    
    public ActionReturnUtil listSvcByName(ParsedIngressListDto parsedIngressListDto) throws Exception;
    
    public ActionReturnUtil svcCreate(SvcRouterDto svcRouter) throws Exception;
    
    public ActionReturnUtil createTcpSvc(SvcTcpDto svcTcpDto) throws Exception;
    
    public ActionReturnUtil createhttpsvc(SvcTcpDto svcTcpDto) throws Exception;
   
    public ActionReturnUtil  createHttpSvc(ParsedIngressListDto parsedIngressList) throws Exception;
    
    public ActionReturnUtil svcUpdate(SvcRouterUpdateDto svcRouterUpdate) throws Exception;
    
    public ActionReturnUtil svcDelete(String namespace, String name) throws Exception;
   
    public ActionReturnUtil deleteTcpSvc(String namespace, String name,String port) throws Exception;

    public ActionReturnUtil getEntry() throws Exception;
    
    public ActionReturnUtil getHost() throws Exception;
    
    public ActionReturnUtil listProvider() throws Exception;
    
    public ActionReturnUtil getPort() throws Exception;
    
    public ActionReturnUtil checkPort(CheckPort checkPort) throws Exception;

    List<RouterSvc> listIngressByName(ParsedIngressListDto parsedIngressListDto) throws Exception;
}
