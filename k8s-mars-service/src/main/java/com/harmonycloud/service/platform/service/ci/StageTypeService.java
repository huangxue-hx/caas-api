package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.dao.ci.bean.StageType;

import java.util.List;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-15
 * @Modified
 */
public interface StageTypeService {
    List<StageType> queryByType(String type) throws Exception;
}
