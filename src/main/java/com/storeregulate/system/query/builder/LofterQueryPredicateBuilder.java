package com.storeregulate.system.query.builder;

import com.storeregulate.system.exceptions.IllegalAnnotationError;
import com.storeregulate.system.query.annotations.*;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.repository.core.support.ExampleMatcherAccessor;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class LofterQueryPredicateBuilder {

	private static List<Class<? extends Annotation>> LOFTER_ANNOTATIONS_WITHOUT_OR;

	private static final Set<PersistentAttributeType> ASSOCIATION_TYPES;

	private static final Logger logger = LoggerFactory.getLogger(LofterQueryPredicateBuilder.class);

	static {
		ASSOCIATION_TYPES = new HashSet<PersistentAttributeType>(
				Arrays.asList(PersistentAttributeType.MANY_TO_MANY, PersistentAttributeType.MANY_TO_ONE,
						PersistentAttributeType.ONE_TO_MANY, PersistentAttributeType.ONE_TO_ONE));
		LOFTER_ANNOTATIONS_WITHOUT_OR = Arrays.asList(QueryLike.class, QueryGreaterEqual.class, QueryGreaterThan.class,
				QueryLessEqual.class, QueryLessThan.class);
	}
	private LofterQueryPredicateBuilder() {
		
	}

	/**
	 * Extract the {@link Predicate} representing the {@link Example}.
	 *
	 * @param root
	 *            must not be {@literal null}.
	 * @param cb
	 *            must not be {@literal null}.
	 * @param entity
	 *            must not be {@literal null}.
	 * @return never {@literal null}.
	 */
	public static <T> Predicate getPredicate(Root<T> root, CriteriaBuilder cb, T entity) {

		Assert.notNull(root, "Root must not be null!");
		Assert.notNull(cb, "CriteriaBuilder must not be null!");
		Assert.notNull(cb, "Entity must not be null!");

		ExampleMatcher matcher = ExampleMatcher.matching();
		List<Predicate> predicates = getPredicates("", cb, root, root.getModel(), entity,
				(Class<T>) ClassUtils.getUserClass(entity.getClass()), matcher, new PathNode("root", null, entity));

		if (predicates.isEmpty()) {
			return cb.isTrue(cb.literal(true));
		}

		if (predicates.size() == 1) {
			return predicates.iterator().next();
		}

		Predicate[] array = predicates.toArray(new Predicate[predicates.size()]);

		return cb.and(array);
	}
	public static <T> Predicate getPredicate(Root<T> root, CriteriaBuilder cb, T entity, ExampleMatcher matcher) {
		
		Assert.notNull(root, "Root must not be null!");
		Assert.notNull(cb, "CriteriaBuilder must not be null!");
		Assert.notNull(cb, "Entity must not be null!");
		
		if(matcher == null){
			matcher = ExampleMatcher.matching();
		}
		List<Predicate> predicates = getPredicates("", cb, root, root.getModel(), entity,
				(Class<T>) ClassUtils.getUserClass(entity.getClass()), matcher, new PathNode("root", null, entity));
		
		if (predicates.isEmpty()) {
			return cb.isTrue(cb.literal(true));
		}
		
		if (predicates.size() == 1) {
			return predicates.iterator().next();
		}
		
		Predicate[] array = predicates.toArray(new Predicate[predicates.size()]);
		
		return cb.and(array);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static List<Predicate> getPredicates(String path, CriteriaBuilder cb, Path<?> from, ManagedType<?> type,
			Object entity, Class<?> probeType, ExampleMatcher matcher, PathNode currentNode) {

		List<Predicate> predicates = new ArrayList<Predicate>();
		Field idField = getIdField(entity);
		Object idFieldValue = getFieldValue(entity, idField);
		if(!StringUtils.isEmpty(idFieldValue)){
			ExampleMatcherAccessor exampleAccessorForId = new ExampleMatcherAccessor(matcher);
			String currentPath = !StringUtils.hasText(path) ? idField.getName() : path + "." + idField.getName();

			if (!exampleAccessorForId.isIgnoredPath(currentPath)) {
				predicates.add(cb.equal(from.get(idField.getName()), idFieldValue));
				return predicates;
			}
		}
		DirectFieldAccessFallbackBeanWrapper beanWrapper = new DirectFieldAccessFallbackBeanWrapper(entity);

		Set<Field> withoutOrFields = getWithoutOrFields(entity.getClass());
		Set<Field> orFields = getFieldsWithLofterOr(entity.getClass());

		matcher = getPredicateFromAnnotatedFieldsForOr(path, cb, from, entity, matcher, predicates, orFields,
				withoutOrFields);
		matcher = getPredicateFromAnnotatedFieldsWithoutOr(path, cb, from, entity, matcher, predicates, withoutOrFields);
		ExampleMatcherAccessor exampleAccessor = new ExampleMatcherAccessor(matcher);
		for (SingularAttribute attribute : type.getSingularAttributes()) {

			String currentPath = !StringUtils.hasText(path) ? attribute.getName() : path + "." + attribute.getName();

			if (exampleAccessor.isIgnoredPath(currentPath)) {
				continue;
			}

			Object attributeValue = exampleAccessor.getValueTransformerForPath(currentPath)
					.convert(beanWrapper.getPropertyValue(attribute.getName()));

			if (attributeValue == null) {

				if (exampleAccessor.getNullHandler().equals(ExampleMatcher.NullHandler.INCLUDE)) {
					predicates.add(cb.isNull(from.get(attribute)));
				}
				continue;
			}

			if (attribute.getPersistentAttributeType().equals(PersistentAttributeType.EMBEDDED)) {

				predicates.addAll(getPredicates(currentPath, cb, from.get(attribute.getName()),
						(ManagedType<?>) attribute.getType(), attributeValue, probeType, matcher, currentNode));
				continue;
			}

			if (isAssociation(attribute)) {

				if (!(from instanceof From)) {
					throw new JpaSystemException(new IllegalArgumentException(
							String.format("Unexpected path type for %s. Found %s where From.class was expected.",
									currentPath, from)));
				}

				PathNode node = currentNode.add(attribute.getName(), attributeValue);
				if (node.spansCycle()) {
					throw new InvalidDataAccessApiUsageException(
							String.format("Path '%s' from root %s must not span a cyclic property reference!\r\n%s",
									currentPath, ClassUtils.getShortName(probeType), node));
				}
				predicates.addAll(getPredicates(currentPath, cb, ((From<?, ?>) from).join(attribute.getName()),
						(ManagedType<?>) attribute.getType(), attributeValue, probeType, matcher, node));

				continue;
			}

			if (attribute.getJavaType().equals(String.class)) {

				Expression<String> expression = from.get(attribute);
				if (exampleAccessor.isIgnoreCaseForPath(currentPath)) {
					expression = cb.lower(expression);
					attributeValue = attributeValue.toString().toLowerCase();
				}
				
				if(StringUtils.isEmpty(attributeValue)) {
					continue;
				}

				switch (exampleAccessor.getStringMatcherForPath(currentPath)) {

				case DEFAULT:
				case EXACT:
					predicates.add(cb.equal(expression, attributeValue));
					break;
				case CONTAINING:
					predicates.add(cb.like(expression, "%" + attributeValue + "%"));
					break;
				case STARTING:
					predicates.add(cb.like(expression, attributeValue + "%"));
					break;
				case ENDING:
					predicates.add(cb.like(expression, "%" + attributeValue));
					break;
				default:
					throw new IllegalArgumentException(
							"Unsupported StringMatcher " + exampleAccessor.getStringMatcherForPath(currentPath));
				}
			} else {
				predicates.add(cb.equal(from.get(attribute), attributeValue));
			}
		}

		return predicates;
	}

	private static Field getIdField(Object entity) {
		@SuppressWarnings("unchecked")
		Set<Field> fields = ReflectionUtils.getAllFields(entity.getClass(), ReflectionUtils.withAnnotation(Id.class));
		if (!fields.iterator().hasNext()) {
			return null;
		}
		return fields.iterator().next();
	}

	/**
	 * 检查查询实体属性上是否使用了{@link QueryOr} 注解 返回处理过后的matcher
	 * 
	 * @param path
	 * @param cb
	 * @param from
	 * @param matcher
	 * @param predicates
	 * @param orFields
	 * @return
	 */
	private static ExampleMatcher getPredicateFromAnnotatedFieldsForOr(String path, CriteriaBuilder cb, Path<?> from,
			Object entity, ExampleMatcher matcher, List<Predicate> predicates, Set<Field> orFields,
			Set<Field> withoutOrFields) {
		Set<Field> excludeFields = new HashSet<>();
		for (Field field : orFields) {
			if (!field.isAnnotationPresent(Transient.class)) {
				throw new RuntimeException("QueryOr annotation must be used with 'javax.persistence.Transient'");
			}
			if (!field.getType().toString().endsWith("String")) {
				throw new RuntimeException("QueryOr annotation must be used on 'java.lang.String'");
			}
			
			Object filterValue = getFieldValue(entity, field);
			if (StringUtils.isEmpty(filterValue)) {
				continue;
			}
			String filterStringValue = filterValue + "";
			QueryOr ann = AnnotationUtils.findAnnotation(field, QueryOr.class);
			String[] targets = ann.target();
			List<Predicate> orPredicates = new ArrayList<>();
			for (String targetName : targets) {
				if (!StringUtils.isEmpty(targetName)) {
					Field targetField = getFieldByName(entity, targetName);
					if (targetField != null) {
						excludeFields.add(targetField);
						matcher = matcher.withIgnorePaths(
								!StringUtils.hasText(path) ? targetField.getName() : path + "." + targetField.getName());
						QueryLike queryLike = AnnotationUtils.findAnnotation(targetField, QueryLike.class);
						if (queryLike != null) {

							switch (queryLike.matcher()) {

							case DEFAULT:
							case EXACT:
								orPredicates.add(cb.equal(cb.lower(from.get(targetField.getName())), filterStringValue));
								break;
							case CONTAINING:
								orPredicates.add(cb.like(cb.lower(from.get(targetField.getName())),
										"%" + filterStringValue.toLowerCase() + "%"));
								break;
							case STARTING:
								orPredicates.add(cb.like(cb.lower(from.get(targetField.getName())),
										filterStringValue.toLowerCase() + "%"));
								break;
							case ENDING:
								orPredicates.add(cb.like(cb.lower(from.get(targetField.getName())),
										"%" + filterStringValue.toLowerCase()));
								break;
							default:
								throw new IllegalArgumentException("Unsupported StringMatcher ");
							}
						} else {
							orPredicates.add(cb.like(cb.lower(from.get(targetField.getName())),
									"%" + filterStringValue.toLowerCase() + "%"));
						}
					}
				}
			}
			if (orPredicates.size() > 0) {
				Predicate[] orps = new Predicate[orPredicates.size()];
				predicates.add(cb.or(orPredicates.toArray(orps)));
			}
		}
		// 过滤已参与or条件的字段
		withoutOrFields.removeAll(excludeFields);
		return matcher;
	}

	private static Field getFieldByName(Object entity, String targetName) {
		Set<Field> fields = ReflectionUtils.getAllFields(entity.getClass(), ReflectionUtils.withName(targetName));
		if (!fields.iterator().hasNext()) {
			return null;
		}
		return fields.iterator().next();
	}

	private static Object getFieldValue(Object value, Field field) {
		field.setAccessible(true);
		Object val = null;
		try {
			val = field.get(value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		}
		return val;
	}

	private static ExampleMatcher getPredicateFromAnnotatedFieldsWithoutOr(String path, CriteriaBuilder cb,
			Path<?> from, Object value, ExampleMatcher matcher, List<Predicate> predicates,
			Set<Field> annotatedfields) {
		for (Field field : annotatedfields) {

			if (field != null && field.isAnnotationPresent(QueryLike.class)) {
				QueryLike ann = AnnotationUtils.findAnnotation(field, QueryLike.class);
				field.setAccessible(true); // 设置些属性是可以访问的
				Object val = getFieldValue(value, field);
				// 限制 like查询只能应用于 String类型的字段上
				if (!field.getType().toString().endsWith("String")) {
					throw new IllegalAnnotationError("Annotation \"@QueryLike\" was used on field \""+ field.getName() +"\" which is unsupported!");
				}
				if (!StringUtils.isEmpty(val)) {
					matcher = matcher.withIgnorePaths(
							!StringUtils.hasText(path) ? field.getName() : path + "." + field.getName());
					String fieldStringVal = val + "";
					switch (ann.matcher()) {

					case DEFAULT:
					case EXACT:
						predicates.add(cb.equal(cb.lower(from.get(field.getName())), fieldStringVal));
						break;
					case CONTAINING:
						predicates.add(
								cb.like(cb.lower(from.get(field.getName())), "%" + fieldStringVal.toLowerCase() + "%"));
						break;
					case STARTING:
						predicates
								.add(cb.like(cb.lower(from.get(field.getName())), fieldStringVal.toLowerCase() + "%"));
						break;
					case ENDING:
						predicates
								.add(cb.like(cb.lower(from.get(field.getName())), "%" + fieldStringVal.toLowerCase()));
						break;
					default:
						throw new IllegalArgumentException("Unsupported StringMatcher ");
					}
				}
				continue;
			}

			if (field != null && field.isAnnotationPresent(QueryGreaterEqual.class)) {
				QueryGreaterEqual ann = AnnotationUtils.findAnnotation(field, QueryGreaterEqual.class);
				String target = ann.target();
				Object targetValue = processComparable(value, field, target);
				if (!StringUtils.isEmpty(targetValue)) {
					Predicate predicate = cb.greaterThanOrEqualTo(from.get(field.getName()), (Comparable) targetValue);
					predicates.add(predicate);
					matcher = matcher
							.withIgnorePaths(!StringUtils.hasText(path) ? field.getName() : path + "." + field.getName());
				}
			}
			if (field != null && field.isAnnotationPresent(QueryGreaterThan.class)) {
				QueryGreaterThan ann = AnnotationUtils.findAnnotation(field, QueryGreaterThan.class);
				String target = ann.target();
				Object targetValue = processComparable(value, field, target);
				if (!StringUtils.isEmpty(targetValue)) {
					Predicate predicate = cb.greaterThan(from.get(field.getName()), (Comparable) targetValue);
					predicates.add(predicate);
					matcher = matcher
							.withIgnorePaths(!StringUtils.hasText(path) ? field.getName() : path + "." + field.getName());
				}
			}
			if (field != null && field.isAnnotationPresent(QueryLessEqual.class)) {
				QueryLessEqual ann = AnnotationUtils.findAnnotation(field, QueryLessEqual.class);
				String target = ann.target();
				Object targetValue = processComparable(value, field, target);
				if (!StringUtils.isEmpty(targetValue)) {
					Predicate predicate = cb.lessThanOrEqualTo(from.get(field.getName()), (Comparable) targetValue);
					predicates.add(predicate);
					matcher = matcher
							.withIgnorePaths(!StringUtils.hasText(path) ? field.getName() : path + "." + field.getName());
				}
			}
			if (field != null && field.isAnnotationPresent(QueryLessThan.class)) {
				QueryLessThan ann = AnnotationUtils.findAnnotation(field, QueryLessThan.class);
				String target = ann.target();
				Object targetValue = processComparable(value, field, target);
				if (StringUtils.isEmpty(targetValue)) {
					Predicate predicate = cb.lessThan(from.get(field.getName()), (Comparable) targetValue);
					predicates.add(predicate);
					matcher.withIgnorePaths(!StringUtils.hasText(path) ? field.getName() : path + "." + field.getName());
				}
			}

		}
		return matcher;
	}

	/**
	 * 获取比较目标的值，如果没有在注解中指定目标那么使用被注解的属性本身的值做比较
	 * 
	 * @param value
	 *            entity
	 * @param field
	 * @param target
	 *            属性名称
	 * @return
	 */
	private static Object processComparable(Object value, Field field, String target) {
		if (StringUtils.isEmpty(target)) {
			logger.info("未找到 " + field.getName() + " 需要比较的字段,跳过构造过滤条件");
			field.setAccessible(true);
			Object val = getFieldValue(value, field);
			return val;
		}

		Set<Field> fields = ReflectionUtils.getAllFields(value.getClass(), ReflectionUtils.withName(target));
		if (!fields.iterator().hasNext()) {
			return null;
		}
		Field targetField = fields.iterator().next();

		Object targetValue = null;
		try {
			targetField.setAccessible(true);
			targetValue = targetField.get(value);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return targetValue;
	}

	private static Set<Field> getWithoutOrFields(Class<?> probeType) {
		Set<Field> withoutOrFields = new HashSet<>();
		for (Class<? extends Annotation> annotation : LOFTER_ANNOTATIONS_WITHOUT_OR) {
			withoutOrFields.addAll(ReflectionUtils.getAllFields(probeType, ReflectionUtils.withAnnotation(annotation)));
		}
		return withoutOrFields;
	}

	private static Set<Field> getFieldsWithLofterOr(Class<?> probeType) {
		Set<Field> orFields = new HashSet<>();
		orFields.addAll(ReflectionUtils.getAllFields(probeType, ReflectionUtils.withAnnotation(QueryOr.class)));
		return orFields;
	}

	private static boolean isAssociation(Attribute<?, ?> attribute) {
		return ASSOCIATION_TYPES.contains(attribute.getPersistentAttributeType());
	}

	/**
	 * {@link PathNode} is used to dynamically grow a directed graph structure
	 * that allows to detect cycles within its direct predecessor nodes by
	 * comparing parent node values using
	 * {@link System#identityHashCode(Object)}.
	 *
	 * @author Christoph Strobl
	 */
	private static class PathNode {

		String name;
		PathNode parent;
		List<PathNode> siblings = new ArrayList<PathNode>();;
		Object value;

		public PathNode(String edge, PathNode parent, Object value) {

			this.name = edge;
			this.parent = parent;
			this.value = value;
		}

		PathNode add(String attribute, Object value) {

			PathNode node = new PathNode(attribute, this, value);
			siblings.add(node);
			return node;
		}

		boolean spansCycle() {

			if (value == null) {
				return false;
			}

			String identityHex = ObjectUtils.getIdentityHexString(value);
			PathNode tmp = parent;

			while (tmp != null) {

				if (ObjectUtils.getIdentityHexString(tmp.value).equals(identityHex)) {
					return true;
				}
				tmp = tmp.parent;
			}

			return false;
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder();
			if (parent != null) {
				sb.append(parent.toString());
				sb.append(" -");
				sb.append(name);
				sb.append("-> ");
			}

			sb.append("[{ ");
			sb.append(ObjectUtils.nullSafeToString(value));
			sb.append(" }]");
			return sb.toString();
		}
	}
}
