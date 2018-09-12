package com.harmonycloud.common.enumm;

import java.lang.annotation.*;

/**
 * Created by anson on 18/6/20.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DataPrivilegeType {
    DataResourceTypeEnum type();
}
