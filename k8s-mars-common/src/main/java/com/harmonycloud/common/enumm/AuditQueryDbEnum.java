package com.harmonycloud.common.enumm;

/**
 * @Author jiangmi
 * @Description 将url内需查询数据库对应的Service名称与code进行对应
 * @Date created in 2018-1-13
 * @Modified
 */
public enum AuditQueryDbEnum {

    USERSERVICE(1),
    ROLELOCALSERVICE(2),
    BUILDENVIRONMENTSERVICE(3),
    DOCKERFILESERVICE(4),
    JOBSERVICE(5),
    TENANTSERVICE(6),
    NAMESPACELOCALSERVICE(7),
    HARBORPROJECTSERVICE(8),
    LOCALROLESERVICE(9),
    CLUSTERSERVICE(10),
    LOGBACKUPRULEMAPPER(11),
    CONFIGCENTERSERVICE(12),
    PROJECT(13);

    private final Integer code;

    AuditQueryDbEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}
