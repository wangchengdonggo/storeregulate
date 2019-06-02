package com.example.storeregulate;

import com.common.utils.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@EnableAsync
@EnableCaching //启用缓存

@EnableTransactionManagement(proxyTargetClass = true)
public class StoreregulateApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(StoreregulateApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(StoreregulateApplication.class);
	}

	@Bean
	public SpringUtil springUtil() {
		return new SpringUtil();
	}
}
