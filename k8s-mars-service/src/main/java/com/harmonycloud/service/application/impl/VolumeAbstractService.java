package com.harmonycloud.service.application.impl;

import com.harmonycloud.dto.application.PersistentVolumeDto;
import com.harmonycloud.service.platform.bean.UpdateVolume;
import org.springframework.stereotype.Service;


/**
 * Created by zhangkui on 2018/1/4.
 * Volume相关公共服务
 */

@Service
public abstract class VolumeAbstractService {

    protected PersistentVolumeDto convertCreateVolumeDto(UpdateVolume pv, String namespace, String serviceName){
        return this.convertCreateVolumeDto(pv,namespace,null,serviceName);
    }

    protected PersistentVolumeDto convertCreateVolumeDto(UpdateVolume pv, String namespace, String serviceType, String serviceName){
        PersistentVolumeDto persistentVolumeDto = new PersistentVolumeDto();
        persistentVolumeDto.setNamespace(namespace);
        persistentVolumeDto.setPvcName(pv.getPvcName());
        persistentVolumeDto.setCapacity(pv.getPvcCapacity());
        persistentVolumeDto.setProjectId(pv.getProjectId());
        persistentVolumeDto.setReadOnly(Boolean.parseBoolean(pv.getReadOnly()));
        persistentVolumeDto.setBindOne(Boolean.parseBoolean(pv.getPvcBindOne()));
        persistentVolumeDto.setVolumeName(pv.getName());
        persistentVolumeDto.setServiceType(serviceType);
        persistentVolumeDto.setServiceName(serviceName);
        return persistentVolumeDto;
    }


}