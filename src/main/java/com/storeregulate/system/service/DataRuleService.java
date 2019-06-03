package com.storeregulate.system.service;

import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.List;

public interface DataRuleService<T, ID extends Serializable> {

	/**
	 * 给传入的条件拼接数据范围条件
	 * @param specification
	 * @return 已拼接数据权限的条件
	 */
	Specification<T> withDataRule(Specification<T> specification);
	
	/**
	 * 根据传入的查询条件拼接上数据范围条件后查询
	 * @param specification
	 * @return 带权限范围的数据集合
	 */
	List<T> findAllWithDataRule(Specification<T> specification);
	
	/**
	 * 根据传入的查询条件拼接上数据范围条件后查询（分页）
	 * @param specification
	 * @param pageable
	 * @return 带权限范围的数据集合
	 */
	Page<T> findAllWithDataRule(Specification<T> specification, Pageable pageable);

	/**
	 * 根据对象模板，查询带数据权限范围的集合
	 * @param entity
	 * @return
	 */
	List<T> findAllWithDataRule(T entity);

	/**
	 * 根据对象模板，查询带数据权限范围的集合（分页）
	 * @param entity
	 * @param pageable
	 * @return
	 */
	Page<T> findAllWithDataRule(T entity, Pageable pageable);
	
	/**
	 * 根据对象模板，和指定的 Matcher 查询带数据权限范围的集合
	 * @param entity
	 * @param matcher 一般用来 ignore一组不需要自动匹配的属性
	 * @return
	 */
	List<T> findAllWithDataRule(T entity, ExampleMatcher matcher);

	/**
	 * 根据对象模板，和指定的 Matcher 查询带数据权限范围的集合（分页）
	 * @param entity
	 * @param matcher 一般用来 ignore一组不需要自动匹配的属性
	 * @return
	 */
	Page<T> findAllWithDataRule(T entity, ExampleMatcher matcher, Pageable pageable);
}
