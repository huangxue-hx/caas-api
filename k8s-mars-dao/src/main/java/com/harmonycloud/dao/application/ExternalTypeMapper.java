package com.harmonycloud.dao.application;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.application.bean.ExternalTypeBean;


/**
 * Created by ly on 17/3/30.
 */
@Repository
public interface ExternalTypeMapper {

    /**
	 * 获取所有外部服务类型
	 * 
	 */
	public List<ExternalTypeBean> list() throws Exception; 
   
}
