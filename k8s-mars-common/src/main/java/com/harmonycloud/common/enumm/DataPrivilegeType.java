package com.harmonycloud.common.enumm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by anson on 18/6/20.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPrivilegeType {
    DataResourceTypeEnum type();
    String field();
}
