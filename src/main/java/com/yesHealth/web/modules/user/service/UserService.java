package com.yesHealth.web.modules.user.service;

import java.util.List;

import com.yesHealth.web.modules.user.dto.RegistrationDto;
import com.yesHealth.web.modules.user.entity.Menu;
import com.yesHealth.web.modules.user.entity.UserEntity;

public interface UserService {
	void saveUser(RegistrationDto registrationDto);

	UserEntity findByEmail(String email);

	UserEntity findByUsername(String username);

	public List<Menu> getUserMenus();
}
