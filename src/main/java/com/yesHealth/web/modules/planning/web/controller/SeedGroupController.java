package com.yesHealth.web.modules.planning.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/production")
public class SeedGroupController {
	@GetMapping("group-seed")
	public String getIndex() {
		return "seedingPage";
	}
}
