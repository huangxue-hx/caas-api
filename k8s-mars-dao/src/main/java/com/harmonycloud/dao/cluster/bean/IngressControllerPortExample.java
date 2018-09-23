package com.harmonycloud.dao.cluster.bean;

import java.util.ArrayList;
import java.util.List;

public class IngressControllerPortExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public IngressControllerPortExample() {
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

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("name is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("name is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("name =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("name <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("name >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("name >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("name <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("name <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("name like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("name not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("name in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("name not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("name between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("name not between", value1, value2, "name");
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

        public Criteria andClusterIdGreaterThan(String value) {
            addCriterion("cluster_id >", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdGreaterThanOrEqualTo(String value) {
            addCriterion("cluster_id >=", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLessThan(String value) {
            addCriterion("cluster_id <", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLessThanOrEqualTo(String value) {
            addCriterion("cluster_id <=", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLike(String value) {
            addCriterion("cluster_id like", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotLike(String value) {
            addCriterion("cluster_id not like", value, "clusterId");
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

        public Criteria andClusterIdBetween(String value1, String value2) {
            addCriterion("cluster_id between", value1, value2, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotBetween(String value1, String value2) {
            addCriterion("cluster_id not between", value1, value2, "clusterId");
            return (Criteria) this;
        }

        public Criteria andHttpPortIsNull() {
            addCriterion("http_port is null");
            return (Criteria) this;
        }

        public Criteria andHttpPortIsNotNull() {
            addCriterion("http_port is not null");
            return (Criteria) this;
        }

        public Criteria andHttpPortEqualTo(Integer value) {
            addCriterion("http_port =", value, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortNotEqualTo(Integer value) {
            addCriterion("http_port <>", value, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortGreaterThan(Integer value) {
            addCriterion("http_port >", value, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("http_port >=", value, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortLessThan(Integer value) {
            addCriterion("http_port <", value, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortLessThanOrEqualTo(Integer value) {
            addCriterion("http_port <=", value, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortIn(List<Integer> values) {
            addCriterion("http_port in", values, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortNotIn(List<Integer> values) {
            addCriterion("http_port not in", values, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortBetween(Integer value1, Integer value2) {
            addCriterion("http_port between", value1, value2, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpPortNotBetween(Integer value1, Integer value2) {
            addCriterion("http_port not between", value1, value2, "httpPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortIsNull() {
            addCriterion("https_port is null");
            return (Criteria) this;
        }

        public Criteria andHttpsPortIsNotNull() {
            addCriterion("https_port is not null");
            return (Criteria) this;
        }

        public Criteria andHttpsPortEqualTo(Integer value) {
            addCriterion("https_port =", value, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortNotEqualTo(Integer value) {
            addCriterion("https_port <>", value, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortGreaterThan(Integer value) {
            addCriterion("https_port >", value, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("https_port >=", value, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortLessThan(Integer value) {
            addCriterion("https_port <", value, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortLessThanOrEqualTo(Integer value) {
            addCriterion("https_port <=", value, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortIn(List<Integer> values) {
            addCriterion("https_port in", values, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortNotIn(List<Integer> values) {
            addCriterion("https_port not in", values, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortBetween(Integer value1, Integer value2) {
            addCriterion("https_port between", value1, value2, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHttpsPortNotBetween(Integer value1, Integer value2) {
            addCriterion("https_port not between", value1, value2, "httpsPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortIsNull() {
            addCriterion("health_port is null");
            return (Criteria) this;
        }

        public Criteria andHealthPortIsNotNull() {
            addCriterion("health_port is not null");
            return (Criteria) this;
        }

        public Criteria andHealthPortEqualTo(Integer value) {
            addCriterion("health_port =", value, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortNotEqualTo(Integer value) {
            addCriterion("health_port <>", value, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortGreaterThan(Integer value) {
            addCriterion("health_port >", value, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("health_port >=", value, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortLessThan(Integer value) {
            addCriterion("health_port <", value, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortLessThanOrEqualTo(Integer value) {
            addCriterion("health_port <=", value, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortIn(List<Integer> values) {
            addCriterion("health_port in", values, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortNotIn(List<Integer> values) {
            addCriterion("health_port not in", values, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortBetween(Integer value1, Integer value2) {
            addCriterion("health_port between", value1, value2, "healthPort");
            return (Criteria) this;
        }

        public Criteria andHealthPortNotBetween(Integer value1, Integer value2) {
            addCriterion("health_port not between", value1, value2, "healthPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortIsNull() {
            addCriterion("status_port is null");
            return (Criteria) this;
        }

        public Criteria andStatusPortIsNotNull() {
            addCriterion("status_port is not null");
            return (Criteria) this;
        }

        public Criteria andStatusPortEqualTo(Integer value) {
            addCriterion("status_port =", value, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortNotEqualTo(Integer value) {
            addCriterion("status_port <>", value, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortGreaterThan(Integer value) {
            addCriterion("status_port >", value, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("status_port >=", value, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortLessThan(Integer value) {
            addCriterion("status_port <", value, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortLessThanOrEqualTo(Integer value) {
            addCriterion("status_port <=", value, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortIn(List<Integer> values) {
            addCriterion("status_port in", values, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortNotIn(List<Integer> values) {
            addCriterion("status_port not in", values, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortBetween(Integer value1, Integer value2) {
            addCriterion("status_port between", value1, value2, "statusPort");
            return (Criteria) this;
        }

        public Criteria andStatusPortNotBetween(Integer value1, Integer value2) {
            addCriterion("status_port not between", value1, value2, "statusPort");
            return (Criteria) this;
        }

        public Criteria andNameLikeInsensitive(String value) {
            addCriterion("upper(name) like", value.toUpperCase(), "name");
            return (Criteria) this;
        }

        public Criteria andClusterIdLikeInsensitive(String value) {
            addCriterion("upper(cluster_id) like", value.toUpperCase(), "clusterId");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
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