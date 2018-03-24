package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.dao.ci.StageTypeMapper;
import com.harmonycloud.dao.ci.bean.StageType;
import com.harmonycloud.service.platform.service.ci.StageTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-15
 * @Modified
 */
@Service
public class StageTypeServiceImpl implements StageTypeService{
    @Autowired
    StageTypeMapper stageTypeMapper;

    @Override
    public List<StageType> queryByType(String type) {
        return stageTypeMapper.queryByType(type);
    }
}
