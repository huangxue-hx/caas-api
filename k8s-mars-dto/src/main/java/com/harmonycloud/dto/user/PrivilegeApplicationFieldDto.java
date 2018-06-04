package com.harmonycloud.dto.user;

import com.harmonycloud.common.enumm.PrivilegeField;
import com.harmonycloud.common.enumm.PrivilegeType;

/**
 * 业务内部使用的数据权限管理类--应用
 * field注解命名规则一般为方法名+具体字段（key）名
 */
@PrivilegeType(name = "application")
public class PrivilegeApplicationFieldDto {

    @PrivilegeField(name = "selectApp.name", cnDesc = "应用名称")
    private String nameInSelectApp;

    @PrivilegeField(name = "selectService.name", cnDesc = "服务名称")
    private String nameInSelectService;

    public String getNameInSelectService() {
        return nameInSelectService;
    }

    public void setNameInSelectService(String nameInSelectService) {
        this.nameInSelectService = nameInSelectService;
    }

    public String getNameInSelectApp() {
        return nameInSelectApp;
    }

    public void setNameInSelectApp(String nameInSelectApp) {
        this.nameInSelectApp = nameInSelectApp;
    }
}
