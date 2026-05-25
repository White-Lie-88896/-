package com.sky.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行公共字段自动填充处理。
 * 通常用于 Mapper 层的方法，在执行 INSERT 或 UPDATE 操作时，
 * 自动填充 createTime, createUser, updateTime, updateUser 等字段。
 */
@Target(ElementType.METHOD) // 标识该注解只能用于方法上
@Retention(RetentionPolicy.RUNTIME) // 标识该注解在运行时有效，允许反射读取
public @interface AutoFill {
    /**
     * 数据库操作类型：UPDATE（更新） 或 INSERT（插入）
     * 通过该属性来区分当前方法执行的是哪种操作，
     * 从而决定在 AOP 切面中填充哪些公共字段。
     */
    // 定义注解参数，value 是特殊属性名，允许在使用时省略属性名直接传参
    OperationType value();
}
