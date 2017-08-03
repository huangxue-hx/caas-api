package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.ParsedIngressListDto;
import com.harmonycloud.dto.business.ParsedIngressListUpdateDto;
import com.harmonycloud.dto.business.SvcRouterDto;
import com.harmonycloud.dto.business.SvcRouterUpdateDto;
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
   
    public ActionReturnUtil deleteTcpSvc(String namespace, String name,List<Integer> ports,String tenantId) throws Exception;

    public ActionReturnUtil getEntry() throws Exception;
    
    public ActionReturnUtil getHost() throws Exception;
    
    public ActionReturnUtil listProvider() throws Exception;
    
    public ActionReturnUtil getPort(String tenantId) throws Exception;
    
    public ActionReturnUtil getListPort(String tenantId) throws Exception;
    
    public ActionReturnUtil checkPort(String port,String tenantId) throws Exception;
    
    public ActionReturnUtil updatePort(String oldport,String nowport,String tenantId) throws Exception;
    
    public ActionReturnUtil delPort(String port,String tenantId) throws Exception;

    List<RouterSvc> listIngressByName(ParsedIngressListDto parsedIngressListDto) throws Exception;
    
    public ActionReturnUtil listIngressByName(String namespace, String name, Cluster cluster) throws Exception;
    
}
