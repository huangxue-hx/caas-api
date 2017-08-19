package com.harmonycloud.service.system.impl;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.service.system.SuperSaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 * Created by legendous on 2017/8/19.
 */
@Service
public class SuperSaleServiceImpl implements SuperSaleService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Double addSuperSaleRate(Double rate) throws Exception{
		if (rate == null && rate < 1.0){
			logger.error("输入的超卖系数有误，系数不能为空，且不能小于1，请重新输入!");
			throw new MarsRuntimeException("输入的超卖系数有误，系数不能为空，且不能小于1，请重新输入!");
		}
		return rate;
	}
}
