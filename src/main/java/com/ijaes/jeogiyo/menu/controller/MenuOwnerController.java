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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/owner")
@RequiredArgsConstructor
@Tag(name = "사장님")
public class MenuOwnerController {

	private final MenuOwnerService menuOwnerService;

	@PostMapping("/menus")
	@Operation(summary = "메뉴 등록", description = "메뉴를 등록합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public MenuResponse createMenu(@Valid @RequestBody CreateMenuRequest request, Authentication authentication) {
		return menuOwnerService.createMenu(request, authentication);
	}
}
