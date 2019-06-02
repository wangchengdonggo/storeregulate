package com.storeregularte.system.entity;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.Audited;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author PitterWang
 * @create 2019/6/1
 * @since 1.0.0
 */
@MappedSuperclass
@EntityListeners(RepairableEntityListener.class)
@Audited
public abstract class Repairable extends Auditable{
	private static final long serialVersionUID = 7125062208353024592L;

	@ColumnDefault("0")
	private Boolean isDeleted;

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}