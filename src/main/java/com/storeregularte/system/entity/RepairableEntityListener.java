package com.storeregularte.system.entity;

import javax.persistence.PrePersist;

public class RepairableEntityListener {

	@PrePersist
	public void setDefaultDeletedValue(Object target) {
		if (target instanceof Repairable) {
			Repairable entity = (Repairable) target;
			if (null == entity.getIsDeleted()) {
				entity.setIsDeleted(false);
			}
		}
	}
}
