package com.harmonycloud.service.user.impl;

import com.harmonycloud.dao.user.UrlDicMapper;
import com.harmonycloud.dao.user.bean.UrlDic;
import com.harmonycloud.dao.user.bean.UrlDicExample;
import com.harmonycloud.service.user.UrlDicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UrlDicServiceImpl implements UrlDicService{
    @Autowired
    UrlDicMapper urlDicMapper;
    /**
     * 获取url对应的map(测试使用)
     *
     * @return
     * @throws Exception
     */
    @Override
    public Map getUrlMap() throws Exception {
        UrlDicExample example = this.getExample();
        List<UrlDic> urlDics = this.urlDicMapper.selectByExample(example);
        Map<String, Map<String, List<UrlDic>>> result = new HashMap<>();
        //获取所有的模块
        if (CollectionUtils.isEmpty(urlDics)){
            return result;
        }
        Map<String, List<UrlDic>> modules = urlDics.stream().collect(Collectors.groupingBy(UrlDic::getModule));
        if (!CollectionUtils.isEmpty(modules)){
            for (Map.Entry<String, List<UrlDic>> entry : modules.entrySet()) {
                Map<String, List<UrlDic>> resource = new HashMap<>();
                //获取当前模块的所有的资源
                if (!CollectionUtils.isEmpty(entry.getValue())){
                    Map<String, List<UrlDic>> resources = entry.getValue().
                            stream().collect(Collectors.groupingBy(UrlDic::getResource));
                    if (!CollectionUtils.isEmpty(resources)){
                        for (Map.Entry<String, List<UrlDic>> entryResource : resources.entrySet()) {
                            //添加模块下资源权限信息
                            resource.put(entryResource.getKey(),entryResource.getValue());
                        }
                    }
                }
                if (!Objects.isNull(resource)){
                    //添加模块信息
                    result.put(entry.getKey(),resource);
                }
            }
        }
        if (!CollectionUtils.isEmpty(result)){
            for (Map.Entry<String, Map<String, List<UrlDic>>> entryResource : result.entrySet()) {
                for (Map.Entry<String, List<UrlDic>> entry : entryResource.getValue().entrySet()) {
                    //添加模块下资源权限信息
                    List<UrlDic> value = entry.getValue();
                    for (UrlDic urlDic:value) {
                        System.out.println(urlDic.getModule()+"|"+urlDic.getResource()+"|"+urlDic.getUrl());
                    }
                }
            }
        }
        return result;

    }

    /**
     * 获取拦截权限Url字典
     * @return
     * @throws Exception
     */
    public Map<String,UrlDic> getUrlDicMap() throws Exception {
        UrlDicExample example = this.getExample();
        List<UrlDic> urlDics = this.urlDicMapper.selectByExample(example);
        Map<String, UrlDic> result = new HashMap<>();
        //获取所有的模块
        if (CollectionUtils.isEmpty(urlDics)){
            return result;
        }
        for (UrlDic urlDic:urlDics) {
            result.put(urlDic.getUrl().trim(),urlDic);
        }
        return result;
    }
    private UrlDicExample getExample(){
        return new UrlDicExample();
    }
}
