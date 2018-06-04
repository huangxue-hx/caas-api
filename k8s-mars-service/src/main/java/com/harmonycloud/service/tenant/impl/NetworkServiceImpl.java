package com.harmonycloud.service.tenant.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.UUIDUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.network.NamespceBindSubnetMapper;
import com.harmonycloud.dao.network.NetworkCalicoMapper;
import com.harmonycloud.dao.network.NetworkTopologyMapper;
import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NamespceBindSubnetExample;
import com.harmonycloud.dao.network.bean.NetworkCalico;
import com.harmonycloud.dao.network.bean.NetworkCalicoExample;
import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dao.network.bean.NetworkTopologyExample;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.SubNetwork;
import com.harmonycloud.dto.tenant.CreateNetwork;
import com.harmonycloud.dto.tenant.NetworkBean;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.platform.bean.ProviderPlugin;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NetworkService;
import com.harmonycloud.service.tenant.TenantService;


@Service
@Transactional(rollbackFor = Exception.class)
public class NetworkServiceImpl implements NetworkService {

    @Autowired
    TenantBindingMapper tenantBindingMapper;

    @Autowired
    NetworkCalicoMapper networkCalicoMapper;

    @Autowired
    NamespceBindSubnetMapper namespceBindSubnetMapper;

    @Autowired
    NetworkTopologyMapper networkTopologyMapper;

    @Autowired
    private TenantService tenantService;

    @Value("#{propertiesReader['network.openstackurl']}")
    private String openstackUrl;

    @Value("#{propertiesReader['network.version']}")
    private String version;

    @Autowired
    ClusterService clusterService;

//    NetworkServiceImpl() throws Exception{
//        version = clusterService.getPlatformCluster().getNetwork().getVersion();
//    }

    @Autowired
    com.harmonycloud.k8s.service.NamespaceService namespaceService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public List<Map<String, Object>> networkList(String tenantid) throws Exception {
        List<Map<String, Object>> listnet = new ArrayList<Map<String, Object>>();
        NetworkCalicoExample example = new NetworkCalicoExample();
        if (!(tenantid == CommonConstant.NULL || CommonConstant.UNDEFINED.equals(tenantid))) {
            example.createCriteria().andTenantidEqualTo(tenantid);
        }
        List<NetworkCalico> list = networkCalicoMapper.selectByExample(example);
        for (NetworkCalico networkCalico : list) {
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> tenant = new HashMap<>();
            Date createTime = networkCalico.getCreatetime();
            SimpleDateFormat adf = new SimpleDateFormat(CommonConstant.UTCTIME);
            String date = adf.format(createTime);
            tenant.put(CommonConstant.NAME, networkCalico.getTenantname());
            tenant.put(CommonConstant.TENANTID, networkCalico.getTenantid());
            map.put(CommonConstant.ANNOTATION, networkCalico.getAnnotation());
            map.put(CommonConstant.NETWORKID, networkCalico.getNetworkid());
            map.put(CommonConstant.NAME, networkCalico.getNetworkname());
            map.put(CommonConstant.TIME, date);
            map.put(CommonConstant.TENANT, tenant);
            listnet.add(map);
        }
        return listnet;
    }

    @SuppressWarnings(CommonConstant.UNCHECKED)
    @Override
    public ActionReturnUtil networkCreate(CreateNetwork createNetwork) throws Exception {
        String tenantName = createNetwork.getTenant().getTenantname();
        String networkName = createNetwork.getNetworkname();
        AssertUtil.notBlank(createNetwork.getTenant().getTenantname(), DictEnum.TENANT_NAME);
        AssertUtil.notBlank(createNetwork.getNetworkname(), DictEnum.NETWORK_NAME);
        String annotation = createNetwork.getAnnotation();
        NetworkCalico networkCalico = getnetworkbyname(networkName, tenantName);
        if (networkCalico != null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NAME_EXIST);
        }
        NetworkCalico networkid2 = getnetworkbyNetworkid(createNetwork.getTenant().getTenantid());
        if (networkid2 != null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.TENANT_NETWORK_EXIST);
        }
        NetworkBean network = new NetworkBean();
        network.setAnnotation(annotation);
        network.setNetworkName(networkName);
        network.setTenantName(tenantName);
        List<SubNetwork> subnets = createNetwork.getSubnets();
        List<SubNetwork> s = new ArrayList<SubNetwork>();
        String networkid = UUIDUtil.getUUID();
        Date date = DateUtil.getCurrentUtcTime();
        NetworkCalico record = new NetworkCalico();
        record.setAnnotation(annotation);
        record.setNetworkname(networkName);
        record.setNetworkid(networkid);
        record.setTenantid(createNetwork.getTenant().getTenantid());
        record.setTenantname(tenantName);
        record.setCreatetime(date);
        networkCalicoMapper.insertSelective(record);
        if (subnets != null) {
            for (SubNetwork sub : subnets) {
                ActionReturnUtil subnetcreate = subnetworkCreate(networkid, sub.getSubname());
                if (!subnetcreate.isSuccess()) {
                    return subnetcreate;
                }
                s.add(sub);
            }
        }
        network.setSubnets(s);
        return ActionReturnUtil.returnSuccessWithData(network);
    }

    public ActionReturnUtil calicoNetworkdetail(String networkid, String tenantid, String bind) throws Exception {

        List<Map<String, Object>> listnet = new ArrayList<Map<String, Object>>();
        NetworkCalicoExample example1 = new NetworkCalicoExample();
        example1.createCriteria().andNetworkidEqualTo(networkid);
        List<NetworkCalico> list1 = networkCalicoMapper.selectByExample(example1);
        if (list1.size() < 1) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_FOUND, DictEnum.NETWORK_ID.phrase(), true);
        }
        Map<String, Object> maps = new HashMap<String, Object>();
        Map<String, Object> maps2 = new HashMap<String, Object>();
        Date createTime = list1.get(0).getCreatetime();
        SimpleDateFormat adf = new SimpleDateFormat(CommonConstant.UTCTIME);
        String date = adf.format(createTime);
        maps.put(CommonConstant.NAME, list1.get(0).getNetworkname());
        maps.put(CommonConstant.TIME, date);
        maps.put(CommonConstant.NETWORKID, list1.get(0).getNetworkid());
        maps.put(CommonConstant.ANNOTATION, list1.get(0).getAnnotation());
        // tenant
        maps2.put(CommonConstant.NAME, list1.get(0).getTenantname());
        maps2.put(CommonConstant.TENANTID, list1.get(0).getTenantid());
        maps.put(CommonConstant.TENANT, maps2);
        NamespceBindSubnetExample example = new NamespceBindSubnetExample();
        example.createCriteria().andNetIdEqualTo(networkid);
        List<Map<String, Object>> sub = new ArrayList<Map<String, Object>>();
        List<NamespceBindSubnet> list = namespceBindSubnetMapper.selectByExample(example);
        for (NamespceBindSubnet namespceBindSubnet : list) {
            if (!StringUtils.isEmpty(bind) && namespceBindSubnet.getBinding() != 1) {
                Map<String, Object> maps3 = new HashMap<String, Object>();
                // Integer binding = namespceBindSubnet.getBinding();
                maps3.put(CommonConstant.BINDING, namespceBindSubnet.getBinding() == 0 ? CommonConstant.FALSE : CommonConstant.TRUE);
                maps3.put(CommonConstant.RESULTNAMESPACE, namespceBindSubnet.getBinding() == 0 ? CommonConstant.NULL : namespceBindSubnet.getNamespace());
                maps3.put(CommonConstant.NAME, namespceBindSubnet.getSubnetName());
                maps3.put(CommonConstant.SUBNETID, namespceBindSubnet.getSubnetId());
                sub.add(maps3);
            } else if (StringUtils.isEmpty(bind)) {
                Map<String, Object> maps3 = new HashMap<String, Object>();
                // Integer binding = namespceBindSubnet.getBinding();
                maps3.put(CommonConstant.BINDING, namespceBindSubnet.getBinding() == 0 ? CommonConstant.FALSE : CommonConstant.TRUE);
                maps3.put(CommonConstant.RESULTNAMESPACE, namespceBindSubnet.getBinding() == 0 ? CommonConstant.NULL : namespceBindSubnet.getNamespace());
                maps3.put(CommonConstant.NAME, namespceBindSubnet.getSubnetName());
                maps3.put(CommonConstant.SUBNETID, namespceBindSubnet.getSubnetId());
                sub.add(maps3);
            }
        }
        maps.put(CommonConstant.SUBNETS, sub);
        listnet.add(maps);
        return ActionReturnUtil.returnSuccessWithData(listnet);
    }

    @Override
    public NetworkCalico getnetworkbyname(String networkName, String tenantName) {
        NetworkCalicoExample example = new NetworkCalicoExample();
        example.createCriteria().andTenantnameEqualTo(tenantName).andNetworknameEqualTo(networkName);
        List<NetworkCalico> netlist = networkCalicoMapper.selectByExample(example);
        if (netlist.size() <= 0) {
            return null;
        }
        return netlist.get(0);
    }

    @Override
    public List<Map<String, Object>> subnetworklistbynetworkid(String networkid) {
        NamespceBindSubnetExample example = new NamespceBindSubnetExample();
        example.createCriteria().andNetIdEqualTo(networkid);
        List<NamespceBindSubnet> list = namespceBindSubnetMapper.selectByExample(example);
        return null;
    }

    @Override
    public NamespceBindSubnet getsubnetbySubnetname(String networkid, String subnetname) {
        NamespceBindSubnetExample example = new NamespceBindSubnetExample();
        example.createCriteria().andNetIdEqualTo(networkid).andSubnetNameEqualTo(subnetname);
        List<NamespceBindSubnet> list = namespceBindSubnetMapper.selectByExample(example);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public ActionReturnUtil subnetworkCreate(String networkid, String subnetname) throws Exception {
        NamespceBindSubnet getsubnetbySubnetname = getsubnetbySubnetname(networkid, subnetname);
        if (getsubnetbySubnetname != null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NAME_EXIST);
        }
        Date date = DateUtil.getCurrentUtcTime();

        // insert subnets
        String subnetId = UUIDUtil.getUUID();
        NamespceBindSubnet subnet = new NamespceBindSubnet();
        subnet.setBinding(0);
        subnet.setCreateTime(date);
        subnet.setSubnetName(subnetname);
        subnet.setNetId(networkid);
        subnet.setSubnetId(subnetId);
        namespceBindSubnetMapper.insertSelective(subnet);
        return ActionReturnUtil.returnSuccessWithData(subnet);
    }

    @Override
    public ActionReturnUtil subnetworkupdatebinding(String subnetid, String namespace) throws Exception {
        Date date = DateUtil.getCurrentUtcTime();
        NamespceBindSubnet subnet = this.getsubnetbySubnetid(subnetid);
        if (subnet == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_FOUND, DictEnum.SUB_NETWORK_ID.phrase(),true);
        }
        subnet.setUpdateTime(date);
        subnet.setNamespace(namespace);
        subnet.setBinding(1);
        namespceBindSubnetMapper.updateByPrimaryKey(subnet);
        return ActionReturnUtil.returnSuccessWithData(subnet);
    }

    @Override
    public NamespceBindSubnet getsubnetbySubnetid(String subnetid) {
        NamespceBindSubnetExample example = new NamespceBindSubnetExample();
        example.createCriteria().andSubnetIdEqualTo(subnetid);
        List<NamespceBindSubnet> list = namespceBindSubnetMapper.selectByExample(example);
        return list.size() <= 0 ? null : list.get(0);
    }

    @Override
    public NamespceBindSubnet getsubnetbySubnetnameAndSubnetid(String subnetid, String subnetname) {
        NamespceBindSubnetExample example = new NamespceBindSubnetExample();
        example.createCriteria().andSubnetIdEqualTo(subnetid).andSubnetNameEqualTo(subnetname);
        List<NamespceBindSubnet> list = namespceBindSubnetMapper.selectByExample(example);
        return list.size() <= 0 ? null : list.get(0);
    }

    @Override
    public ActionReturnUtil subnetRemoveBing(String namespace) {
        Date date = DateUtil.getCurrentUtcTime();
        // update subnets
        NamespceBindSubnetExample example = new NamespceBindSubnetExample();
        example.createCriteria().andNamespaceEqualTo(namespace);
        List<NamespceBindSubnet> subnets = namespceBindSubnetMapper.selectByExample(example);
        if (subnets.size() > 0) {
            NamespceBindSubnet subnet = subnets.get(0);
            namespceBindSubnetMapper.deleteByPrimaryKey(subnet.getId());
        }

        return ActionReturnUtil.returnSuccessWithData("removeBing success");
    }

    @Override
    public NetworkCalico getnetworkbyNetworkid(String networkid) {
        NetworkCalicoExample netexample = new NetworkCalicoExample();
        netexample.createCriteria().andNetworkidEqualTo(networkid);
        List<NetworkCalico> list = networkCalicoMapper.selectByExample(netexample);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public ActionReturnUtil networkDelete(String networkid) throws Exception {
        NetworkCalico network = getnetworkbyNetworkid(networkid);
        if (network == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_FOUND, DictEnum.NETWORK.phrase(),true);
        }
        NamespceBindSubnetExample example = new NamespceBindSubnetExample();
        example.createCriteria().andNetIdEqualTo(networkid);
        List<NamespceBindSubnet> subnets = namespceBindSubnetMapper.selectByExample(example);
        for (NamespceBindSubnet namespceBindSubnet : subnets) {
            namespceBindSubnetMapper.deleteBySubnetId(namespceBindSubnet.getSubnetId());
        }
        networkCalicoMapper.deleteByNetworkId(networkid);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public void subnetworkDelete(String subnetid) throws Exception {
        namespceBindSubnetMapper.deleteBySubnetId(subnetid);
    }

    @Override
    public NetworkTopology getTopologybyNetworkidfromAndNetworkidto(String networkidfrom, String networkidto, String networknamefrom, String networknameto) {
        NetworkTopologyExample nt = new NetworkTopologyExample();
        nt.createCriteria().andNetIdEqualTo(networkidfrom).andDestinationidEqualTo(networkidto).andTopologyEqualTo(networknamefrom + CommonConstant.UNDER_LINE + networknameto);
        List<NetworkTopology> list = networkTopologyMapper.selectByExample(nt);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public void createNetworkTopology(NetworkTopology networkTopology) {
        networkTopologyMapper.insertSelective(networkTopology);
    }

    @Override
    public List<NamespceBindSubnet> getsubnetbynetworkid(String networkid) {
        NamespceBindSubnetExample NSSubfrom = new NamespceBindSubnetExample();
        NSSubfrom.createCriteria().andNetIdEqualTo(networkid);
        List<NamespceBindSubnet> list = namespceBindSubnetMapper.selectByExample(NSSubfrom);
        return list;
    }

    @Override
    public NetworkTopology deletetopologybyId(Integer id) {
        networkTopologyMapper.deleteByPrimaryKey(id);
        return null;
    }

    @Override
    public List<NetworkTopology> getTopologybyNetworkid(String networkid) {
        NetworkTopologyExample nt1 = new NetworkTopologyExample();
        nt1.createCriteria().andNetIdEqualTo(networkid);
        List<NetworkTopology> list1 = networkTopologyMapper.selectByExample(nt1);
        return list1;
    }

    @Override
    public List<NetworkTopology> getTopologybydestination(String networkid) {
        NetworkTopologyExample nt1 = new NetworkTopologyExample();
        nt1.createCriteria().andDestinationidEqualTo(networkid);
        List<NetworkTopology> list1 = networkTopologyMapper.selectByExample(nt1);
        return list1;
    }

    @Override
    public List<NetworkCalico> getnetworkbyTenantid(String tenantid) {
        NetworkCalicoExample example = new NetworkCalicoExample();
        example.createCriteria().andTenantidEqualTo(tenantid);
        List<NetworkCalico> list = networkCalicoMapper.selectByExample(example);
        return list;
    }

    @Override
    public ActionReturnUtil subnetChecked(String subnetid, String subnetname) throws Exception {
        NamespceBindSubnet subnet = this.getsubnetbySubnetnameAndSubnetid(subnetid, subnetname);
        if (subnet == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_FOUND, DictEnum.NETWORK.phrase(),true);
        }
        if (subnet.getBinding() == 1) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NETWORK_ALREADY_BIND);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public List<NetworkTopology> getNetworkTopologyList(String networkid) {

        List<NetworkTopology> fromList = this.getTopologybyNetworkid(networkid);

        List<NetworkTopology> toList = this.getTopologybydestination(networkid);

        List<NetworkTopology> networkTopologies = new LinkedList<>();

        if (fromList != null && toList != null) {
            networkTopologies.addAll(fromList);
            networkTopologies.addAll(toList);
        }
        
        return networkTopologies;
    }

    @Override
    public ActionReturnUtil netwrokInit(Cluster cluster) throws Exception {
        // TODO Auto-generated method stub
        ActionReturnUtil updateInit = this.updateInit(cluster);
        return updateInit;
    }
    private Map<String, Object> getInitLables() {
        Map<String, Object> lables = new HashMap<>();
        lables.put(CommonConstant.INITKUBESYSTEM, CommonConstant.KUBE_SYSTEM);
        return lables;
    }
    public ActionReturnUtil updateInit(Cluster cluster) throws Exception {

        ObjectMeta objectMeta = new ObjectMeta();

        // objectMeta.setAnnotations(this.getAnnotations(namespaceDto));
        objectMeta.setLabels(this.getInitLables());
        objectMeta.setName(CommonConstant.KUBE_SYSTEM);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.KIND, CommonConstant.NAMESPACE);
        bodys.put(CommonConstant.METADATA, objectMeta);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);

        K8SClientResponse k8SClientResponse = namespaceService.update(headers, bodys, CommonConstant.KUBE_SYSTEM,cluster);

        if (!HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            logger.error("调用k8s初始化 kube-system 失败", k8SClientResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(k8SClientResponse.getBody());
        }
        return ActionReturnUtil.returnSuccessWithData("namespaces初始化成功！");

    }

    public ActionReturnUtil listProvider() throws Exception {
        List<ProviderPlugin> provider = new ArrayList<ProviderPlugin>();
        ProviderPlugin providerPlugin = new ProviderPlugin();
        providerPlugin.setIp(openstackUrl);
        providerPlugin.setName(Constant.NETWORK);
        providerPlugin.setVersion(version);
        provider.add(providerPlugin);
        return ActionReturnUtil.returnSuccessWithData(provider);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOpenstackUrl() {
        return openstackUrl;
    }

    public void setOpenstackUrl(String openstackUrl) {
        this.openstackUrl = openstackUrl;
    }

    @Override
    public ActionReturnUtil networkDeleteByTenantid(String tenantid) throws Exception {
        // TODO Auto-generated method stub
        List<NetworkCalico> getnetworkbyTenantid = getnetworkbyTenantid(tenantid);
        if (getnetworkbyTenantid.size() < 1) {
            return ActionReturnUtil.returnSuccess();
        }
        String networkid = getnetworkbyTenantid.get(0).getNetworkid();
        NamespceBindSubnetExample example = new NamespceBindSubnetExample();
        example.createCriteria().andNetIdEqualTo(networkid);
        List<NamespceBindSubnet> subnets = namespceBindSubnetMapper.selectByExample(example);
        for (NamespceBindSubnet namespceBindSubnet : subnets) {
            namespceBindSubnetMapper.deleteBySubnetId(namespceBindSubnet.getSubnetId());
        }
        networkCalicoMapper.deleteByNetworkId(networkid);
        return ActionReturnUtil.returnSuccessWithData("delete network success");
    }

    @Override
    public List<NetworkTopology> getTrustNetworkTopologyList(String networkid) throws Exception {
        List<NetworkTopology> toList = new ArrayList<NetworkTopology>();
        toList = this.getTopologybydestination(networkid);
        return toList;
    }

}
