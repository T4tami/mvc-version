package com.yesHealth.web.global.security.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yesHealth.web.global.security.model.CustomUserDetails;
import com.yesHealth.web.modules.user.entity.UserEntity;
import com.yesHealth.web.modules.user.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity user = userRepository.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}
		List<GrantedAuthority> authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName())) // 假設Role有getName方法
				.collect(Collectors.toList());

		return CustomUserDetails.builder().username(user.getUsername()).name(user.getName()).email(user.getEmail())
				.imgName(user.getImgName()).password(user.getPassword()).authorities(authorities)
				.isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).isEnabled(true)
				.build();
	}

}
