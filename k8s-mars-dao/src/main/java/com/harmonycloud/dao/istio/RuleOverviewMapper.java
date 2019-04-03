package com.harmonycloud.dao.istio;

import com.harmonycloud.dao.istio.bean.RuleOverview;

import java.util.List;
import java.util.Map;

public interface RuleOverviewMapper {

    void deleteByPrimaryKey(String ruleId);

    void insert(RuleOverview record);

    List<RuleOverview> selectByRuleInfo(Map ruleInfo);

    Map<String, Object> selectRuleStatus(String ruleId);

    int updateByPrimaryKeySelective(RuleOverview record);

    int updateByPrimaryKey(RuleOverview record);

    void updateSwitchStatus(RuleOverview record);

    void updateDataStatus(RuleOverview ruleOverview);

    RuleOverview selectByRuleId(String ruleId);

    void  deleteIstioPolicy(Map ruleInfo);
}