package com.common.utils;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author PitterWang
 * @create 2019/6/1
 * @since 1.0.0
 */
public class RedisUtil {
	private static StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) SpringUtil
			.getBean("stringRedisTemplate");

	/**
	 * 存储Value
	 *
	 * @param key
	 *            cache对象key
	 * @param value
	 *            被存储的值
	 * @param timeOut
	 *            超时时间(秒)
	 */
	public static void setValueToCache(String key, String value, long timeOut) {
		stringRedisTemplate.opsForValue().set(key, value, timeOut, TimeUnit.SECONDS);
	}

	/**
	 * 根据key获取值
	 *
	 * @param key
	 *            cache对象key
	 */
	public static String getValueByCache(String key) {
		return (String) stringRedisTemplate.opsForValue().get(key);
	}

	/**
	 * 存储Map
	 *
	 * @param key
	 *            cache对象key
	 * @param map
	 *            存储对象
	 * @param timeOut
	 *            超时时间(秒)
	 */
	public static void setMapToCache(String key, Map<String, Object> map, long timeOut) {
		stringRedisTemplate.opsForHash().putAll(key, map);
		stringRedisTemplate.expire(key, timeOut, TimeUnit.SECONDS);
	}

	/**
	 * 根据key获取Map
	 *
	 * @param key
	 *            cache对象key
	 */
	public static Map<String, Object> getMapByCache(String key) {
		BoundHashOperations<String, String, Object> boundHashOperations = stringRedisTemplate.boundHashOps(key);
		System.out.println("key 超时时间:=" + stringRedisTemplate.getExpire(key));
		return boundHashOperations.entries();
	}

	/**
	 * 向key对应的map中添加缓存对象
	 *
	 * @param key
	 *            cache对象key
	 * @param field
	 *            map对应的key
	 * @param value
	 *            值
	 */
	public static void addKeyAndValueToCacheMap(String key, String field, String value) {
		stringRedisTemplate.opsForHash().put(key, field, value);
	}

}