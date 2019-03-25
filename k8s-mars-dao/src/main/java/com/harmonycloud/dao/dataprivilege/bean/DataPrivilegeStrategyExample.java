package com.harmonycloud.dao.dataprivilege.bean;

import java.util.ArrayList;
import java.util.List;

public class DataPrivilegeStrategyExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DataPrivilegeStrategyExample() {
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

        public Criteria andScopeTypeIsNull() {
            addCriterion("scope_type is null");
            return (Criteria) this;
        }

        public Criteria andScopeTypeIsNotNull() {
            addCriterion("scope_type is not null");
            return (Criteria) this;
        }

        public Criteria andScopeTypeEqualTo(Byte value) {
            addCriterion("scope_type =", value, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeNotEqualTo(Byte value) {
            addCriterion("scope_type <>", value, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeGreaterThan(Byte value) {
            addCriterion("scope_type >", value, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("scope_type >=", value, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeLessThan(Byte value) {
            addCriterion("scope_type <", value, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeLessThanOrEqualTo(Byte value) {
            addCriterion("scope_type <=", value, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeIn(List<Byte> values) {
            addCriterion("scope_type in", values, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeNotIn(List<Byte> values) {
            addCriterion("scope_type not in", values, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeBetween(Byte value1, Byte value2) {
            addCriterion("scope_type between", value1, value2, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("scope_type not between", value1, value2, "scopeType");
            return (Criteria) this;
        }

        public Criteria andScopeIdIsNull() {
            addCriterion("scope_id is null");
            return (Criteria) this;
        }

        public Criteria andScopeIdIsNotNull() {
            addCriterion("scope_id is not null");
            return (Criteria) this;
        }

        public Criteria andScopeIdEqualTo(String value) {
            addCriterion("scope_id =", value, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdNotEqualTo(String value) {
            addCriterion("scope_id <>", value, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdGreaterThan(String value) {
            addCriterion("scope_id >", value, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdGreaterThanOrEqualTo(String value) {
            addCriterion("scope_id >=", value, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdLessThan(String value) {
            addCriterion("scope_id <", value, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdLessThanOrEqualTo(String value) {
            addCriterion("scope_id <=", value, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdLike(String value) {
            addCriterion("scope_id like", value, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdNotLike(String value) {
            addCriterion("scope_id not like", value, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdIn(List<String> values) {
            addCriterion("scope_id in", values, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdNotIn(List<String> values) {
            addCriterion("scope_id not in", values, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdBetween(String value1, String value2) {
            addCriterion("scope_id between", value1, value2, "scopeId");
            return (Criteria) this;
        }

        public Criteria andScopeIdNotBetween(String value1, String value2) {
            addCriterion("scope_id not between", value1, value2, "scopeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdIsNull() {
            addCriterion("resource_type_id is null");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdIsNotNull() {
            addCriterion("resource_type_id is not null");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdEqualTo(String value) {
            addCriterion("resource_type_id =", value, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdNotEqualTo(String value) {
            addCriterion("resource_type_id <>", value, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdGreaterThan(String value) {
            addCriterion("resource_type_id >", value, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdGreaterThanOrEqualTo(String value) {
            addCriterion("resource_type_id >=", value, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdLessThan(String value) {
            addCriterion("resource_type_id <", value, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdLessThanOrEqualTo(String value) {
            addCriterion("resource_type_id <=", value, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdLike(String value) {
            addCriterion("resource_type_id like", value, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdNotLike(String value) {
            addCriterion("resource_type_id not like", value, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdIn(List<String> values) {
            addCriterion("resource_type_id in", values, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdNotIn(List<String> values) {
            addCriterion("resource_type_id not in", values, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdBetween(String value1, String value2) {
            addCriterion("resource_type_id between", value1, value2, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdNotBetween(String value1, String value2) {
            addCriterion("resource_type_id not between", value1, value2, "resourceTypeId");
            return (Criteria) this;
        }

        public Criteria andStrategyIsNull() {
            addCriterion("strategy is null");
            return (Criteria) this;
        }

        public Criteria andStrategyIsNotNull() {
            addCriterion("strategy is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyEqualTo(Byte value) {
            addCriterion("strategy =", value, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyNotEqualTo(Byte value) {
            addCriterion("strategy <>", value, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyGreaterThan(Byte value) {
            addCriterion("strategy >", value, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyGreaterThanOrEqualTo(Byte value) {
            addCriterion("strategy >=", value, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyLessThan(Byte value) {
            addCriterion("strategy <", value, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyLessThanOrEqualTo(Byte value) {
            addCriterion("strategy <=", value, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyIn(List<Byte> values) {
            addCriterion("strategy in", values, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyNotIn(List<Byte> values) {
            addCriterion("strategy not in", values, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyBetween(Byte value1, Byte value2) {
            addCriterion("strategy between", value1, value2, "strategy");
            return (Criteria) this;
        }

        public Criteria andStrategyNotBetween(Byte value1, Byte value2) {
            addCriterion("strategy not between", value1, value2, "strategy");
            return (Criteria) this;
        }

        public Criteria andScopeIdLikeInsensitive(String value) {
            addCriterion("upper(scope_id) like", value.toUpperCase(), "scopeId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIdLikeInsensitive(String value) {
            addCriterion("upper(resource_type_id) like", value.toUpperCase(), "resourceTypeId");
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