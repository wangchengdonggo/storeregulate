package com.storeregularte.system.query.mybatis.interceptor;

import com.storeregularte.system.exceptions.BusinessException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis拦截器，用于处理排序参数，将驼峰形式转为下划线形式
 * @author jiawei
 *
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
		RowBounds.class, ResultHandler.class }) })
public class SortParamTranslatorInterceptor implements Interceptor {
	static int MAPPED_STATEMENT_INDEX = 0;
	static int PARAMETER_INDEX = 1;
	static int ROWBOUNDS_INDEX = 2;
	static int RESULT_HANDLER_INDEX = 3;
	private static Pattern safeParameterPattern = Pattern.compile("[A-Za-z]+[A-Za-z0-9,_]*");
	static ExecutorService pool;
	String dialectClass;
	boolean asyncTotalCount = false;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		//final Executor executor = (Executor) invocation.getTarget();
        try {
        Object[] queryArgs = invocation.getArgs();

		Object prefParameter = queryArgs[PARAMETER_INDEX];
		if (!(prefParameter instanceof HashMap)) {
			return invocation.proceed();
		}

		HashMap<?, ?> params = (HashMap<?, ?>) prefParameter;
		for(Entry param : params.entrySet()){
			if(param.getValue() != null && param.getValue() instanceof Pageable){
				Pageable pageable = (Pageable) param.getValue();
				List<Order> orders = new ArrayList<Order>();
				Sort sort = pageable.getSort();
				
				if(pageable.getSort() != null){
					for(Order order: sort){
						if(!safeParameterPattern.matcher(order.getProperty()).matches()){
							throw new BusinessException("非法的请求参数");
						}
						orders.add(new Order(order.getDirection(), camelToUnderline(order.getProperty())));
					};
				}
				Pageable pageRequest = null;
				if(orders != null && orders.size() > 0){
					 pageRequest = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(orders));
				}else{
					 pageRequest = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());
				}
				param.setValue(pageRequest);
			}
		}

		return invocation.proceed();
        }catch (Exception e){
		    e.printStackTrace();
            return null;
        }
	}

	private static Pattern humpPattern = Pattern.compile("[A-Z]");  
	private String camelToUnderline(CharSequence str) {
            Matcher matcher = humpPattern.matcher(str);  
            StringBuffer sb = new StringBuffer();  
            while(matcher.find()){  
                matcher.appendReplacement(sb, "_"+matcher.group(0).toLowerCase());  
            }  
            matcher.appendTail(sb);  
            return sb.toString();  
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}
}