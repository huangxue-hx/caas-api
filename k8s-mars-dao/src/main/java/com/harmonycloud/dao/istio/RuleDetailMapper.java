package com.harmonycloud.dao.istio;

import com.harmonycloud.dao.istio.bean.RuleDetail;

import java.util.List;

public interface RuleDetailMapper {

    void deleteByPrimaryKey(String ruleId);

    void insert(RuleDetail record);

    List<RuleDetail> selectByPrimaryKey(String ruleId);

    void updateByPrimaryKeySelective(RuleDetail record);

    void updateByPrimaryKey(RuleDetail record);

    List<RuleDetail> selectByRuleId(String ruleId);
}