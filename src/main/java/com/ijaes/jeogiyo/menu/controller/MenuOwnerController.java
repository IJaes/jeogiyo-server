package com.ijaes.jeogiyo.menu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuResponse;
import com.ijaes.jeogiyo.menu.service.MenuOwnerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/owner")
@RequiredArgsConstructor
public class MenuOwnerController {

	private final MenuOwnerService menuOwnerService;

	@PostMapping("/menus")
	public MenuResponse createMenu(@RequestBody CreateMenuRequest request, Authentication authentication) {
		return menuOwnerService.createMenu(request, authentication);
	}
}
