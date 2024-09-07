package com.yesHealth.web.modules.user.service;

import com.yesHealth.web.modules.user.dto.RegistrationDto;
import com.yesHealth.web.modules.user.entity.UserEntity;

public interface UserService {
	void saveUser(RegistrationDto registrationDto);

	UserEntity findByEmail(String email);

	UserEntity findByUsername(String username);
}
