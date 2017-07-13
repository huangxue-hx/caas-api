package com.harmonycloud.dto.user;

import java.util.List;

import com.harmonycloud.dao.user.bean.User;

public class SummaryUserInfo {
    private Integer userSum;// 用户总数
    private Integer adminSum;// 管理员总数,
    private Integer activeSum;// 活跃用户数量 ,
    private Integer unActiveSum;// 不活跃用户数量 ,
    private Integer userPausedSum; // 被阻止用户，
    private Integer userNormalSum; // 正常用户，
    private Integer unauthorizedUserSum;// 未授权用户
    private List<User> adminList;// 管理员列表
    private List<User> activeUserList;// 活跃用户列表
    private List<User> unActiveUserList;// 不活跃用户列表
    private List<User> unauthorizedUserList;// 未授权用户列表
    private List<User> pausedUserList;// 被阻止用户列表
    private List<User> normalUserList;// 正常用户列表
   
    public Integer getUnActiveSum() {
        return unActiveSum;
    }
    public void setUnActiveSum(Integer unActiveSum) {
        this.unActiveSum = unActiveSum;
    }
    public List<User> getUnActiveUserList() {
        return unActiveUserList;
    }
    public void setUnActiveUserList(List<User> unActiveUserList) {
        this.unActiveUserList = unActiveUserList;
    }
    public Integer getUserSum() {
        return userSum;
    }
    public void setUserSum(Integer userSum) {
        this.userSum = userSum;
    }
    public Integer getAdminSum() {
        return adminSum;
    }
    public void setAdminSum(Integer adminSum) {
        this.adminSum = adminSum;
    }
    public Integer getActiveSum() {
        return activeSum;
    }
    public void setActiveSum(Integer activeSum) {
        this.activeSum = activeSum;
    }
    public Integer getUserPausedSum() {
        return userPausedSum;
    }
    public void setUserPausedSum(Integer userPausedSum) {
        this.userPausedSum = userPausedSum;
    }
    public Integer getUserNormalSum() {
        return userNormalSum;
    }
    public void setUserNormalSum(Integer userNormalSum) {
        this.userNormalSum = userNormalSum;
    }
    public Integer getUnauthorizedUserSum() {
        return unauthorizedUserSum;
    }
    public void setUnauthorizedUserSum(Integer unauthorizedUserSum) {
        this.unauthorizedUserSum = unauthorizedUserSum;
    }
    public List<User> getAdminList() {
        return adminList;
    }
    public void setAdminList(List<User> adminList) {
        this.adminList = adminList;
    }
    public List<User> getActiveUserList() {
        return activeUserList;
    }
    public void setActiveUserList(List<User> activeUserList) {
        this.activeUserList = activeUserList;
    }
    public List<User> getUnauthorizedUserList() {
        return unauthorizedUserList;
    }
    public void setUnauthorizedUserList(List<User> unauthorizedUserList) {
        this.unauthorizedUserList = unauthorizedUserList;
    }
    public List<User> getPausedUserList() {
        return pausedUserList;
    }
    public void setPausedUserList(List<User> pausedUserList) {
        this.pausedUserList = pausedUserList;
    }
    public List<User> getNormalUserList() {
        return normalUserList;
    }
    public void setNormalUserList(List<User> normalUserList) {
        this.normalUserList = normalUserList;
    }

}
