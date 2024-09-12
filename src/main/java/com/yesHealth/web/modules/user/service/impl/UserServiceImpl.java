package com.yesHealth.web.modules.user.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yesHealth.web.modules.user.dto.RegistrationDto;
import com.yesHealth.web.modules.user.entity.Menu;
import com.yesHealth.web.modules.user.entity.Role;
import com.yesHealth.web.modules.user.entity.UserEntity;
import com.yesHealth.web.modules.user.repository.RoleRepository;
import com.yesHealth.web.modules.user.repository.UserRepository;
import com.yesHealth.web.modules.user.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private PasswordEncoder passwordEncoder;

	@Autowired
	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void saveUser(RegistrationDto registrationDto) {
		UserEntity user = new UserEntity();
		user.setUsername(registrationDto.getUsername());
		user.setEmail(registrationDto.getEmail());
		user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
		user.setCreatedBy(Long.valueOf("0"));
		user.setUpdatedBy(Long.valueOf("0"));
		user.setCreatedTime(LocalDateTime.now());
		user.setUpdatedTime(LocalDateTime.now());

		Role role = roleRepository.findByRoleName("USER");
		user.setRoles(Arrays.asList(role));
		userRepository.save(user);
	}

	@Override
	public UserEntity findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public UserEntity findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public List<Menu> getUserMenus() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			UserEntity user = userRepository.findByUsername(userDetails.getUsername());
			List<Role> roles = user.getRoles();
			Set<Menu> menus = new HashSet<>();
			for (Role role : roles) {
				menus.addAll(role.getMenu());
			}
			return new ArrayList<>(menus);
		}
		return Collections.emptyList();
	}
}
