package com.harmonycloud.k8s.service;

import com.harmonycloud.common.util.HttpK8SClientUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andy on 17-1-16.
 */
@Service
public class PersistentvolumeService {

    private String surfix="/persistentvolumes";
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentvolumeService.class);

    /**
     * 获取pv,name不为null时,获取单个pv,否则获取所有pv列表
     * @param beanClass
     * @param k8sUrl
     * @param headers
     * @param bodys
     * @param name
     * @param <T>
     * @return
     */
    public <T> T getClusterRolebindings(Class<T> beanClass, String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys, String name){
        try {

            if(StringUtils.isEmpty(name)){
                String url=k8sUrl+Constant.PV_VERSION+surfix;
                String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);
                T cList= (T) JsonUtil.jsonToPojo(body.toString(), beanClass);

                return cList;
            }

            String url=k8sUrl+Constant.PV_VERSION+surfix+"/"+name;
            String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);

            if(StringUtils.isEmpty(body)){
                return null;
            }

            T c= (T) JsonUtil.jsonToPojo(body.toString(), beanClass);
            return  c;

        } catch (Exception e) {
            LOGGER.warn("获取pv失败", e);
        }
        return null;
    }

    public static void main(String[] args) {
        Map<String, Object> headers = new HashMap<String, Object>();
        Map<String, Object> bodys = new HashMap<>();
        headers.put("authorization", "Bearer 330957b867a3462ea457bec41410624b");


        PersistentVolumeList pvs = new PersistentvolumeService().getClusterRolebindings(PersistentVolumeList.class,
                "https://10.10.102.25:6443",headers,null, null);

        if(pvs != null){
            bodys.put("labelSelector", "nephele_tenantid_c4b6ae6cd95f412c881814666978e3af=c4b6ae6cd95f412c881814666978e3af");
            PersistentVolume persistentVolume = new PersistentvolumeService().getClusterRolebindings(
                    PersistentVolume.class, "https://10.10.102.25:6443", headers, bodys,
                    pvs.getItems().get(0).getMetadata().getName());

        }

    }

}
