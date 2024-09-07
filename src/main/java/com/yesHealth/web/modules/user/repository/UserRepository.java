package com.yesHealth.web.modules.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yesHealth.web.modules.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
	UserEntity findByEmail(String email);

	UserEntity findByUsername(String userName);
}
