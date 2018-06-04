package com.harmonycloud.dao.cluster.bean;

import java.util.ArrayList;
import java.util.List;

public class ClusterLoadbalanceExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ClusterLoadbalanceExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andLbIdIsNull() {
            addCriterion("lb_id is null");
            return (Criteria) this;
        }

        public Criteria andLbIdIsNotNull() {
            addCriterion("lb_id is not null");
            return (Criteria) this;
        }

        public Criteria andLbIdEqualTo(Integer value) {
            addCriterion("lb_id =", value, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdNotEqualTo(Integer value) {
            addCriterion("lb_id <>", value, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdGreaterThan(Integer value) {
            addCriterion("lb_id >", value, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("lb_id >=", value, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdLessThan(Integer value) {
            addCriterion("lb_id <", value, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdLessThanOrEqualTo(Integer value) {
            addCriterion("lb_id <=", value, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdIn(List<Integer> values) {
            addCriterion("lb_id in", values, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdNotIn(List<Integer> values) {
            addCriterion("lb_id not in", values, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdBetween(Integer value1, Integer value2) {
            addCriterion("lb_id between", value1, value2, "lbId");
            return (Criteria) this;
        }

        public Criteria andLbIdNotBetween(Integer value1, Integer value2) {
            addCriterion("lb_id not between", value1, value2, "lbId");
            return (Criteria) this;
        }

        public Criteria andClusterIdIsNull() {
            addCriterion("cluster_id is null");
            return (Criteria) this;
        }

        public Criteria andClusterIdIsNotNull() {
            addCriterion("cluster_id is not null");
            return (Criteria) this;
        }

        public Criteria andClusterIdEqualTo(String value) {
            addCriterion("cluster_id =", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotEqualTo(String value) {
            addCriterion("cluster_id <>", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdIn(List<String> values) {
            addCriterion("cluster_id in", values, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotIn(List<String> values) {
            addCriterion("cluster_id not in", values, "clusterId");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameIsNull() {
            addCriterion("loadbalance_name is null");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameIsNotNull() {
            addCriterion("loadbalance_name is not null");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameEqualTo(String value) {
            addCriterion("loadbalance_name =", value, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameNotEqualTo(String value) {
            addCriterion("loadbalance_name <>", value, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameGreaterThan(String value) {
            addCriterion("loadbalance_name >", value, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameGreaterThanOrEqualTo(String value) {
            addCriterion("loadbalance_name >=", value, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameLessThan(String value) {
            addCriterion("loadbalance_name <", value, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameLessThanOrEqualTo(String value) {
            addCriterion("loadbalance_name <=", value, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameLike(String value) {
            addCriterion("loadbalance_name like", value, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameNotLike(String value) {
            addCriterion("loadbalance_name not like", value, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameIn(List<String> values) {
            addCriterion("loadbalance_name in", values, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameNotIn(List<String> values) {
            addCriterion("loadbalance_name not in", values, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameBetween(String value1, String value2) {
            addCriterion("loadbalance_name between", value1, value2, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceNameNotBetween(String value1, String value2) {
            addCriterion("loadbalance_name not between", value1, value2, "loadbalanceName");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpIsNull() {
            addCriterion("loadbalance_ip is null");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpIsNotNull() {
            addCriterion("loadbalance_ip is not null");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpEqualTo(String value) {
            addCriterion("loadbalance_ip =", value, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpNotEqualTo(String value) {
            addCriterion("loadbalance_ip <>", value, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpGreaterThan(String value) {
            addCriterion("loadbalance_ip >", value, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpGreaterThanOrEqualTo(String value) {
            addCriterion("loadbalance_ip >=", value, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpLessThan(String value) {
            addCriterion("loadbalance_ip <", value, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpLessThanOrEqualTo(String value) {
            addCriterion("loadbalance_ip <=", value, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpLike(String value) {
            addCriterion("loadbalance_ip like", value, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpNotLike(String value) {
            addCriterion("loadbalance_ip not like", value, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpIn(List<String> values) {
            addCriterion("loadbalance_ip in", values, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpNotIn(List<String> values) {
            addCriterion("loadbalance_ip not in", values, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpBetween(String value1, String value2) {
            addCriterion("loadbalance_ip between", value1, value2, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalanceIpNotBetween(String value1, String value2) {
            addCriterion("loadbalance_ip not between", value1, value2, "loadbalanceIp");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortIsNull() {
            addCriterion("loadbalance_port is null");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortIsNotNull() {
            addCriterion("loadbalance_port is not null");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortEqualTo(String value) {
            addCriterion("loadbalance_port =", value, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortNotEqualTo(String value) {
            addCriterion("loadbalance_port <>", value, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortGreaterThan(String value) {
            addCriterion("loadbalance_port >", value, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortGreaterThanOrEqualTo(String value) {
            addCriterion("loadbalance_port >=", value, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortLessThan(String value) {
            addCriterion("loadbalance_port <", value, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortLessThanOrEqualTo(String value) {
            addCriterion("loadbalance_port <=", value, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortLike(String value) {
            addCriterion("loadbalance_port like", value, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortNotLike(String value) {
            addCriterion("loadbalance_port not like", value, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortIn(List<String> values) {
            addCriterion("loadbalance_port in", values, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortNotIn(List<String> values) {
            addCriterion("loadbalance_port not in", values, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortBetween(String value1, String value2) {
            addCriterion("loadbalance_port between", value1, value2, "loadbalancePort");
            return (Criteria) this;
        }

        public Criteria andLoadbalancePortNotBetween(String value1, String value2) {
            addCriterion("loadbalance_port not between", value1, value2, "loadbalancePort");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }

        public Criteria andLoadbalanceNameLikeInsensitive(String value) {
            addCriterion("upper(loadbalance_name) like", value.toUpperCase(), "loadbalanceName");
            return this;
        }

        public Criteria andLoadbalanceIpLikeInsensitive(String value) {
            addCriterion("upper(loadbalance_ip) like", value.toUpperCase(), "loadbalanceIp");
            return this;
        }

        public Criteria andLoadbalancePortLikeInsensitive(String value) {
            addCriterion("upper(loadbalance_port) like", value.toUpperCase(), "loadbalancePort");
            return this;
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}