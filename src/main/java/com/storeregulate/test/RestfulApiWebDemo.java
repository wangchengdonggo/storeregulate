package com.storeregulate.test;

import com.storeregulate.common.utils.RedisUtil;
import com.storeregulate.user.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author PitterWang
 * @create 2019/5/31
 * @since 1.0.0
 */
@RestController
public class RestfulApiWebDemo {
	@RequestMapping("/redis-test")
	@Cacheable(value = "redis-test")
	public User home() {
		//User us = new User(1L,"admin","admin");
		User us = null;
		RedisUtil.setValueToCache("test", "user", 1 * 60);
		//System.out.println("若下面没出现“无缓存的时候调用”字样且能打印出数据表示测试成功");
		return us;
	}

}