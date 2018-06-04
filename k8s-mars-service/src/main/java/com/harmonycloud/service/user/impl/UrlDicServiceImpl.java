package com.harmonycloud.service.user.impl;

import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.user.UrlDicMapper;
import com.harmonycloud.dao.user.bean.UrlDic;
import com.harmonycloud.dao.user.bean.UrlDicExample;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.user.UrlDicService;
import com.harmonycloud.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class UrlDicServiceImpl implements UrlDicService{
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlDicServiceImpl.class);
    @Autowired
    private UrlDicMapper urlDicMapper;
    @Autowired
    private HarborProjectService harborProjectService;
    @Autowired
    private UserService userService;
    /**
     * 获取url对应的map(测试使用)
     *
     * @return
     * @throws Exception
     */
    @Override
    public Map getUrlMap() throws Exception {
        UrlDicExample example = this.getExample();
        //测试 TODO
        example.createCriteria().andModuleEqualTo("appcenter");
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
        //test TODO
        ImageRepository repository = new ImageRepository();
        repository.setTenantId("B812870533934326AF6D532F1363D1E3");
        List<ImageRepository> imageRepositories = harborProjectService.listRepositories(repository);
        repository.setProjectId("6042eef939f94bca980d6afbb349590f");
         imageRepositories = harborProjectService.listRepositories(repository);
        userService.changePhone("w_zhangkui","13071815676");
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
