package com.yesHealth.web.modules.production.domain.model;

public enum PlanStatus {
	NOT_IMPLEMENTED("0"), IMPLEMENTED("1");

	private String status;

	PlanStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
