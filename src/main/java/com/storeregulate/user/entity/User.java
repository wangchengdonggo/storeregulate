package com.storeregulate.user.entity;


import com.storeregulate.system.entity.Repairable;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br>
 * 〈用户〉
 *
 * @author PitterWang
 * @create 2019/6/1
 * @since 1.0.0
 */
@Entity
@Table(name="sys_user")
public class User extends Repairable implements Serializable {

	private static final long serialVersionUID = 2255422530199803322L;

	/**
	 * 性别枚举类
	 */
	public enum Sex {
		/**
		 * 男
		 */
		MALE,
		/**
		 * 女
		 */
		FAMALE
	}
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String username;

	private String firstname;

    private String password;

    private Sex sex;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}
}