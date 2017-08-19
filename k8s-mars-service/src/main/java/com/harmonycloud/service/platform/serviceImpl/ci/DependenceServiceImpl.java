package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.cicd.DependenceDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.platform.service.ci.DependenceService;
import com.harmonycloud.service.tenant.PersistentVolumeService;
import com.harmonycloud.service.tenant.TenantService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anson on 17/7/29.
 */
@Service
public class DependenceServiceImpl implements DependenceService {

    @Autowired
    private PvService pvService;

    @Autowired
    private PersistentVolumeService persistentvolumeService;

    @Autowired
    private TenantService tenantService;


    @Override
    public ActionReturnUtil listByTenantId(String tenantId, String name) throws Exception {
        Cluster cluster = tenantService.getClusterByTenantid(tenantId);
        String label = "common = true";
        K8SClientResponse commonResponse = pvService.listPvBylabel(label, cluster);
        label = "cicd_tenantid =" + tenantId + ",common = false";
        K8SClientResponse privateResponse = pvService.listPvBylabel(label, cluster);

        ActionReturnUtil result = persistentvolumeService.listProvider();
        String server = null;
        String path = null;
        if (result.isSuccess()) {
            List<Map<String, Object>> providerList = (List<Map<String, Object>>) result.get("data");
            for (Map provider : providerList) {
                if (CommonConstant.NFS.equals((String) provider.get(CommonConstant.TYPE))) {
                    server = (String) provider.get(CommonConstant.ADDRESS);
                    path = (String) provider.get(CommonConstant.PATH);
                    break;
                }
            }
        }

        List<Map> pvDtos = new ArrayList<>();
        if (HttpStatusUtil.isSuccessStatus(commonResponse.getStatus())) {
            PersistentVolumeList persistentVolumeList = K8SClient.converToBean(commonResponse, PersistentVolumeList.class);
            List<PersistentVolume> items = persistentVolumeList.getItems();
            // 处理items返回页面需要的对象
            for (PersistentVolume pv : items) {
                Map pvDto = new HashMap();
                String displayName = pv.getMetadata().getName().replaceFirst("cicd-", "");
                if (name == null || displayName.contains(name)) {
                    pvDto.put("name", displayName);
                    pvDto.put("type", CommonConstant.NFS);
                    pvDto.put("server", server);
                    pvDto.put("serverPath", path + "/" + pv.getMetadata().getName());
                    pvDto.put("common", true);
                    pvDtos.add(pvDto);
                }
            }
        }
        if (HttpStatusUtil.isSuccessStatus(privateResponse.getStatus())) {
            PersistentVolumeList persistentVolumeList = K8SClient.converToBean(privateResponse, PersistentVolumeList.class);
            List<PersistentVolume> items = persistentVolumeList.getItems();
            // 处理items返回页面需要的对象
            for (PersistentVolume pv : items) {
                Map pvDto = new HashMap();
                String displayName = pv.getMetadata().getName().replaceFirst("cicd-", "");
                if (name == null || displayName.contains(name)) {
                    pvDto.put("name", displayName);
                    pvDto.put("type", CommonConstant.NFS);
                    pvDto.put("server", server);
                    pvDto.put("serverPath", path + "/" + pv.getMetadata().getName());
                    pvDto.put("common", false);
                    pvDtos.add(pvDto);
                }

            }
        }
        return ActionReturnUtil.returnSuccessWithData(pvDtos);
    }

    public ActionReturnUtil add(DependenceDto dependenceDto) throws Exception {
        if (StringUtils.isBlank(dependenceDto.getNfsServer()) || StringUtils.isBlank(dependenceDto.getPath())) {
            ActionReturnUtil result = persistentvolumeService.listProvider();
            if (result.isSuccess()) {
                List<Map<String, Object>> providerList = (List<Map<String, Object>>) result.get("data");
                for (Map provider : providerList) {
                    if (CommonConstant.NFS.equals((String) provider.get(CommonConstant.TYPE))) {
                        dependenceDto.setNfsServer((String) provider.get(CommonConstant.ADDRESS));
                        dependenceDto.setPath((String) provider.get(CommonConstant.PATH));
                        break;
                    }
                }
            }
        }

        PersistentVolume persistentVolume = new PersistentVolume();
        // 设置metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName("cicd-" + dependenceDto.getName());
        Map<String, Object> labels = new HashMap<>();
        labels.put("cicd_tenantid", dependenceDto.getTenantid());
        labels.put("common", dependenceDto.isCommon() ? "true" : "false");

        metadata.setLabels(labels);
        // 设置spec
        PersistentVolumeSpec spec = new PersistentVolumeSpec();
        Map<String, Object> cap = new HashMap<>();
        cap.put(CommonConstant.STORAGE, "1Mi");
        spec.setCapacity(cap);
        spec.setPersistentVolumeReclaimPolicy(CommonConstant.RECYCLE);
        NFSVolumeSource nfs = new NFSVolumeSource();
        // 设置nfs地址
        nfs.setPath(dependenceDto.getPath() + "/" + dependenceDto.getName());
        nfs.setServer(dependenceDto.getNfsServer());
        spec.setNfs(nfs);
        List<String> accessModes = new ArrayList<>();
        accessModes.add(CommonConstant.READONLYMANY);
        spec.setAccessModes(accessModes);
        persistentVolume.setMetadata(metadata);
        persistentVolume.setSpec(spec);
        persistentVolume.setApiVersion("v1");
        persistentVolume.setKind(CommonConstant.PERSISTENTVOLUME);
        Cluster cluster = tenantService.getClusterByTenantid(dependenceDto.getTenantid());
        try {
            ActionReturnUtil addPv = pvService.addPv(persistentVolume, cluster);
        }catch(Exception e){
            if(e.getMessage().contains("already exists")){
                return ActionReturnUtil.returnErrorWithMsg("依赖\""+dependenceDto.getName()+"\"已存在，请重新输入");
            }else{
                return ActionReturnUtil.returnErrorWithMsg("创建依赖失败");
            }
        }

        return ActionReturnUtil.returnSuccess();

    }

    @Override
    public ActionReturnUtil delete(String name) throws Exception {
        return pvService.delPvByName("cicd-" + name, null);
    }
}
