package com.storeregulate.system.query.annotations;

import java.lang.annotation.*;

/**
 * 标识实体对象(Entity)中的游离属性字段<br/>
 * 
 * 旨在提供基于form表单的或条件的查询，当前限制为只能使用在String类型游离属性上<br/>
 * 示例：
 * <code>
 * @QueryOr(target = {"name","username","displayName"})
 * String filter;
 * </code>
 * 解析完成后会生成类似如下查询，其中模糊查询的形式依赖于{@link QueryLike} 注解
 * <strong>NOTE:</strong> 条件解析成功后，name 、  username 和 displayName 的其他查询会被忽略
 * <code>
 * and (u.name like '%filter%' or u.username like '%filter%' or u.display_name like '%filter%')
 * @author jiawei
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QueryOr {
	String[] target();
}