package com.yesHealth.web.modules.production.domain.model;

public enum FileSrcType {
	FILE, MANUAL;

	@Override
	public String toString() {
		return name();
	}
}
