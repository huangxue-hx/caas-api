package com.harmonycloud.dao.application.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogBackupRuleExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LogBackupRuleExample() {
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

        public Criteria andBackupDirIsNull() {
            addCriterion("backup_dir is null");
            return (Criteria) this;
        }

        public Criteria andBackupDirIsNotNull() {
            addCriterion("backup_dir is not null");
            return (Criteria) this;
        }

        public Criteria andBackupDirEqualTo(String value) {
            addCriterion("backup_dir =", value, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirNotEqualTo(String value) {
            addCriterion("backup_dir <>", value, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirGreaterThan(String value) {
            addCriterion("backup_dir >", value, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirGreaterThanOrEqualTo(String value) {
            addCriterion("backup_dir >=", value, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirLessThan(String value) {
            addCriterion("backup_dir <", value, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirLessThanOrEqualTo(String value) {
            addCriterion("backup_dir <=", value, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirLike(String value) {
            addCriterion("backup_dir like", value, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirNotLike(String value) {
            addCriterion("backup_dir not like", value, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirIn(List<String> values) {
            addCriterion("backup_dir in", values, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirNotIn(List<String> values) {
            addCriterion("backup_dir not in", values, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirBetween(String value1, String value2) {
            addCriterion("backup_dir between", value1, value2, "backupDir");
            return (Criteria) this;
        }

        public Criteria andBackupDirNotBetween(String value1, String value2) {
            addCriterion("backup_dir not between", value1, value2, "backupDir");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeIsNull() {
            addCriterion("days_before is null");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeIsNotNull() {
            addCriterion("days_before is not null");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeEqualTo(Integer value) {
            addCriterion("days_before =", value, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeNotEqualTo(Integer value) {
            addCriterion("days_before <>", value, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeGreaterThan(Integer value) {
            addCriterion("days_before >", value, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeGreaterThanOrEqualTo(Integer value) {
            addCriterion("days_before >=", value, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeLessThan(Integer value) {
            addCriterion("days_before <", value, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeLessThanOrEqualTo(Integer value) {
            addCriterion("days_before <=", value, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeIn(List<Integer> values) {
            addCriterion("days_before in", values, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeNotIn(List<Integer> values) {
            addCriterion("days_before not in", values, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeBetween(Integer value1, Integer value2) {
            addCriterion("days_before between", value1, value2, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysBeforeNotBetween(Integer value1, Integer value2) {
            addCriterion("days_before not between", value1, value2, "daysBefore");
            return (Criteria) this;
        }

        public Criteria andDaysDurationIsNull() {
            addCriterion("days_duration is null");
            return (Criteria) this;
        }

        public Criteria andDaysDurationIsNotNull() {
            addCriterion("days_duration is not null");
            return (Criteria) this;
        }

        public Criteria andDaysDurationEqualTo(Integer value) {
            addCriterion("days_duration =", value, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationNotEqualTo(Integer value) {
            addCriterion("days_duration <>", value, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationGreaterThan(Integer value) {
            addCriterion("days_duration >", value, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationGreaterThanOrEqualTo(Integer value) {
            addCriterion("days_duration >=", value, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationLessThan(Integer value) {
            addCriterion("days_duration <", value, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationLessThanOrEqualTo(Integer value) {
            addCriterion("days_duration <=", value, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationIn(List<Integer> values) {
            addCriterion("days_duration in", values, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationNotIn(List<Integer> values) {
            addCriterion("days_duration not in", values, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationBetween(Integer value1, Integer value2) {
            addCriterion("days_duration between", value1, value2, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andDaysDurationNotBetween(Integer value1, Integer value2) {
            addCriterion("days_duration not between", value1, value2, "daysDuration");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedIsNull() {
            addCriterion("max_snapshot_speed is null");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedIsNotNull() {
            addCriterion("max_snapshot_speed is not null");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedEqualTo(String value) {
            addCriterion("max_snapshot_speed =", value, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedNotEqualTo(String value) {
            addCriterion("max_snapshot_speed <>", value, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedGreaterThan(String value) {
            addCriterion("max_snapshot_speed >", value, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedGreaterThanOrEqualTo(String value) {
            addCriterion("max_snapshot_speed >=", value, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedLessThan(String value) {
            addCriterion("max_snapshot_speed <", value, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedLessThanOrEqualTo(String value) {
            addCriterion("max_snapshot_speed <=", value, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedLike(String value) {
            addCriterion("max_snapshot_speed like", value, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedNotLike(String value) {
            addCriterion("max_snapshot_speed not like", value, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedIn(List<String> values) {
            addCriterion("max_snapshot_speed in", values, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedNotIn(List<String> values) {
            addCriterion("max_snapshot_speed not in", values, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedBetween(String value1, String value2) {
            addCriterion("max_snapshot_speed between", value1, value2, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedNotBetween(String value1, String value2) {
            addCriterion("max_snapshot_speed not between", value1, value2, "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedIsNull() {
            addCriterion("max_restore_speed is null");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedIsNotNull() {
            addCriterion("max_restore_speed is not null");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedEqualTo(String value) {
            addCriterion("max_restore_speed =", value, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedNotEqualTo(String value) {
            addCriterion("max_restore_speed <>", value, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedGreaterThan(String value) {
            addCriterion("max_restore_speed >", value, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedGreaterThanOrEqualTo(String value) {
            addCriterion("max_restore_speed >=", value, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedLessThan(String value) {
            addCriterion("max_restore_speed <", value, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedLessThanOrEqualTo(String value) {
            addCriterion("max_restore_speed <=", value, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedLike(String value) {
            addCriterion("max_restore_speed like", value, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedNotLike(String value) {
            addCriterion("max_restore_speed not like", value, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedIn(List<String> values) {
            addCriterion("max_restore_speed in", values, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedNotIn(List<String> values) {
            addCriterion("max_restore_speed not in", values, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedBetween(String value1, String value2) {
            addCriterion("max_restore_speed between", value1, value2, "maxRestoreSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedNotBetween(String value1, String value2) {
            addCriterion("max_restore_speed not between", value1, value2, "maxRestoreSpeed");
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

        public Criteria andAvailableIsNull() {
            addCriterion("available is null");
            return (Criteria) this;
        }

        public Criteria andAvailableIsNotNull() {
            addCriterion("available is not null");
            return (Criteria) this;
        }

        public Criteria andAvailableEqualTo(Boolean value) {
            addCriterion("available =", value, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableNotEqualTo(Boolean value) {
            addCriterion("available <>", value, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableGreaterThan(Boolean value) {
            addCriterion("available >", value, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("available >=", value, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableLessThan(Boolean value) {
            addCriterion("available <", value, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableLessThanOrEqualTo(Boolean value) {
            addCriterion("available <=", value, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableIn(List<Boolean> values) {
            addCriterion("available in", values, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableNotIn(List<Boolean> values) {
            addCriterion("available not in", values, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableBetween(Boolean value1, Boolean value2) {
            addCriterion("available between", value1, value2, "available");
            return (Criteria) this;
        }

        public Criteria andAvailableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("available not between", value1, value2, "available");
            return (Criteria) this;
        }

        public Criteria andClusterIdLikeInsensitive(String value) {
            addCriterion("upper(cluster_id) like", value.toUpperCase(), "clusterId");
            return (Criteria) this;
        }

        public Criteria andBackupDirLikeInsensitive(String value) {
            addCriterion("upper(backup_dir) like", value.toUpperCase(), "backupDir");
            return (Criteria) this;
        }

        public Criteria andMaxSnapshotSpeedLikeInsensitive(String value) {
            addCriterion("upper(max_snapshot_speed) like", value.toUpperCase(), "maxSnapshotSpeed");
            return (Criteria) this;
        }

        public Criteria andMaxRestoreSpeedLikeInsensitive(String value) {
            addCriterion("upper(max_restore_speed) like", value.toUpperCase(), "maxRestoreSpeed");
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