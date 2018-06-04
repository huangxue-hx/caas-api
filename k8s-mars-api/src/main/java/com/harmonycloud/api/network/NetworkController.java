package com.harmonycloud.api.network;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.harmonycloud.api.config.InitClusterConfig;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NetworkCalico;
import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dto.tenant.CreateNetwork;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NetworkService;


@Controller
@RequestMapping("/tenants/{tenantId}")
@Transactional
public class NetworkController {

    @Autowired
    NetworkService networkService;

//    @Value("#{propertiesReader['network.networkFlag']}")
    private String networkFlag ;

    NetworkController() throws Exception{
        networkFlag = InitClusterConfig.getNetworkConfig().getNetworkFlag();
    }



    @RequestMapping(value = "/networks", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listNetwork(@PathVariable("tenantId") String tenantId) throws Exception {

        ActionReturnUtil data = null;
        if (Constant.NETWORK_CALICO.equals(networkFlag)) {
            List<Map<String, Object>> networklist = networkService.networkList(tenantId);
            data = ActionReturnUtil.returnSuccessWithData(networklist);
        } else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_CONFIG,"networkFlag",true);
        }
        return data;
    }

    @RequestMapping(value = "/networks/{networkId}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNetwork(@PathVariable("tenantId") String tenantId, @PathVariable("networkId") String networkId,
            @RequestParam(value = "bind", required = false) String bind) throws Exception {

        if (StringUtils.isEmpty(networkId) || StringUtils.isEmpty(tenantId)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        ActionReturnUtil data = null;
        if (Constant.NETWORK_CALICO.equals(networkFlag)) {
            data = networkService.calicoNetworkdetail(networkId, tenantId, bind);
        } else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_CONFIG,"networkFlag",true);
        }
        return data;
    }

    @RequestMapping(value = "/networks", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(isolation = Isolation.DEFAULT)
    public ActionReturnUtil createNetwork(@PathVariable("tenantId") String tenantId,
                                          @ModelAttribute CreateNetwork createNetwork) throws Exception {
        return networkService.networkCreate(createNetwork);

    }

    @RequestMapping(value = "/networks/removeBingSubnet", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public ActionReturnUtil removeBingSubnet(String namespace) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.NAMESPACE);
        ActionReturnUtil data = networkService.subnetRemoveBing(namespace);
        return data;
    }

    /**
     * delete network form db
     * 
     * @param networkId
     * @return
     */
    @RequestMapping(value = "/networks/{networkId}", method = RequestMethod.DELETE)
    @ResponseBody
    @Transactional
    public ActionReturnUtil deleteNetwork(@PathVariable("networkId") String networkId) throws Exception {
        return networkService.networkDelete(networkId);
    }


    @RequestMapping(value = "/networks/{networkId}/nettopology", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createNetTopology(@PathVariable("networkId") String networkId, @RequestParam(value = "toNetworkId") String toNetworkId) throws Exception {
        
        NetworkCalico fromNetwork = networkService.getnetworkbyNetworkid(networkId);
        if (fromNetwork == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER,"networkId", true);
        }

        NetworkCalico toNetwork = networkService.getnetworkbyNetworkid(networkId);
        if (toNetwork == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER,"toNetworkId", true);
        }
        String fromNetworkName = fromNetwork.getNetworkname();
        String toNetworkName = toNetwork.getNetworkname();
        NetworkTopology topology2 = networkService.getTopologybyNetworkidfromAndNetworkidto(networkId, toNetworkId, fromNetworkName, toNetworkName);
        if (topology2 != null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.TOPOLOGY_EXIST,fromNetworkName + " -> " + toNetworkName + " ",true );
        }
        Date date = DateUtil.getCurrentUtcTime();
        NetworkTopology topology = new NetworkTopology();
        topology.setCreatetime(date);
        topology.setNetId(networkId);
        topology.setNetName(fromNetworkName);
        topology.setTopology(fromNetworkName + "_" + toNetworkName);
        topology.setDestinationid(toNetworkId);
        topology.setDestinationname(toNetworkName);
        networkService.createNetworkTopology(topology);

        // 查询from 和 to 对应的 ns并返回
        List<NamespceBindSubnet> listfrom = networkService.getsubnetbynetworkid(networkId);
        List<String> nsfromlist = new ArrayList<>();
        for (NamespceBindSubnet namespceBindSubnet : listfrom) {
            nsfromlist.add(namespceBindSubnet.getNamespace());
        }

        List<NamespceBindSubnet> listto = networkService.getsubnetbynetworkid(toNetworkId);
        List<String> nstolist = new ArrayList<>();
        for (NamespceBindSubnet namespceBindSubnet : listto) {
            nstolist.add(namespceBindSubnet.getNamespace());
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("networknamefrom", fromNetworkName);
        data.put("networknameto", toNetworkName);
        data.put("nsfrom", nsfromlist);
        data.put("nsto", nstolist);
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @RequestMapping(value = "/networks/{networkId}/nettopology", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteNetTopology(@PathVariable("networkId") String networkId,
                                              @RequestParam(value = "toNetworkId") String toNetworkId) throws Exception {

        NetworkCalico networkfrom = networkService.getnetworkbyNetworkid(networkId);
        if (networkfrom == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER,"networkId", true);
        }

        NetworkCalico networkto = networkService.getnetworkbyNetworkid(networkId);
        if (networkto == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER,"toNetworkId", true);
        }
        String networknamefrom = networkfrom.getNetworkname();
        String networknameto = networkto.getNetworkname();
        NetworkTopology topology2 = networkService.getTopologybyNetworkidfromAndNetworkidto(networkId, toNetworkId, networknamefrom, networknameto);
        if (topology2 == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.TOPOLOGY_NOT_EXIST,networknamefrom + " -> " + networknameto + " ",true );
        }

        networkService.deletetopologybyId(topology2.getId());
        // 查询from 和 to 对应的 ns并返回
        List<NamespceBindSubnet> listfrom = networkService.getsubnetbynetworkid(networkId);
        List<String> nsfromlist = new ArrayList<>();
        for (NamespceBindSubnet namespceBindSubnet : listfrom) {
            nsfromlist.add(namespceBindSubnet.getNamespace());
        }

        List<NamespceBindSubnet> listto = networkService.getsubnetbynetworkid(toNetworkId);
        List<String> nstolist = new ArrayList<>();
        for (NamespceBindSubnet namespceBindSubnet : listto) {
            nstolist.add(namespceBindSubnet.getNamespace());
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("networknamefrom", networknamefrom);
        data.put("networknameto", networknameto);
        data.put("nsfrom", nsfromlist);
        data.put("nsto", nstolist);
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @RequestMapping(value = "/networks/{networkId}/nettopology", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNetTopology(@PathVariable("networkId") String networkId) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        List<NetworkTopology> list1 = networkService.getTopologybyNetworkid(networkId);

        List<NetworkTopology> list2 = networkService.getTopologybydestination(networkId);
        if (list1.size() <= 0 && list2.size() <= 0) {
            data.put("hasTopology", false);
            return ActionReturnUtil.returnSuccessWithData(data);
        } else {
            List<String> topologylist = new ArrayList<>();
            if (list2.size() > 0) {
                for (NetworkTopology networkTopology : list2) {
                    topologylist.add(networkTopology.getTopology());
                }
            }
            if (list1.size() > 0) {
                for (NetworkTopology networkTopology : list1) {
                    topologylist.add(networkTopology.getTopology());
                }
            }
            data.put("hasTopology", true);
            data.put("topologylist", topologylist);
            return ActionReturnUtil.returnSuccessWithData(data);
        }
    }

    @RequestMapping(value = "/networks/nettopology", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listNetTopology(@PathVariable("tenantId") String tenantId) throws Exception {
        List<NetworkCalico> list = networkService.getnetworkbyTenantid(tenantId);
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Integer> tem = new HashMap<>();
        List<Map<String, Object>> listnetnodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> listnetedges = new ArrayList<Map<String, Object>>();
        int location = 0;
        for (NetworkCalico networkCalico : list) {
            List<NetworkTopology> list1 = networkService.getTopologybyNetworkid(networkCalico.getNetworkid());
            Map<String, Object> node = new HashMap<String, Object>();
            tem.put(networkCalico.getNetworkname(), location++);
            node.put("tenantname", networkCalico.getTenantname());
            node.put("networkname", networkCalico.getNetworkname());
            node.put("networkid", networkCalico.getNetworkid());
            listnetnodes.add(node);
            if (list1.size() > 0) {
                for (NetworkTopology networkTopology : list1) {
                    String[] split = networkTopology.getTopology().split("_");
                    Map<String, Object> edges = new HashMap<String, Object>();
                    edges.put("source", split[0]);
                    edges.put("target", split[1]);
                    listnetedges.add(edges);
                }
            }
        }

        for (int i = 0; i < listnetedges.size(); i++) {
            String source = listnetedges.get(i).get("source").toString();
            Integer sourcelocation = tem.get(source);
            listnetedges.get(i).put("source", sourcelocation);

            String target = listnetedges.get(i).get("target").toString();
            Integer targetlocation = tem.get(target);
            listnetedges.get(i).put("target", targetlocation);
        }

        data.put("nodes", listnetnodes);
        data.put("edges", listnetedges);
        return ActionReturnUtil.returnSuccessWithData(data);

    }

    public String getNetworkFlag() {
        return networkFlag;
    }

    public void setNetworkFlag(String networkFlag) {
        this.networkFlag = networkFlag;
    }
}
