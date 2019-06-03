package com.storeregulate.system.query.annotations;

import org.springframework.data.domain.ExampleMatcher.StringMatcher;

import java.lang.annotation.*;

/**
 * 标识实体对象(Entity)的字段
 * 自动封装Specification时将该字段解析为like查询
 * 只能应用与String类型的属性，禁止用于被@Clob注解标记的属性
 * @author jiawei
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QueryLike {
	StringMatcher matcher() default StringMatcher.CONTAINING;
}
