package com.yesHealth.web.modules.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yesHealth.web.modules.user.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Role findByRoleName(String name);
}
