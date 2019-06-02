package com.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;

/**
 * 〈JDBC连接数据库〉<br>
 * 〈〉
 *
 * @author PitterWang
 * @create 2019/5/30
 * @since 1.0.0
 */
@Configuration
@EnableTransactionManagement
@PropertySource(value = {"classpath:application.properties"})
public class BeanConfig {
	@Autowired
	private Environment env;

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setDriverClassName(env.getProperty("source.driverClassName").trim());
		dataSource.setUrl(env.getProperty("spring.datasource.url").trim());
		dataSource.setUsername(env.getProperty("source.username").trim());
		dataSource.setPassword(env.getProperty("source.password").trim());
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource());
		return jdbcTemplate;
	}
}