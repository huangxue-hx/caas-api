package com.harmonycloud.dao.tenant.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TenantBindingExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TenantBindingExample() {
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

        public Criteria andTenantNameIsNull() {
            addCriterion("tenant_name is null");
            return (Criteria) this;
        }

        public Criteria andTenantNameIsNotNull() {
            addCriterion("tenant_name is not null");
            return (Criteria) this;
        }

        public Criteria andTenantNameEqualTo(String value) {
            addCriterion("tenant_name =", value, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameNotEqualTo(String value) {
            addCriterion("tenant_name <>", value, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameGreaterThan(String value) {
            addCriterion("tenant_name >", value, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameGreaterThanOrEqualTo(String value) {
            addCriterion("tenant_name >=", value, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameLessThan(String value) {
            addCriterion("tenant_name <", value, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameLessThanOrEqualTo(String value) {
            addCriterion("tenant_name <=", value, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameLike(String value) {
            addCriterion("tenant_name like", value, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameNotLike(String value) {
            addCriterion("tenant_name not like", value, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameIn(List<String> values) {
            addCriterion("tenant_name in", values, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameNotIn(List<String> values) {
            addCriterion("tenant_name not in", values, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameBetween(String value1, String value2) {
            addCriterion("tenant_name between", value1, value2, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTenantNameNotBetween(String value1, String value2) {
            addCriterion("tenant_name not between", value1, value2, "tenantName");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesIsNull() {
            addCriterion("tm_userNames is null");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesIsNotNull() {
            addCriterion("tm_userNames is not null");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesEqualTo(String value) {
            addCriterion("tm_userNames =", value, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesNotEqualTo(String value) {
            addCriterion("tm_userNames <>", value, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesGreaterThan(String value) {
            addCriterion("tm_userNames >", value, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesGreaterThanOrEqualTo(String value) {
            addCriterion("tm_userNames >=", value, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesLessThan(String value) {
            addCriterion("tm_userNames <", value, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesLessThanOrEqualTo(String value) {
            addCriterion("tm_userNames <=", value, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesLike(String value) {
            addCriterion("tm_userNames like", value, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesNotLike(String value) {
            addCriterion("tm_userNames not like", value, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesIn(List<String> values) {
            addCriterion("tm_userNames in", values, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesNotIn(List<String> values) {
            addCriterion("tm_userNames not in", values, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesBetween(String value1, String value2) {
            addCriterion("tm_userNames between", value1, value2, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andTmUsernamesNotBetween(String value1, String value2) {
            addCriterion("tm_userNames not between", value1, value2, "tmUsernames");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsIsNull() {
            addCriterion("harbor_projects is null");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsIsNotNull() {
            addCriterion("harbor_projects is not null");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsEqualTo(String value) {
            addCriterion("harbor_projects =", value, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsNotEqualTo(String value) {
            addCriterion("harbor_projects <>", value, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsGreaterThan(String value) {
            addCriterion("harbor_projects >", value, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsGreaterThanOrEqualTo(String value) {
            addCriterion("harbor_projects >=", value, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsLessThan(String value) {
            addCriterion("harbor_projects <", value, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsLessThanOrEqualTo(String value) {
            addCriterion("harbor_projects <=", value, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsLike(String value) {
            addCriterion("harbor_projects like", value, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsNotLike(String value) {
            addCriterion("harbor_projects not like", value, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsIn(List<String> values) {
            addCriterion("harbor_projects in", values, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsNotIn(List<String> values) {
            addCriterion("harbor_projects not in", values, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsBetween(String value1, String value2) {
            addCriterion("harbor_projects between", value1, value2, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andHarborProjectsNotBetween(String value1, String value2) {
            addCriterion("harbor_projects not between", value1, value2, "harborProjects");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsIsNull() {
            addCriterion("network_ids is null");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsIsNotNull() {
            addCriterion("network_ids is not null");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsEqualTo(String value) {
            addCriterion("network_ids =", value, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsNotEqualTo(String value) {
            addCriterion("network_ids <>", value, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsGreaterThan(String value) {
            addCriterion("network_ids >", value, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsGreaterThanOrEqualTo(String value) {
            addCriterion("network_ids >=", value, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsLessThan(String value) {
            addCriterion("network_ids <", value, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsLessThanOrEqualTo(String value) {
            addCriterion("network_ids <=", value, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsLike(String value) {
            addCriterion("network_ids like", value, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsNotLike(String value) {
            addCriterion("network_ids not like", value, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsIn(List<String> values) {
            addCriterion("network_ids in", values, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsNotIn(List<String> values) {
            addCriterion("network_ids not in", values, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsBetween(String value1, String value2) {
            addCriterion("network_ids between", value1, value2, "networkIds");
            return (Criteria) this;
        }

        public Criteria andNetworkIdsNotBetween(String value1, String value2) {
            addCriterion("network_ids not between", value1, value2, "networkIds");
            return (Criteria) this;
        }

        public Criteria andK8sPvsIsNull() {
            addCriterion("k8s_pvs is null");
            return (Criteria) this;
        }

        public Criteria andK8sPvsIsNotNull() {
            addCriterion("k8s_pvs is not null");
            return (Criteria) this;
        }

        public Criteria andK8sPvsEqualTo(String value) {
            addCriterion("k8s_pvs =", value, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsNotEqualTo(String value) {
            addCriterion("k8s_pvs <>", value, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsGreaterThan(String value) {
            addCriterion("k8s_pvs >", value, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsGreaterThanOrEqualTo(String value) {
            addCriterion("k8s_pvs >=", value, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsLessThan(String value) {
            addCriterion("k8s_pvs <", value, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsLessThanOrEqualTo(String value) {
            addCriterion("k8s_pvs <=", value, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsLike(String value) {
            addCriterion("k8s_pvs like", value, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsNotLike(String value) {
            addCriterion("k8s_pvs not like", value, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsIn(List<String> values) {
            addCriterion("k8s_pvs in", values, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsNotIn(List<String> values) {
            addCriterion("k8s_pvs not in", values, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsBetween(String value1, String value2) {
            addCriterion("k8s_pvs between", value1, value2, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sPvsNotBetween(String value1, String value2) {
            addCriterion("k8s_pvs not between", value1, value2, "k8sPvs");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesIsNull() {
            addCriterion("k8s_namespaces is null");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesIsNotNull() {
            addCriterion("k8s_namespaces is not null");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesEqualTo(String value) {
            addCriterion("k8s_namespaces =", value, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesNotEqualTo(String value) {
            addCriterion("k8s_namespaces <>", value, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesGreaterThan(String value) {
            addCriterion("k8s_namespaces >", value, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesGreaterThanOrEqualTo(String value) {
            addCriterion("k8s_namespaces >=", value, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesLessThan(String value) {
            addCriterion("k8s_namespaces <", value, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesLessThanOrEqualTo(String value) {
            addCriterion("k8s_namespaces <=", value, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesLike(String value) {
            addCriterion("k8s_namespaces like", value, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesNotLike(String value) {
            addCriterion("k8s_namespaces not like", value, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesIn(List<String> values) {
            addCriterion("k8s_namespaces in", values, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesNotIn(List<String> values) {
            addCriterion("k8s_namespaces not in", values, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesBetween(String value1, String value2) {
            addCriterion("k8s_namespaces between", value1, value2, "k8sNamespaces");
            return (Criteria) this;
        }

        public Criteria andK8sNamespacesNotBetween(String value1, String value2) {
            addCriterion("k8s_namespaces not between", value1, value2, "k8sNamespaces");
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

        public Criteria andClusterIdIsNull() {
            addCriterion("cluster_id is null");
            return (Criteria) this;
        }

        public Criteria andClusterIdIsNotNull() {
            addCriterion("cluster_id is not null");
            return (Criteria) this;
        }

        public Criteria andClusterIdEqualTo(Integer value) {
            addCriterion("cluster_id =", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotEqualTo(Integer value) {
            addCriterion("cluster_id <>", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdGreaterThan(Integer value) {
            addCriterion("cluster_id >", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("cluster_id >=", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLessThan(Integer value) {
            addCriterion("cluster_id <", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLessThanOrEqualTo(Integer value) {
            addCriterion("cluster_id <=", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdIn(List<Integer> values) {
            addCriterion("cluster_id in", values, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotIn(List<Integer> values) {
            addCriterion("cluster_id not in", values, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdBetween(Integer value1, Integer value2) {
            addCriterion("cluster_id between", value1, value2, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotBetween(Integer value1, Integer value2) {
            addCriterion("cluster_id not between", value1, value2, "clusterId");
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