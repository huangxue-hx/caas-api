package com.harmonycloud.dao.ci.bean;

import java.util.ArrayList;
import java.util.List;

public class TriggerExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TriggerExample() {
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

        public Criteria andJobIdIsNull() {
            addCriterion("job_id is null");
            return (Criteria) this;
        }

        public Criteria andJobIdIsNotNull() {
            addCriterion("job_id is not null");
            return (Criteria) this;
        }

        public Criteria andJobIdEqualTo(Integer value) {
            addCriterion("job_id =", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdNotEqualTo(Integer value) {
            addCriterion("job_id <>", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdGreaterThan(Integer value) {
            addCriterion("job_id >", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("job_id >=", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdLessThan(Integer value) {
            addCriterion("job_id <", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdLessThanOrEqualTo(Integer value) {
            addCriterion("job_id <=", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdIn(List<Integer> values) {
            addCriterion("job_id in", values, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdNotIn(List<Integer> values) {
            addCriterion("job_id not in", values, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdBetween(Integer value1, Integer value2) {
            addCriterion("job_id between", value1, value2, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdNotBetween(Integer value1, Integer value2) {
            addCriterion("job_id not between", value1, value2, "jobId");
            return (Criteria) this;
        }

        public Criteria andIsValidIsNull() {
            addCriterion("is_valid is null");
            return (Criteria) this;
        }

        public Criteria andIsValidIsNotNull() {
            addCriterion("is_valid is not null");
            return (Criteria) this;
        }

        public Criteria andIsValidEqualTo(Boolean value) {
            addCriterion("is_valid =", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidNotEqualTo(Boolean value) {
            addCriterion("is_valid <>", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidGreaterThan(Boolean value) {
            addCriterion("is_valid >", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_valid >=", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidLessThan(Boolean value) {
            addCriterion("is_valid <", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidLessThanOrEqualTo(Boolean value) {
            addCriterion("is_valid <=", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidIn(List<Boolean> values) {
            addCriterion("is_valid in", values, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidNotIn(List<Boolean> values) {
            addCriterion("is_valid not in", values, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidBetween(Boolean value1, Boolean value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_valid not between", value1, value2, "isValid");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("type is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(Integer value) {
            addCriterion("type =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(Integer value) {
            addCriterion("type <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(Integer value) {
            addCriterion("type >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("type >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(Integer value) {
            addCriterion("type <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(Integer value) {
            addCriterion("type <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<Integer> values) {
            addCriterion("type in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<Integer> values) {
            addCriterion("type not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(Integer value1, Integer value2) {
            addCriterion("type between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("type not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedIsNull() {
            addCriterion("is_customised is null");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedIsNotNull() {
            addCriterion("is_customised is not null");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedEqualTo(Integer value) {
            addCriterion("is_customised =", value, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedNotEqualTo(Integer value) {
            addCriterion("is_customised <>", value, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedGreaterThan(Integer value) {
            addCriterion("is_customised >", value, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_customised >=", value, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedLessThan(Integer value) {
            addCriterion("is_customised <", value, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedLessThanOrEqualTo(Integer value) {
            addCriterion("is_customised <=", value, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedIn(List<Integer> values) {
            addCriterion("is_customised in", values, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedNotIn(List<Integer> values) {
            addCriterion("is_customised not in", values, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedBetween(Integer value1, Integer value2) {
            addCriterion("is_customised between", value1, value2, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andIsCustomisedNotBetween(Integer value1, Integer value2) {
            addCriterion("is_customised not between", value1, value2, "isCustomised");
            return (Criteria) this;
        }

        public Criteria andCronExpIsNull() {
            addCriterion("cron_exp is null");
            return (Criteria) this;
        }

        public Criteria andCronExpIsNotNull() {
            addCriterion("cron_exp is not null");
            return (Criteria) this;
        }

        public Criteria andCronExpEqualTo(String value) {
            addCriterion("cron_exp =", value, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpNotEqualTo(String value) {
            addCriterion("cron_exp <>", value, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpGreaterThan(String value) {
            addCriterion("cron_exp >", value, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpGreaterThanOrEqualTo(String value) {
            addCriterion("cron_exp >=", value, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpLessThan(String value) {
            addCriterion("cron_exp <", value, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpLessThanOrEqualTo(String value) {
            addCriterion("cron_exp <=", value, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpLike(String value) {
            addCriterion("cron_exp like", value, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpNotLike(String value) {
            addCriterion("cron_exp not like", value, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpIn(List<String> values) {
            addCriterion("cron_exp in", values, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpNotIn(List<String> values) {
            addCriterion("cron_exp not in", values, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpBetween(String value1, String value2) {
            addCriterion("cron_exp between", value1, value2, "cronExp");
            return (Criteria) this;
        }

        public Criteria andCronExpNotBetween(String value1, String value2) {
            addCriterion("cron_exp not between", value1, value2, "cronExp");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdIsNull() {
            addCriterion("trigger_job_id is null");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdIsNotNull() {
            addCriterion("trigger_job_id is not null");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdEqualTo(Integer value) {
            addCriterion("trigger_job_id =", value, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdNotEqualTo(Integer value) {
            addCriterion("trigger_job_id <>", value, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdGreaterThan(Integer value) {
            addCriterion("trigger_job_id >", value, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("trigger_job_id >=", value, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdLessThan(Integer value) {
            addCriterion("trigger_job_id <", value, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdLessThanOrEqualTo(Integer value) {
            addCriterion("trigger_job_id <=", value, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdIn(List<Integer> values) {
            addCriterion("trigger_job_id in", values, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdNotIn(List<Integer> values) {
            addCriterion("trigger_job_id not in", values, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdBetween(Integer value1, Integer value2) {
            addCriterion("trigger_job_id between", value1, value2, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andTriggerJobIdNotBetween(Integer value1, Integer value2) {
            addCriterion("trigger_job_id not between", value1, value2, "triggerJobId");
            return (Criteria) this;
        }

        public Criteria andCronExpLikeInsensitive(String value) {
            addCriterion("upper(cron_exp) like", value.toUpperCase(), "cronExp");
            return (Criteria) this;
        }

        public Criteria andTriggerImageIsNull() {
            addCriterion("trigger_image is null");
            return (Criteria) this;
        }

        public Criteria andTriggerImageIsNotNull() {
            addCriterion("trigger_image is not null");
            return (Criteria) this;
        }

        public Criteria andTriggerImageEqualTo(String value) {
            addCriterion("trigger_image =", value, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageNotEqualTo(String value) {
            addCriterion("trigger_image <>", value, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageGreaterThan(String value) {
            addCriterion("trigger_image >", value, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageGreaterThanOrEqualTo(String value) {
            addCriterion("trigger_image >=", value, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageLessThan(String value) {
            addCriterion("trigger_image <", value, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageLessThanOrEqualTo(String value) {
            addCriterion("trigger_image <=", value, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageLike(String value) {
            addCriterion("trigger_image like", value, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageNotLike(String value) {
            addCriterion("trigger_image not like", value, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageIn(List<String> values) {
            addCriterion("trigger_image in", values, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageNotIn(List<String> values) {
            addCriterion("trigger_image not in", values, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageBetween(String value1, String value2) {
            addCriterion("trigger_image between", value1, value2, "triggerImage");
            return (Criteria) this;
        }

        public Criteria andTriggerImageNotBetween(String value1, String value2) {
            addCriterion("trigger_image not between", value1, value2, "triggerImage");
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