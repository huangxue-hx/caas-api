package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.util.MessageUtil;
import com.harmonycloud.dao.ci.StageTypeMapper;
import com.harmonycloud.dao.ci.bean.StageType;
import com.harmonycloud.service.platform.service.ci.StageTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-15
 * @Modified
 */
@Service
public class StageTypeServiceImpl implements StageTypeService{
    @Autowired
    private StageTypeMapper stageTypeMapper;

    @Override
    public List<StageType> queryByType(String type) {
        List<StageType> stageTypes = stageTypeMapper.queryByType(type);
        if (!CollectionUtils.isEmpty(stageTypes)){
            stageTypes.stream().forEach(stageType -> stageType.setName(MessageUtil.getMessage(stageType.getName())));
        }
        return stageTypes;
    }
}
