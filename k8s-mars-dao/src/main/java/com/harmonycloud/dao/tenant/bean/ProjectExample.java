package com.harmonycloud.dao.tenant.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    private Integer limit;

    private Integer offset;

    public ProjectExample() {
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

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getOffset() {
        return offset;
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

        public Criteria andTenantIdIsNull() {
            addCriterion("tenant_id is null");
            return (Criteria) this;
        }

        public Criteria andTenantIdIsNotNull() {
            addCriterion("tenant_id is not null");
            return (Criteria) this;
        }

        public Criteria andTenantIdEqualTo(String value) {
            addCriterion("tenant_id =", value, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdNotEqualTo(String value) {
            addCriterion("tenant_id <>", value, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdGreaterThan(String value) {
            addCriterion("tenant_id >", value, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdGreaterThanOrEqualTo(String value) {
            addCriterion("tenant_id >=", value, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdLessThan(String value) {
            addCriterion("tenant_id <", value, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdLessThanOrEqualTo(String value) {
            addCriterion("tenant_id <=", value, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdLike(String value) {
            addCriterion("tenant_id like", value, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdNotLike(String value) {
            addCriterion("tenant_id not like", value, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdIn(List<String> values) {
            addCriterion("tenant_id in", values, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdNotIn(List<String> values) {
            addCriterion("tenant_id not in", values, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdBetween(String value1, String value2) {
            addCriterion("tenant_id between", value1, value2, "tenantId");
            return (Criteria) this;
        }

        public Criteria andTenantIdNotBetween(String value1, String value2) {
            addCriterion("tenant_id not between", value1, value2, "tenantId");
            return (Criteria) this;
        }

        public Criteria andProjectIdIsNull() {
            addCriterion("project_id is null");
            return (Criteria) this;
        }

        public Criteria andProjectIdIsNotNull() {
            addCriterion("project_id is not null");
            return (Criteria) this;
        }

        public Criteria andProjectIdEqualTo(String value) {
            addCriterion("project_id =", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotEqualTo(String value) {
            addCriterion("project_id <>", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdGreaterThan(String value) {
            addCriterion("project_id >", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdGreaterThanOrEqualTo(String value) {
            addCriterion("project_id >=", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLessThan(String value) {
            addCriterion("project_id <", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLessThanOrEqualTo(String value) {
            addCriterion("project_id <=", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLike(String value) {
            addCriterion("project_id like", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotLike(String value) {
            addCriterion("project_id not like", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdIn(List<String> values) {
            addCriterion("project_id in", values, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotIn(List<String> values) {
            addCriterion("project_id not in", values, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdBetween(String value1, String value2) {
            addCriterion("project_id between", value1, value2, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotBetween(String value1, String value2) {
            addCriterion("project_id not between", value1, value2, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeIsNull() {
            addCriterion("project_system_code is null");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeIsNotNull() {
            addCriterion("project_system_code is not null");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeEqualTo(String value) {
            addCriterion("project_system_code =", value, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeNotEqualTo(String value) {
            addCriterion("project_system_code <>", value, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeGreaterThan(String value) {
            addCriterion("project_system_code >", value, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeGreaterThanOrEqualTo(String value) {
            addCriterion("project_system_code >=", value, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeLessThan(String value) {
            addCriterion("project_system_code <", value, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeLessThanOrEqualTo(String value) {
            addCriterion("project_system_code <=", value, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeLike(String value) {
            addCriterion("project_system_code like", value, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeNotLike(String value) {
            addCriterion("project_system_code not like", value, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeIn(List<String> values) {
            addCriterion("project_system_code in", values, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeNotIn(List<String> values) {
            addCriterion("project_system_code not in", values, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeBetween(String value1, String value2) {
            addCriterion("project_system_code between", value1, value2, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeNotBetween(String value1, String value2) {
            addCriterion("project_system_code not between", value1, value2, "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectNameIsNull() {
            addCriterion("project_name is null");
            return (Criteria) this;
        }

        public Criteria andProjectNameIsNotNull() {
            addCriterion("project_name is not null");
            return (Criteria) this;
        }

        public Criteria andProjectNameEqualTo(String value) {
            addCriterion("project_name =", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameNotEqualTo(String value) {
            addCriterion("project_name <>", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameGreaterThan(String value) {
            addCriterion("project_name >", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameGreaterThanOrEqualTo(String value) {
            addCriterion("project_name >=", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameLessThan(String value) {
            addCriterion("project_name <", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameLessThanOrEqualTo(String value) {
            addCriterion("project_name <=", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameLike(String value) {
            addCriterion("project_name like", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameNotLike(String value) {
            addCriterion("project_name not like", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameIn(List<String> values) {
            addCriterion("project_name in", values, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameNotIn(List<String> values) {
            addCriterion("project_name not in", values, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameBetween(String value1, String value2) {
            addCriterion("project_name between", value1, value2, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameNotBetween(String value1, String value2) {
            addCriterion("project_name not between", value1, value2, "projectName");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andAnnotationIsNull() {
            addCriterion("annotation is null");
            return (Criteria) this;
        }

        public Criteria andAnnotationIsNotNull() {
            addCriterion("annotation is not null");
            return (Criteria) this;
        }

        public Criteria andAnnotationEqualTo(String value) {
            addCriterion("annotation =", value, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationNotEqualTo(String value) {
            addCriterion("annotation <>", value, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationGreaterThan(String value) {
            addCriterion("annotation >", value, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationGreaterThanOrEqualTo(String value) {
            addCriterion("annotation >=", value, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationLessThan(String value) {
            addCriterion("annotation <", value, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationLessThanOrEqualTo(String value) {
            addCriterion("annotation <=", value, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationLike(String value) {
            addCriterion("annotation like", value, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationNotLike(String value) {
            addCriterion("annotation not like", value, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationIn(List<String> values) {
            addCriterion("annotation in", values, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationNotIn(List<String> values) {
            addCriterion("annotation not in", values, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationBetween(String value1, String value2) {
            addCriterion("annotation between", value1, value2, "annotation");
            return (Criteria) this;
        }

        public Criteria andAnnotationNotBetween(String value1, String value2) {
            addCriterion("annotation not between", value1, value2, "annotation");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesIsNull() {
            addCriterion("pm_usernames is null");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesIsNotNull() {
            addCriterion("pm_usernames is not null");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesEqualTo(String value) {
            addCriterion("pm_usernames =", value, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesNotEqualTo(String value) {
            addCriterion("pm_usernames <>", value, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesGreaterThan(String value) {
            addCriterion("pm_usernames >", value, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesGreaterThanOrEqualTo(String value) {
            addCriterion("pm_usernames >=", value, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesLessThan(String value) {
            addCriterion("pm_usernames <", value, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesLessThanOrEqualTo(String value) {
            addCriterion("pm_usernames <=", value, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesLike(String value) {
            addCriterion("pm_usernames like", value, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesNotLike(String value) {
            addCriterion("pm_usernames not like", value, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesIn(List<String> values) {
            addCriterion("pm_usernames in", values, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesNotIn(List<String> values) {
            addCriterion("pm_usernames not in", values, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesBetween(String value1, String value2) {
            addCriterion("pm_usernames between", value1, value2, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesNotBetween(String value1, String value2) {
            addCriterion("pm_usernames not between", value1, value2, "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountIsNull() {
            addCriterion("update_user_account is null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountIsNotNull() {
            addCriterion("update_user_account is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountEqualTo(String value) {
            addCriterion("update_user_account =", value, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountNotEqualTo(String value) {
            addCriterion("update_user_account <>", value, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountGreaterThan(String value) {
            addCriterion("update_user_account >", value, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountGreaterThanOrEqualTo(String value) {
            addCriterion("update_user_account >=", value, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountLessThan(String value) {
            addCriterion("update_user_account <", value, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountLessThanOrEqualTo(String value) {
            addCriterion("update_user_account <=", value, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountLike(String value) {
            addCriterion("update_user_account like", value, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountNotLike(String value) {
            addCriterion("update_user_account not like", value, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountIn(List<String> values) {
            addCriterion("update_user_account in", values, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountNotIn(List<String> values) {
            addCriterion("update_user_account not in", values, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountBetween(String value1, String value2) {
            addCriterion("update_user_account between", value1, value2, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountNotBetween(String value1, String value2) {
            addCriterion("update_user_account not between", value1, value2, "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdIsNull() {
            addCriterion("update_user_id is null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdIsNotNull() {
            addCriterion("update_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdEqualTo(String value) {
            addCriterion("update_user_id =", value, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdNotEqualTo(String value) {
            addCriterion("update_user_id <>", value, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdGreaterThan(String value) {
            addCriterion("update_user_id >", value, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("update_user_id >=", value, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdLessThan(String value) {
            addCriterion("update_user_id <", value, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdLessThanOrEqualTo(String value) {
            addCriterion("update_user_id <=", value, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdLike(String value) {
            addCriterion("update_user_id like", value, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdNotLike(String value) {
            addCriterion("update_user_id not like", value, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdIn(List<String> values) {
            addCriterion("update_user_id in", values, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdNotIn(List<String> values) {
            addCriterion("update_user_id not in", values, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdBetween(String value1, String value2) {
            addCriterion("update_user_id between", value1, value2, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdNotBetween(String value1, String value2) {
            addCriterion("update_user_id not between", value1, value2, "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameIsNull() {
            addCriterion("update_user_name is null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameIsNotNull() {
            addCriterion("update_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameEqualTo(String value) {
            addCriterion("update_user_name =", value, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameNotEqualTo(String value) {
            addCriterion("update_user_name <>", value, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameGreaterThan(String value) {
            addCriterion("update_user_name >", value, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("update_user_name >=", value, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameLessThan(String value) {
            addCriterion("update_user_name <", value, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameLessThanOrEqualTo(String value) {
            addCriterion("update_user_name <=", value, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameLike(String value) {
            addCriterion("update_user_name like", value, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameNotLike(String value) {
            addCriterion("update_user_name not like", value, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameIn(List<String> values) {
            addCriterion("update_user_name in", values, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameNotIn(List<String> values) {
            addCriterion("update_user_name not in", values, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameBetween(String value1, String value2) {
            addCriterion("update_user_name between", value1, value2, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameNotBetween(String value1, String value2) {
            addCriterion("update_user_name not between", value1, value2, "updateUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountIsNull() {
            addCriterion("create_user_account is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountIsNotNull() {
            addCriterion("create_user_account is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountEqualTo(String value) {
            addCriterion("create_user_account =", value, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountNotEqualTo(String value) {
            addCriterion("create_user_account <>", value, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountGreaterThan(String value) {
            addCriterion("create_user_account >", value, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountGreaterThanOrEqualTo(String value) {
            addCriterion("create_user_account >=", value, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountLessThan(String value) {
            addCriterion("create_user_account <", value, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountLessThanOrEqualTo(String value) {
            addCriterion("create_user_account <=", value, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountLike(String value) {
            addCriterion("create_user_account like", value, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountNotLike(String value) {
            addCriterion("create_user_account not like", value, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountIn(List<String> values) {
            addCriterion("create_user_account in", values, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountNotIn(List<String> values) {
            addCriterion("create_user_account not in", values, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountBetween(String value1, String value2) {
            addCriterion("create_user_account between", value1, value2, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountNotBetween(String value1, String value2) {
            addCriterion("create_user_account not between", value1, value2, "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdIsNull() {
            addCriterion("create_user_id is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdIsNotNull() {
            addCriterion("create_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdEqualTo(String value) {
            addCriterion("create_user_id =", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdNotEqualTo(String value) {
            addCriterion("create_user_id <>", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdGreaterThan(String value) {
            addCriterion("create_user_id >", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("create_user_id >=", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdLessThan(String value) {
            addCriterion("create_user_id <", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdLessThanOrEqualTo(String value) {
            addCriterion("create_user_id <=", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdLike(String value) {
            addCriterion("create_user_id like", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdNotLike(String value) {
            addCriterion("create_user_id not like", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdIn(List<String> values) {
            addCriterion("create_user_id in", values, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdNotIn(List<String> values) {
            addCriterion("create_user_id not in", values, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdBetween(String value1, String value2) {
            addCriterion("create_user_id between", value1, value2, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdNotBetween(String value1, String value2) {
            addCriterion("create_user_id not between", value1, value2, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameIsNull() {
            addCriterion("create_user_name is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameIsNotNull() {
            addCriterion("create_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameEqualTo(String value) {
            addCriterion("create_user_name =", value, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameNotEqualTo(String value) {
            addCriterion("create_user_name <>", value, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameGreaterThan(String value) {
            addCriterion("create_user_name >", value, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("create_user_name >=", value, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameLessThan(String value) {
            addCriterion("create_user_name <", value, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameLessThanOrEqualTo(String value) {
            addCriterion("create_user_name <=", value, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameLike(String value) {
            addCriterion("create_user_name like", value, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameNotLike(String value) {
            addCriterion("create_user_name not like", value, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameIn(List<String> values) {
            addCriterion("create_user_name in", values, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameNotIn(List<String> values) {
            addCriterion("create_user_name not in", values, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameBetween(String value1, String value2) {
            addCriterion("create_user_name between", value1, value2, "createUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameNotBetween(String value1, String value2) {
            addCriterion("create_user_name not between", value1, value2, "createUserName");
            return (Criteria) this;
        }

        public Criteria andAliasNameIsNull() {
            addCriterion("alias_name is null");
            return (Criteria) this;
        }

        public Criteria andAliasNameIsNotNull() {
            addCriterion("alias_name is not null");
            return (Criteria) this;
        }

        public Criteria andAliasNameEqualTo(String value) {
            addCriterion("alias_name =", value, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameNotEqualTo(String value) {
            addCriterion("alias_name <>", value, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameGreaterThan(String value) {
            addCriterion("alias_name >", value, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameGreaterThanOrEqualTo(String value) {
            addCriterion("alias_name >=", value, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameLessThan(String value) {
            addCriterion("alias_name <", value, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameLessThanOrEqualTo(String value) {
            addCriterion("alias_name <=", value, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameLike(String value) {
            addCriterion("alias_name like", value, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameNotLike(String value) {
            addCriterion("alias_name not like", value, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameIn(List<String> values) {
            addCriterion("alias_name in", values, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameNotIn(List<String> values) {
            addCriterion("alias_name not in", values, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameBetween(String value1, String value2) {
            addCriterion("alias_name between", value1, value2, "aliasName");
            return (Criteria) this;
        }

        public Criteria andAliasNameNotBetween(String value1, String value2) {
            addCriterion("alias_name not between", value1, value2, "aliasName");
            return (Criteria) this;
        }

        public Criteria andReserve1IsNull() {
            addCriterion("reserve1 is null");
            return (Criteria) this;
        }

        public Criteria andReserve1IsNotNull() {
            addCriterion("reserve1 is not null");
            return (Criteria) this;
        }

        public Criteria andReserve1EqualTo(String value) {
            addCriterion("reserve1 =", value, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1NotEqualTo(String value) {
            addCriterion("reserve1 <>", value, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1GreaterThan(String value) {
            addCriterion("reserve1 >", value, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1GreaterThanOrEqualTo(String value) {
            addCriterion("reserve1 >=", value, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1LessThan(String value) {
            addCriterion("reserve1 <", value, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1LessThanOrEqualTo(String value) {
            addCriterion("reserve1 <=", value, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1Like(String value) {
            addCriterion("reserve1 like", value, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1NotLike(String value) {
            addCriterion("reserve1 not like", value, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1In(List<String> values) {
            addCriterion("reserve1 in", values, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1NotIn(List<String> values) {
            addCriterion("reserve1 not in", values, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1Between(String value1, String value2) {
            addCriterion("reserve1 between", value1, value2, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve1NotBetween(String value1, String value2) {
            addCriterion("reserve1 not between", value1, value2, "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve2IsNull() {
            addCriterion("reserve2 is null");
            return (Criteria) this;
        }

        public Criteria andReserve2IsNotNull() {
            addCriterion("reserve2 is not null");
            return (Criteria) this;
        }

        public Criteria andReserve2EqualTo(String value) {
            addCriterion("reserve2 =", value, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2NotEqualTo(String value) {
            addCriterion("reserve2 <>", value, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2GreaterThan(String value) {
            addCriterion("reserve2 >", value, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2GreaterThanOrEqualTo(String value) {
            addCriterion("reserve2 >=", value, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2LessThan(String value) {
            addCriterion("reserve2 <", value, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2LessThanOrEqualTo(String value) {
            addCriterion("reserve2 <=", value, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2Like(String value) {
            addCriterion("reserve2 like", value, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2NotLike(String value) {
            addCriterion("reserve2 not like", value, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2In(List<String> values) {
            addCriterion("reserve2 in", values, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2NotIn(List<String> values) {
            addCriterion("reserve2 not in", values, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2Between(String value1, String value2) {
            addCriterion("reserve2 between", value1, value2, "reserve2");
            return (Criteria) this;
        }

        public Criteria andReserve2NotBetween(String value1, String value2) {
            addCriterion("reserve2 not between", value1, value2, "reserve2");
            return (Criteria) this;
        }

        public Criteria andTenantIdLikeInsensitive(String value) {
            addCriterion("upper(tenant_id) like", value.toUpperCase(), "tenantId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLikeInsensitive(String value) {
            addCriterion("upper(project_id) like", value.toUpperCase(), "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectSystemCodeLikeInsensitive(String value) {
            addCriterion("upper(project_system_code) like", value.toUpperCase(), "projectSystemCode");
            return (Criteria) this;
        }

        public Criteria andProjectNameLikeInsensitive(String value) {
            addCriterion("upper(project_name) like", value.toUpperCase(), "projectName");
            return (Criteria) this;
        }

        public Criteria andAnnotationLikeInsensitive(String value) {
            addCriterion("upper(annotation) like", value.toUpperCase(), "annotation");
            return (Criteria) this;
        }

        public Criteria andPmUsernamesLikeInsensitive(String value) {
            addCriterion("upper(pm_usernames) like", value.toUpperCase(), "pmUsernames");
            return (Criteria) this;
        }

        public Criteria andUpdateUserAccountLikeInsensitive(String value) {
            addCriterion("upper(update_user_account) like", value.toUpperCase(), "updateUserAccount");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIdLikeInsensitive(String value) {
            addCriterion("upper(update_user_id) like", value.toUpperCase(), "updateUserId");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNameLikeInsensitive(String value) {
            addCriterion("upper(update_user_name) like", value.toUpperCase(), "updateUserName");
            return (Criteria) this;
        }

        public Criteria andCreateUserAccountLikeInsensitive(String value) {
            addCriterion("upper(create_user_account) like", value.toUpperCase(), "createUserAccount");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdLikeInsensitive(String value) {
            addCriterion("upper(create_user_id) like", value.toUpperCase(), "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserNameLikeInsensitive(String value) {
            addCriterion("upper(create_user_name) like", value.toUpperCase(), "createUserName");
            return (Criteria) this;
        }

        public Criteria andAliasNameLikeInsensitive(String value) {
            addCriterion("upper(alias_name) like", value.toUpperCase(), "aliasName");
            return (Criteria) this;
        }

        public Criteria andReserve1LikeInsensitive(String value) {
            addCriterion("upper(reserve1) like", value.toUpperCase(), "reserve1");
            return (Criteria) this;
        }

        public Criteria andReserve2LikeInsensitive(String value) {
            addCriterion("upper(reserve2) like", value.toUpperCase(), "reserve2");
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