package com.storeregulate.system.service;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.storeregulate.common.utils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import com.google.common.collect.Lists;

import com.storeregulate.system.entity.Repairable;
import com.storeregulate.system.query.builder.LofterQueryPredicateBuilder;
import com.storeregulate.system.repository.SupportRepository;



public class BaseService<T, ID extends Serializable, R extends SupportRepository<T, ID>>
		implements CrudService<T, ID>, DataRuleService<T, Serializable> {

	private static final Logger logger = LoggerFactory.getLogger(BaseService.class);

	private Class<T> clazz;
	protected R repository;
	protected boolean isReparable = false;
	protected DataRuleWapper dataRuleWapper;

	@SuppressWarnings("unchecked")
	public BaseService() {
		Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.clazz = clazz;
		this.isReparable = Repairable.class.isAssignableFrom(clazz);
	}

	@Autowired
	public <S extends R> void setRepository(S repository) {
		this.repository = repository;
	}

	@Autowired
	public void setDataRuleWapper(DataRuleWapper dataRuleWapper) {
		this.dataRuleWapper = dataRuleWapper;
	}

	@Override
	public <S extends T> S save(S entity) {
		return repository.saveAndFlush(entity);
	}

	@Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		return repository.saveAll(entities);
	}

	@Override
	public T findOne(ID id) {
		return repository.findById(id).orElse(null);
	}

	@Override
	public T getOne(ID id) {
		return repository.findById(id).orElse(null);
	}

	@Override
	public boolean exists(ID id) {
		return repository.existsById(id);
	}

	@Override
	public Iterable<T> findAll() {
		return repository.findAll();
	}

	@Override
	public Iterable<T> findAll(Iterable<ID> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public void delete(ID id) {
		delete(id, false);
	}

	@Override
	public void delete(List<ID> ids) {
		for (ID id : ids) {
			delete(id, false);
		}
	}

	@Override
	public void delete(ID[] ids) {
		delete(Arrays.asList(ids), false);
	}

	@Override
	public void delete(T entity) {
		delete(entity, false);
	}

	@Override
	public void delete(Iterable<? extends T> entities) {
		delete(entities, false);
	}

	@Override
	public void deleteAll() {
		deleteAll(false);
	}
	@Override
	public <S extends T> Iterable<S> findAllByExample(Example<S> example){
		if(isReparable) {
			Repairable e = (Repairable) example.getProbe();
			if (e.getIsDeleted() == null) {
				e.setIsDeleted(false);
			}
		}
		;
		return repository.findAll(example);
	}
    @Override
	public <S extends T> Page<S> findAllAndPageable(Example<S> example, Pageable pageable){
		if(isReparable) {
			Repairable e = (Repairable) example.getProbe();
			if (e.getIsDeleted() == null) {
				e.setIsDeleted(false);
			}
		}
		return repository.findAll(Example.of(example.getProbe()), pageable);
	}

	/**
	 * 解析form表单提交的查询entity，生成复杂查询（全部，过滤已删除数据，不带支持权限）
	 * @param entity
	 * @return
	 */
	@Override
	public List<T> findAll(T entity) {
		return repository.findAll(handleQueryAnnotation(entity));
	}

	/**
	 * 解析form表单提交的查询entity，生成复杂查询（分页，过滤已删除数据，不带支持权限）
	 * @param entity
	 * @param pageable
	 * @return
	 */
	@Override
	public Page<T> findAll(T entity, Pageable pageable) {
		return repository.findAll(handleQueryAnnotation(entity), pageable);
	}
	/**
	 * 解析form表单提交的查询entity，生成复杂查询（全部，过滤已删除数据，不带支持权限）
	 * @param entity
	 * @return
	 */
	@Override
	public List<T> findAll(T entity, ExampleMatcher matcher) {
		return repository.findAll(handleQueryAnnotation(entity, matcher));
	}
	
	/**
	 * 解析form表单提交的查询entity，生成复杂查询（分页，过滤已删除数据，不带支持权限）
	 * @param entity
	 * @param pageable
	 * @return
	 */
	@Override
	public Page<T> findAll(T entity, ExampleMatcher matcher, Pageable pageable) {
		return repository.findAll(handleQueryAnnotation(entity, matcher), pageable);
	}
	
	/**
	 * 解析form表单提交的查询entity，生成复杂查询（全部，过滤已删除数据，支持权限）
	 * @param entity
	 * @return
	 */
	@Override
	public List<T> findAllWithDataRule(T entity) {
		return repository.findAll(withDataRule(handleQueryAnnotation(entity)));
	}
	
	/**
	 * 解析form表单提交的查询entity，生成复杂查询（分页，过滤已删除数据，支持权限）
	 * @param entity
	 * @param pageable
	 * @return
	 */
	@Override
	public Page<T> findAllWithDataRule(T entity, Pageable pageable) {
		return repository.findAll(withDataRule(handleQueryAnnotation(entity)), pageable);
	}
	
	/**
	 * 解析form表单提交的查询entity，生成复杂查询（全部，过滤已删除数据，支持权限）
	 * @param entity
	 * @return
	 */
	@Override
	public List<T> findAllWithDataRule(T entity, ExampleMatcher matcher) {
		return repository.findAll(withDataRule(handleQueryAnnotation(entity, matcher)));
	}
	
	/**
	 * 解析form表单提交的查询entity，生成复杂查询（分页，过滤已删除数据，支持权限）
	 * @param entity
	 * @param pageable
	 * @return
	 */
	@Override
	public Page<T> findAllWithDataRule(T entity, ExampleMatcher matcher, Pageable pageable) {
		return repository.findAll(withDataRule(handleQueryAnnotation(entity, matcher)), pageable);
	}

	@Override
	public void delete(ID id, boolean isPhysical) {
		if (!checkDeleteable(id)) {
			return;
		}

		if (isReparable && !isPhysical) {
			tagDelete(id);
		} else {
			repository.deleteById(id);
		}
	}

	@Override
	public void delete(List<ID> ids, boolean isPhysical) {
		for (ID id : ids) {
			delete(id, isPhysical);
		}
	}

	@Override
	public void delete(ID[] ids, boolean isPhysical) {
		delete(Arrays.asList(ids), isPhysical);
	}

	@Override
	public void delete(T entity, boolean isPhysical) {
		if (!checkDeleteable(entity)) {
			return;
		}

		if (isReparable && !isPhysical) {
			tagDelete(entity);
		} else {
			repository.delete(entity);
		}
	}

	@Override
	public void delete(Iterable<? extends T> entities, boolean isPhysical) {
		for (T entity : entities) {
			if (!checkDeleteable(entity)) {
				return;
			}
		}

		if (isReparable && !isPhysical) {
			for (T entity : entities) {
				tagDelete(entity);
			}
		} else {
			repository.deleteAll(entities);
		}
	}

	@Override
	public void deleteAll(boolean isPhysical) {
		if (isReparable && !isPhysical) {
			throw new RuntimeException("The Reparable class cannot be tag delete all");
		} else {
			repository.deleteAll();
		}
	}

	protected void tagDelete(ID id) {
		T entity = findOne(id);
		tagDelete(entity);
	}

	protected void tagDelete(T entity) {
		if (null == entity) {
			return;
		}

		if (isReparable) {
			try {
				((Repairable) entity).setIsDeleted(true);
				save(entity);
			} catch (ClassCastException e) {
				throw new RuntimeException("The given class " + entity.getClass() + " is not a Repairable("
						+ Repairable.class + ") class");
			}
		} else {
			throw new RuntimeException(
					"The given class " + entity.getClass() + " is not a Repairable(" + Repairable.class + ") class");
		}
	}

	protected boolean checkDeleteable(T entity) {
		return true;
	}

	protected boolean checkDeleteable(ID id) {
		return true;
	}

	@Override
	public T update(ID id, T entity) {
		T entity2 = findOne(id);
		try {
			new BeanUtils().copyProperties(entity2, entity, true);
			entity2 = save(entity2);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		}

		return entity2;
	}

	@Override
	public Specification<T> withDataRule(Specification<T> specification) {
		if (null != dataRuleWapper) {
			Specifications<T> specifications = Specifications.where(specification)
					.and(dataRuleWapper.getDataRuleSpecification(clazz));
			return specifications;
		} else {
			logger.warn("DataRuleWapper not been providored.");
			return specification;
		}
	}

	/**
	 * 解析实体类上的Query*注解生成Specification
	 * 
	 * @param entity
	 * @return
	 */
	@Override
	public Specification<T> handleQueryAnnotation(T entity) {
		if (entity == null) {
			return null;
		}
		Boolean isReparable = this.isReparable;
		return new Specification<T>() {

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = Lists.newArrayList();
				if (isReparable) {
					Predicate isDeletePredicate = builder.equal(root.get("isDeleted"), false);
					predicates.add(isDeletePredicate);
				}

				predicates.add(LofterQueryPredicateBuilder.getPredicate(root, builder, entity));
				Predicate[] p = new Predicate[predicates.size()];
				return builder.and(predicates.toArray(p));
			}

		};
	}
	/**
	 * 解析实体类上的Query*注解生成Specification, 带过滤条件
	 * 
	 * @param entity
	 * @return
	 */
	@Override
	public Specification<T> handleQueryAnnotation(T entity, ExampleMatcher matcher) {
		if (entity == null) {
			return null;
		}
		Boolean isReparable = this.isReparable;
		return new Specification<T>() {
			
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = Lists.newArrayList();
				if (isReparable) {
					Predicate isDeletePredicate = builder.equal(root.get("isDeleted"), false);
					predicates.add(isDeletePredicate);
				}
				
				predicates.add(LofterQueryPredicateBuilder.getPredicate(root, builder, entity, matcher));
				Predicate[] p = new Predicate[predicates.size()];
				return builder.and(predicates.toArray(p));
			}
			
		};
	}

	@Override
	public List<T> findAllWithDataRule(Specification<T> specification) {
		return findAll(withDataRule(specification));
	}

	@Override
	public Page<T> findAllWithDataRule(Specification<T> specification, Pageable pageable) {
		return findAll(withDataRule(specification), pageable);
	}

	/*@Override
	public T findOne(Specification<T> spec) {
		return repository.findOne(spec);
	}
*/
	@Override
	public List<T> findAll(Specification<T> spec) {
		return repository.findAll(spec);
	}

	@Override
	public Page<T> findAll(Specification<T> spec, Pageable pageable) {
		return repository.findAll(spec, pageable);
	}

	@Override
	public List<T> findAll(Specification<T> spec, Sort sort) {
		return repository.findAll(spec, sort);
	}

	@Override
	public long count(Specification<T> spec) {
		return repository.count(spec);
	}


	@Override
	public Specification<T> handleQueryAnnotation(T entity,List<Long> list) {
		if (entity == null) {
			return null;
		}
		Boolean isReparable = this.isReparable;
		return new Specification<T>() {

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = Lists.newArrayList();
				if (isReparable) {
					Predicate isDeletePredicate = builder.equal(root.get("isDeleted"), false);
					predicates.add(isDeletePredicate);
				}

				if(list.size()>0){
					Predicate predicate = builder.not(builder.in(root.get("id")).value(list));
					predicates.add(predicate);
				}
				predicates.add(LofterQueryPredicateBuilder.getPredicate(root, builder, entity));
				Predicate[] p = new Predicate[predicates.size()];
				return builder.and(predicates.toArray(p));
			}

		};
	}
	
	/**
	 * 解析实体类上的Query*注解生成Specification, 带过滤条件
	 * 
	 * @param entity
	 * @param isReparable false 查询已经删除的数据
	 * @return
	 */
	@Override
	public Specification<T> handleQueryAnnotationChinalife(T entity, ExampleMatcher matcher,Boolean isReparable) {
		if (entity == null) {
			return null;
		}
		return new Specification<T>() {
			
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = Lists.newArrayList();
				if (isReparable) {
					Predicate isDeletePredicate = builder.equal(root.get("isDeleted"), false);
					predicates.add(isDeletePredicate);
				}
				
				predicates.add(LofterQueryPredicateBuilder.getPredicate(root, builder, entity, matcher));
				Predicate[] p = new Predicate[predicates.size()];
				return builder.and(predicates.toArray(p));
			}
			
		};
	}
}
