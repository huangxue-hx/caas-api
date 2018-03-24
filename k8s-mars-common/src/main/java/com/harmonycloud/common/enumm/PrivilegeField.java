package com.harmonycloud.common.enumm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限所管理的字段。
 * 可把在您的业务中使用到的其他字段添加到数据权限管理的类中。
 * 单个字段的全称：
 * 1 作用在方法上为： 类名.方法名.字段名（暂时不做）
 * 2 作用在类上为：类名.字段名
 *
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivilegeField {
    // 字段名称
    String name();
    // 字段所属的类
    String type() default "";
    // 字段所属的方法
    String function() default "";
    String enDesc() default "";
    String cnDesc() default "";
    // 字段列表，为空的话和name一致
//    String[] fields() default {};
}
