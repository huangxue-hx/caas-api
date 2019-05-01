package com.harmonycloud.dto.user;

import com.harmonycloud.dao.user.bean.User;

/**
 * @author ruanjin
 * @since 2019/4/29 11:34
 */
public class UserSyncDto extends User {

    //1：新增、2：修改、3：删除
    private Integer operateType;

    public Integer getOperateType() {
        return operateType;
    }

    public void setOperateType(Integer operateType) {
        this.operateType = operateType;
    }
}
