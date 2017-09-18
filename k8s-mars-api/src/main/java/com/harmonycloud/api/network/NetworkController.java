package com.harmonycloud.api.network;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.util.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.TenantUtils;
import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NetworkCalico;
import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dto.tenant.CreateNetwork;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NetworkService;


@Controller
@RestController
@Transactional
public class NetworkController {

    @Autowired
    NetworkService networkService;

    @Value("#{propertiesReader['network.networkFlag']}")
    private String networkFlag;

    @RequestMapping(value = "/network/list", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil tenantList(@RequestParam(value = "tenantid", required = false) String tenantid) throws Exception {

        ActionReturnUtil data = null;
        if (Constant.NETWORK_CALICO.equals(networkFlag)) {
            List<Map<String, Object>> networklist = networkService.networkList(tenantid);
            data = ActionReturnUtil.returnSuccessWithData(networklist);
        } else {
            return ActionReturnUtil.returnErrorWithMsg("networkFlag 参数 配置错误！");
        }
        return data;
    }

    @RequestMapping(value = "/network/detail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil networkDetail(@RequestParam(value = "networkid", required = true) String networkid, @RequestParam(value = "tenantid", required = true) String tenantid,
            @RequestParam(value = "bind", required = false) String bind) throws Exception {

        if (StringUtils.isEmpty(networkid) || StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("networkid tenantid 不能为空");
        }
        ActionReturnUtil data = null;
        if (Constant.NETWORK_CALICO.equals(networkFlag)) {
            data = networkService.calicoNetworkdetail(networkid, tenantid, bind);
        } else {
            return ActionReturnUtil.returnErrorWithMsg("networkFlag 参数 配置错误！");
        }
        return data;
    }

    @RequestMapping(value = "/network/create", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(isolation = Isolation.DEFAULT)
    public ActionReturnUtil networkCreate(@ModelAttribute CreateNetwork createNetwork) throws Exception {

        return networkService.networkCreate(createNetwork);

    }

    /**
     * create subnet
     * 
     * @param networkid
     * @param subnetname
     * @return
     */
    @RequestMapping(value = "/subnetwork/create", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil subnetCreate(@RequestParam(value = "networkid", required = true) String networkid,
            @RequestParam(value = "subnetname", required = true) String subnetname) throws Exception {

        return networkService.subnetworkCreate(networkid, subnetname);

    }

    @RequestMapping(value = "/subnetwork/update", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public ActionReturnUtil subnetupdate(@RequestParam(value = "subnetid", required = true) String subnetid, String namespace) throws Exception {
        if (StringUtils.isEmpty(subnetid) || StringUtils.isEmpty(namespace)) {
            return ActionReturnUtil.returnErrorWithMsg("subnetid or namespace can not be null");
        }
        return networkService.subnetworkupdatebinding(subnetid, namespace);
    }

    @RequestMapping(value = "/subnetwork/checked", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public ActionReturnUtil subnetchecked(@RequestParam(value = "subnetid", required = true) String subnetid, String subnetname) throws Exception {
        if (StringUtils.isEmpty(subnetid) || StringUtils.isEmpty(subnetname)) {
            return ActionReturnUtil.returnErrorWithMsg("subnetid or subnetname can not be null");
        }
        // update subnets
        ActionReturnUtil data = networkService.subnetChecked(subnetid, subnetname);
        return data;
    }

    @RequestMapping(value = "/network/removeBingSubnet", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public ActionReturnUtil removeBingSubnet(String namespace) throws Exception {
        if (StringUtils.isEmpty(namespace)) {
            return ActionReturnUtil.returnErrorWithMsg("namespace can not be null");
        }
        ActionReturnUtil data = networkService.subnetRemoveBing(namespace);
        return data;
    }

    /**
     * delete network form db
     * 
     * @param networkid
     * @return
     */
    @RequestMapping(value = "/network/delete", method = RequestMethod.DELETE)
    @ResponseBody
    @Transactional
    public ActionReturnUtil networkDelete(@RequestParam(value = "networkid", required = true) String networkid) throws Exception {

        return networkService.networkDelete(networkid);

    }

    @RequestMapping(value = "/subnetwork/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil subnetDelete(@RequestParam(value = "subnetid", required = true) String subnetid) throws Exception {
        networkService.subnetworkDelete(subnetid);
        return ActionReturnUtil.returnSuccessWithData("delete subnet success");
    }

    @RequestMapping(value = "/network/Topology", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createNetwrokTopology(@RequestParam(value = "networkid", required = true) String networkidfrom, String networkidto) throws Exception {
        if (StringUtils.isEmpty(networkidfrom)) {
            return ActionReturnUtil.returnErrorWithMsg("networkid can not be null");
        }

        NetworkCalico networkfrom = networkService.getnetworkbyNetworkid(networkidfrom);
        if (networkfrom == null) {
            return ActionReturnUtil.returnErrorWithMsg("networkidfrom is error");
        }

        NetworkCalico networkto = networkService.getnetworkbyNetworkid(networkidfrom);
        if (networkto == null) {
            return ActionReturnUtil.returnErrorWithMsg("networkidto is error");
        }
        String networknamefrom = networkfrom.getNetworkname();
        String networknameto = networkto.getNetworkname();
        NetworkTopology topology2 = networkService.getTopologybyNetworkidfromAndNetworkidto(networkidfrom, networkidto, networknamefrom, networknameto);
        if (topology2 != null) {
            return ActionReturnUtil.returnErrorWithMsg("network " + networknamefrom + " to " + networknameto + " Topology was existed!");
        }
        Date date = DateUtil.getCurrentUtcTime();
        NetworkTopology topology = new NetworkTopology();
        topology.setCreatetime(date);
        topology.setNetId(networkidfrom);
        topology.setNetName(networknamefrom);
        topology.setTopology(networknamefrom + "_" + networknameto);
        topology.setDestinationid(networkidto);
        topology.setDestinationname(networknameto);
        networkService.createNetworkTopology(topology);

        // 查询from 和 to 对应的 ns并返回
        List<NamespceBindSubnet> listfrom = networkService.getsubnetbynetworkid(networkidfrom);
        List<String> nsfromlist = new ArrayList<>();
        for (NamespceBindSubnet namespceBindSubnet : listfrom) {
            nsfromlist.add(namespceBindSubnet.getNamespace());
        }

        List<NamespceBindSubnet> listto = networkService.getsubnetbynetworkid(networkidto);
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

    @RequestMapping(value = "/network/Topology", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteNetwrokTopology(@RequestParam(value = "networkid", required = true) String networkidfrom, String networkidto) throws Exception {
        if (StringUtils.isEmpty(networkidfrom)) {
            return ActionReturnUtil.returnErrorWithMsg("networkid can not be null");
        }

        NetworkCalico networkfrom = networkService.getnetworkbyNetworkid(networkidfrom);
        if (networkfrom == null) {
            return ActionReturnUtil.returnErrorWithMsg("networkidfrom is error");
        }

        NetworkCalico networkto = networkService.getnetworkbyNetworkid(networkidfrom);
        if (networkto == null) {
            return ActionReturnUtil.returnErrorWithMsg("networkidto is error");
        }
        String networknamefrom = networkfrom.getNetworkname();
        String networknameto = networkto.getNetworkname();
        NetworkTopology topology2 = networkService.getTopologybyNetworkidfromAndNetworkidto(networkidfrom, networkidto, networknamefrom, networknameto);
        if (topology2 == null) {
            return ActionReturnUtil.returnErrorWithMsg("network " + networknamefrom + " to " + networknameto + " Topology wasn't exist!");
        }

        networkService.deletetopologybyId(topology2.getId());
        // 查询from 和 to 对应的 ns并返回
        List<NamespceBindSubnet> listfrom = networkService.getsubnetbynetworkid(networkidfrom);
        List<String> nsfromlist = new ArrayList<>();
        for (NamespceBindSubnet namespceBindSubnet : listfrom) {
            nsfromlist.add(namespceBindSubnet.getNamespace());
        }

        List<NamespceBindSubnet> listto = networkService.getsubnetbynetworkid(networkidto);
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

    @RequestMapping(value = "/network/hasTopology", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil hasTopology(@RequestParam(value = "networkid", required = true) String networkid) throws Exception {
        if (StringUtils.isEmpty(networkid)) {
            return ActionReturnUtil.returnErrorWithMsg("networkid can not be null");
        }
        Map<String, Object> data = new HashMap<String, Object>();
        List<NetworkTopology> list1 = networkService.getTopologybyNetworkid(networkid);

        List<NetworkTopology> list2 = networkService.getTopologybydestination(networkid);
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

    @RequestMapping(value = "/network/getTopologys", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTopologyBytenantid(@RequestParam(value = "tenantid", required = true) String tenantid) throws Exception {
        if (StringUtils.isEmpty(tenantid)) {
            return ActionReturnUtil.returnErrorWithMsg("tenantid can not be null");
        }
        List<NetworkCalico> list = networkService.getnetworkbyTenantid(tenantid);
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Integer> tem = new HashMap<>();
        List<Map<String, Object>> listnetnodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> listnetedges = new ArrayList<Map<String, Object>>();
        int location = 0;
        for (NetworkCalico networkCalico : list) {
            // NetworkTopologyExample nt1 = new NetworkTopologyExample();
            // nt1.createCriteria().andNetIdEqualTo(networkCalico.getNetworkid());
            // List<NetworkTopology> list1 =
            // networkTopologyMapper.selectByExample(nt1);
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

    @RequestMapping(value = "/network/init", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil tenantList() throws Exception {

        ActionReturnUtil data = networkService.netwrokInit(null);
        return data;
    }

    public String getNetworkFlag() {
        return networkFlag;
    }

    public void setNetworkFlag(String networkFlag) {
        this.networkFlag = networkFlag;
    }
}
