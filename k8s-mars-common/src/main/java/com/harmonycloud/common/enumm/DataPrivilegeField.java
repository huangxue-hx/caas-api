package com.harmonycloud.common.enumm;

import java.lang.annotation.*;

/**
 * Created by anson on 18/6/21.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DataPrivilegeField {
    int type();
}
