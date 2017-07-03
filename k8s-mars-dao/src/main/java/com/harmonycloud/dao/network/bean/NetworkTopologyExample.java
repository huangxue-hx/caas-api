package com.harmonycloud.dao.network.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NetworkTopologyExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public NetworkTopologyExample() {
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

        public Criteria andNetIdIsNull() {
            addCriterion("net_id is null");
            return (Criteria) this;
        }

        public Criteria andNetIdIsNotNull() {
            addCriterion("net_id is not null");
            return (Criteria) this;
        }

        public Criteria andNetIdEqualTo(String value) {
            addCriterion("net_id =", value, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdNotEqualTo(String value) {
            addCriterion("net_id <>", value, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdGreaterThan(String value) {
            addCriterion("net_id >", value, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdGreaterThanOrEqualTo(String value) {
            addCriterion("net_id >=", value, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdLessThan(String value) {
            addCriterion("net_id <", value, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdLessThanOrEqualTo(String value) {
            addCriterion("net_id <=", value, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdLike(String value) {
            addCriterion("net_id like", value, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdNotLike(String value) {
            addCriterion("net_id not like", value, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdIn(List<String> values) {
            addCriterion("net_id in", values, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdNotIn(List<String> values) {
            addCriterion("net_id not in", values, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdBetween(String value1, String value2) {
            addCriterion("net_id between", value1, value2, "netId");
            return (Criteria) this;
        }

        public Criteria andNetIdNotBetween(String value1, String value2) {
            addCriterion("net_id not between", value1, value2, "netId");
            return (Criteria) this;
        }

        public Criteria andNetNameIsNull() {
            addCriterion("net_name is null");
            return (Criteria) this;
        }

        public Criteria andNetNameIsNotNull() {
            addCriterion("net_name is not null");
            return (Criteria) this;
        }

        public Criteria andNetNameEqualTo(String value) {
            addCriterion("net_name =", value, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameNotEqualTo(String value) {
            addCriterion("net_name <>", value, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameGreaterThan(String value) {
            addCriterion("net_name >", value, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameGreaterThanOrEqualTo(String value) {
            addCriterion("net_name >=", value, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameLessThan(String value) {
            addCriterion("net_name <", value, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameLessThanOrEqualTo(String value) {
            addCriterion("net_name <=", value, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameLike(String value) {
            addCriterion("net_name like", value, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameNotLike(String value) {
            addCriterion("net_name not like", value, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameIn(List<String> values) {
            addCriterion("net_name in", values, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameNotIn(List<String> values) {
            addCriterion("net_name not in", values, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameBetween(String value1, String value2) {
            addCriterion("net_name between", value1, value2, "netName");
            return (Criteria) this;
        }

        public Criteria andNetNameNotBetween(String value1, String value2) {
            addCriterion("net_name not between", value1, value2, "netName");
            return (Criteria) this;
        }

        public Criteria andTopologyIsNull() {
            addCriterion("topology is null");
            return (Criteria) this;
        }

        public Criteria andTopologyIsNotNull() {
            addCriterion("topology is not null");
            return (Criteria) this;
        }

        public Criteria andTopologyEqualTo(String value) {
            addCriterion("topology =", value, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyNotEqualTo(String value) {
            addCriterion("topology <>", value, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyGreaterThan(String value) {
            addCriterion("topology >", value, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyGreaterThanOrEqualTo(String value) {
            addCriterion("topology >=", value, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyLessThan(String value) {
            addCriterion("topology <", value, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyLessThanOrEqualTo(String value) {
            addCriterion("topology <=", value, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyLike(String value) {
            addCriterion("topology like", value, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyNotLike(String value) {
            addCriterion("topology not like", value, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyIn(List<String> values) {
            addCriterion("topology in", values, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyNotIn(List<String> values) {
            addCriterion("topology not in", values, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyBetween(String value1, String value2) {
            addCriterion("topology between", value1, value2, "topology");
            return (Criteria) this;
        }

        public Criteria andTopologyNotBetween(String value1, String value2) {
            addCriterion("topology not between", value1, value2, "topology");
            return (Criteria) this;
        }

        public Criteria andCreatetimeIsNull() {
            addCriterion("createtime is null");
            return (Criteria) this;
        }

        public Criteria andCreatetimeIsNotNull() {
            addCriterion("createtime is not null");
            return (Criteria) this;
        }

        public Criteria andCreatetimeEqualTo(Date value) {
            addCriterion("createtime =", value, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeNotEqualTo(Date value) {
            addCriterion("createtime <>", value, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeGreaterThan(Date value) {
            addCriterion("createtime >", value, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeGreaterThanOrEqualTo(Date value) {
            addCriterion("createtime >=", value, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeLessThan(Date value) {
            addCriterion("createtime <", value, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeLessThanOrEqualTo(Date value) {
            addCriterion("createtime <=", value, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeIn(List<Date> values) {
            addCriterion("createtime in", values, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeNotIn(List<Date> values) {
            addCriterion("createtime not in", values, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeBetween(Date value1, Date value2) {
            addCriterion("createtime between", value1, value2, "createtime");
            return (Criteria) this;
        }

        public Criteria andCreatetimeNotBetween(Date value1, Date value2) {
            addCriterion("createtime not between", value1, value2, "createtime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeIsNull() {
            addCriterion("updatetime is null");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeIsNotNull() {
            addCriterion("updatetime is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeEqualTo(Date value) {
            addCriterion("updatetime =", value, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeNotEqualTo(Date value) {
            addCriterion("updatetime <>", value, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeGreaterThan(Date value) {
            addCriterion("updatetime >", value, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeGreaterThanOrEqualTo(Date value) {
            addCriterion("updatetime >=", value, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeLessThan(Date value) {
            addCriterion("updatetime <", value, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeLessThanOrEqualTo(Date value) {
            addCriterion("updatetime <=", value, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeIn(List<Date> values) {
            addCriterion("updatetime in", values, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeNotIn(List<Date> values) {
            addCriterion("updatetime not in", values, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeBetween(Date value1, Date value2) {
            addCriterion("updatetime between", value1, value2, "updatetime");
            return (Criteria) this;
        }

        public Criteria andUpdatetimeNotBetween(Date value1, Date value2) {
            addCriterion("updatetime not between", value1, value2, "updatetime");
            return (Criteria) this;
        }

        public Criteria andDestinationidIsNull() {
            addCriterion("destinationid is null");
            return (Criteria) this;
        }

        public Criteria andDestinationidIsNotNull() {
            addCriterion("destinationid is not null");
            return (Criteria) this;
        }

        public Criteria andDestinationidEqualTo(String value) {
            addCriterion("destinationid =", value, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidNotEqualTo(String value) {
            addCriterion("destinationid <>", value, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidGreaterThan(String value) {
            addCriterion("destinationid >", value, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidGreaterThanOrEqualTo(String value) {
            addCriterion("destinationid >=", value, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidLessThan(String value) {
            addCriterion("destinationid <", value, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidLessThanOrEqualTo(String value) {
            addCriterion("destinationid <=", value, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidLike(String value) {
            addCriterion("destinationid like", value, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidNotLike(String value) {
            addCriterion("destinationid not like", value, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidIn(List<String> values) {
            addCriterion("destinationid in", values, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidNotIn(List<String> values) {
            addCriterion("destinationid not in", values, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidBetween(String value1, String value2) {
            addCriterion("destinationid between", value1, value2, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationidNotBetween(String value1, String value2) {
            addCriterion("destinationid not between", value1, value2, "destinationid");
            return (Criteria) this;
        }

        public Criteria andDestinationnameIsNull() {
            addCriterion("destinationname is null");
            return (Criteria) this;
        }

        public Criteria andDestinationnameIsNotNull() {
            addCriterion("destinationname is not null");
            return (Criteria) this;
        }

        public Criteria andDestinationnameEqualTo(String value) {
            addCriterion("destinationname =", value, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameNotEqualTo(String value) {
            addCriterion("destinationname <>", value, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameGreaterThan(String value) {
            addCriterion("destinationname >", value, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameGreaterThanOrEqualTo(String value) {
            addCriterion("destinationname >=", value, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameLessThan(String value) {
            addCriterion("destinationname <", value, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameLessThanOrEqualTo(String value) {
            addCriterion("destinationname <=", value, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameLike(String value) {
            addCriterion("destinationname like", value, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameNotLike(String value) {
            addCriterion("destinationname not like", value, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameIn(List<String> values) {
            addCriterion("destinationname in", values, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameNotIn(List<String> values) {
            addCriterion("destinationname not in", values, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameBetween(String value1, String value2) {
            addCriterion("destinationname between", value1, value2, "destinationname");
            return (Criteria) this;
        }

        public Criteria andDestinationnameNotBetween(String value1, String value2) {
            addCriterion("destinationname not between", value1, value2, "destinationname");
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