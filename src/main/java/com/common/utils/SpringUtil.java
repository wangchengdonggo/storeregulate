package com.common.utils;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author PitterWang
 * @create 2019/6/1
 * @since 1.0.0
 */

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring 上下文对象获取工具
 *
 *
 */
@Component
public class SpringUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if(SpringUtil.applicationContext == null){
			SpringUtil.applicationContext = applicationContext;
		}
	}

	/**
	 * 获取applicationContext
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * 通过name获取 Bean.
	 * @param name Bean的名称
	 * @return Bean对象或null
	 */
	public static Object getBean(String name){
		return getApplicationContext().getBean(name);
	}

	/**
	 * 通过class获取Bean.
	 * @param clazz Class对象
	 * @return Bean对象或null
	 */
	public static <T> T getBean(Class<T> clazz){
		return getApplicationContext().getBean(clazz);

	}

	/**
	 * 通过name,以及Clazz返回指定的Bean
	 * @param name Bean的名称
	 * @param clazz Class对象
	 * @return Bean对象或null
	 */
	public static <T> T getBean(String name,Class<T> clazz){
		return getApplicationContext().getBean(name, clazz);
	}
	/**
	 * 返回所有实现某一接口的bean
	 * @param clazz Class对象
	 * @return Map
	 */
	public static <T> Map<String, T> getBeansOfType(Class<T> clazz){
		return getApplicationContext().getBeansOfType(clazz);
	}
}
