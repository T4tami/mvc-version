package com.yesHealth.web.modules.util.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidDaysContent extends ValidContent {
	private String headOutDate;
	private String growingDate;
	private String matureDate;
	private String harvestDate;
}
