package com.yesHealth.web.modules.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.yesHealth.web.modules.user.dto.RegistrationDto;
import com.yesHealth.web.modules.user.entity.Menu;
import com.yesHealth.web.modules.user.entity.UserEntity;
import com.yesHealth.web.modules.user.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class AuthController {
	private UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String loginPage(HttpSession session) {
		session.setAttribute("options", new ArrayList<>(Arrays.asList(5, 10, 20, 50, 100, 200, 500, 1000)));
		return "login/login";
	}

	@GetMapping("/index")
	public String index(HttpSession session) {
		List<Menu> menus = userService.getUserMenus();
		session.setAttribute("menus", menus);
		return "index/index";
	}

	@GetMapping("/register")
	public String getRegisterForm(Model model) {
		RegistrationDto user = new RegistrationDto();
		model.addAttribute("user", user);
		return "login/register";
	}

	@PostMapping("/register/save")
	public String register(@Valid @ModelAttribute("user") RegistrationDto user, BindingResult result, Model model) {
		UserEntity existingUserEmail = userService.findByEmail(user.getEmail());
		if (existingUserEmail != null && existingUserEmail.getEmail() != null
				&& !existingUserEmail.getEmail().isEmpty()) {
			return "redirect:/register?fail";
		}
		UserEntity existingUserUsername = userService.findByUsername(user.getUsername());
		if (existingUserUsername != null && existingUserUsername.getUsername() != null
				&& !existingUserUsername.getUsername().isEmpty()) {
			return "redirect:/register?fail";
		}
		if (result.hasErrors()) {
			model.addAttribute("user", user);
			return "register";
		}
		userService.saveUser(user);
		return "redirect:/module/products/product?success";
	}
}