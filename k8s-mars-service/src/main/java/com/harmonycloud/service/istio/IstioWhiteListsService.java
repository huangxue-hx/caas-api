package com.harmonycloud.service.istio;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.WhiteListsDto;

/**
 * create by weg on 18-12-27.
 */
public interface IstioWhiteListsService {

    /**
     * 创建白名单策略
     * @param whiteListsDto whiteListsDto
     * @return  返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createWhiteListsPolicy(WhiteListsDto whiteListsDto) throws Exception;
    /**
     * 修改白名单策略
     * @param ruleId 规则id
     * @param whiteListsDto whiteListsDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateWhiteListsPolicy(String ruleId, WhiteListsDto whiteListsDto) throws Exception;

    /**
     * 关闭白名单策略
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名称
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil closeWhiteListsPolicy(String namespace, String ruleId, String deployName) throws Exception;

    /**
     * 开启白名单策略
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName deployName
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil openWhiteListsPolicy(String namespace,  String ruleId, String deployName) throws Exception;

    /**
     * 删除白名单策略
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName deployName
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteWhiteListsPolicy(String namespace, String ruleId, String deployName) throws Exception;

    /**
     * 获取白名单策略信息
     * @param namespace 分区名称
     * @param ruleId  规则id
     * @param deployName deployName
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getWhiteListsPolicy(String namespace, String ruleId, String deployName) throws Exception;
}
