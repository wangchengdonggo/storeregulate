package com.storeregularte.system.entity;

import com.common.json.UserJsonSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.storeregularte.user.entity.User;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 标记对象为可审计的
 * 
 * 创建和更新时，JPA会自动注入修改人和修改时间等审计信息
 * 
 * @author Tony
 *
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler","fieldHandler"})
public abstract class Auditable implements Serializable {
	private static final long serialVersionUID = -6832522029134793266L;
	public interface BaseView{};
	@CreatedBy
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "created_by", updatable=false, foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
	@JsonSerialize(using = UserJsonSerializer.class)
	@JsonView(BaseView.class)
	protected User createdBy;
	@CreatedDate
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Column(updatable=false)
	@JsonView(BaseView.class)
	protected Date createdDate;
	@LastModifiedBy
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "last_modified_by", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
	@JsonSerialize(using = UserJsonSerializer.class)
	@JsonView(BaseView.class)
	protected User lastModifiedBy;
	@LastModifiedDate
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@JsonView(BaseView.class)
	protected Date lastModifiedDate;

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public User getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(User lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
