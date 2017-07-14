package com.harmonycloud.dao.application;

import java.util.List;

public interface NodePortMapper {
	/**
	 * 增加nodePort
	 * 
	 */
	int insert(Integer nodePort);
	
	  /**
		 * 获取所有nodePort
		 * 
		 */
	List<Integer> list();
	
	/**
	 * 根据nodePort删除
	 * @param id
	 */
    int delete(Integer nodePort);
    
    /**
	 * 根据nodePort查询是否存在
	 * @param id
	 */
    int getnodeport(Integer nodePort);
    
    /**
   	 * 根据id查询nodeport
   	 * @param id
   	 */
     int getnodeportbyid(Integer id);
       
       /**
     	 * 根据nodePort查询id
     	 * @param nodePort
     	 */
    int getidbynodeport(Integer nodePort);
	    
}
