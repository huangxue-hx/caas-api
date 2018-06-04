package com.harmonycloud.common.enumm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限所管理的类。
 * 可以是出入参数类，也可为只在业务中使用到的类。
 * 确定只为出参或入参类时，请添加后缀In和Out，以便管理员区分。
 * 如LocalRoleIn为入参，管理员便知道这是在输入数据时作数据控制。
 * 如果出入参为同一个类并且都要做数据控制，请新建其他的类。
 * 单个字段的全称：
 * 1 作用在方法上为： 类名.方法名.字段名（暂时不做）
 * 2 作用在类上为：类名.字段名
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivilegeType {
    // 资源类型名称
    String name();
    String cnDesc() default "";
    String enDesc() default "";


}
