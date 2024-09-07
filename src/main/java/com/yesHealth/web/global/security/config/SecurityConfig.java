package com.yesHealth.web.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//	private final CustomUserDetailsService userDetailsService;
//
//	@Autowired
//	public SecurityConfig(CustomUserDetailsService userDetailsService) {
//		super();
//		this.userDetailsService = userDetailsService;
//	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(
						requests -> requests.antMatchers("/login", "/register", "/css/**", "/js/**").permitAll())
				.formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/").loginProcessingUrl("/login")
						.failureUrl("/login?error=true").permitAll())
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).permitAll());
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // 使用BCrypt加密
	}

}
