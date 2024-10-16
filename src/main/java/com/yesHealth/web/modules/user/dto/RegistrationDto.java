package com.yesHealth.web.modules.user.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class RegistrationDto {
	private Long id;
	@NotEmpty
	private String username;
	@NotEmpty
	private String email;
	@NotEmpty
	private String password;
}
