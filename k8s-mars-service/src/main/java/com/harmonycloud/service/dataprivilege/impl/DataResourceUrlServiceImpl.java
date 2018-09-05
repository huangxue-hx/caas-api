package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.dao.dataprivilege.DataResourceUrlMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataResourceUrl;
import com.harmonycloud.dao.dataprivilege.bean.DataResourceUrlExample;
import com.harmonycloud.service.dataprivilege.DataResourceUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 15988 on 2018/7/2.
 */
@Service
public class DataResourceUrlServiceImpl implements DataResourceUrlService{
    @Autowired
    private DataResourceUrlMapper dataResourceUrlMapper;
    /**
     * 获取拦截权限Url字典
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,DataResourceUrl> getDataResourceUrlMap() throws Exception {
        DataResourceUrlExample example = this.getExample();
        List<DataResourceUrl> DataResourceUrlList = this.dataResourceUrlMapper.selectByExample(example);
        Map<String, DataResourceUrl> result = new HashMap<>();
        //获取所有的模块
        if (CollectionUtils.isEmpty(DataResourceUrlList)){
            return result;
        }
        for (DataResourceUrl dataResourceUrl:DataResourceUrlList) {
            result.put(dataResourceUrl.getUrl().trim()+":"+dataResourceUrl.getMethod(),dataResourceUrl);
        }
        return result;
    }
    private DataResourceUrlExample getExample(){
        return new DataResourceUrlExample();
    }
}
