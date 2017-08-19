package com.harmonycloud.api.system;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.service.system.SuperSaleService;
import com.harmonycloud.service.system.SystemConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by legendous on 2017/8/19.
 */
@RestController
@RequestMapping("/superSale")
public class SuperSaleServiceController {

	@Autowired
	SuperSaleService superSaleService;

	@Autowired
	SystemConfigService systemConfigService;

	@ResponseBody
	@RequestMapping(value = "/addSuperSaleRate", method = RequestMethod.POST)
	public ActionReturnUtil addSuperSaleRate(@RequestParam(value = "rate") Double rate) throws Exception {

		rate = 1.5;
		if (rate == null && rate < 1.0){
			ActionReturnUtil.returnErrorWithMsg("请输入正确的超卖系数!");
		}
		SystemConfig byId = systemConfigService.findById("40");
		byId.setConfigValue(rate.toString());
		systemConfigService.updateSystemConfig(byId);
		superSaleService.addSuperSaleRate(rate);
		return ActionReturnUtil.returnSuccess();
	}
}
