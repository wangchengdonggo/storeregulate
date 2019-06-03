package com.storeregulate.system.service;

import org.springframework.data.jpa.domain.Specification;

public interface DataRuleWapper {

	<T> Specification<T> getDataRuleSpecification(Class<T> entityClass);
}
