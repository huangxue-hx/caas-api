package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.harbor.ImageTagDescMapper;
import com.harmonycloud.dao.harbor.bean.ImageTagDesc;
import com.harmonycloud.service.platform.service.harbor.HarborImageTagDescService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by dengyl on 2019-04-23
 */
@Service
public class HarborImageTagDescServiceImpl implements HarborImageTagDescService {

    @Autowired
    private ImageTagDescMapper imageTagDescMapper;


    @Override
    public ImageTagDesc select(Integer repositoryId, String imageName, String tagName) {
        List<ImageTagDesc> list = selectList(repositoryId, imageName, tagName);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }

    @Override
    public List<ImageTagDesc> selectList(Integer repositoryId, String imageName, String tagName) {
        return imageTagDescMapper.selectList(repositoryId, imageName, tagName);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean create(ImageTagDesc desc) throws Exception {
        if (imageTagDescMapper.insertSelective(desc) != CommonConstant.NUM_ONE) {
            throw new MarsRuntimeException(ErrorCodeMessage.SAVE_FAIL);
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(ImageTagDesc desc) throws Exception {
        if (imageTagDescMapper.updateByPrimaryKeySelective(desc) != CommonConstant.NUM_ONE) {
            throw new MarsRuntimeException(ErrorCodeMessage.SAVE_FAIL);
        }

        return true;
    }


}
