package com.ijaes.jeogiyo.menu.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/v1/owner/menus")
@RequiredArgsConstructor
@Tag(name = "사장님")
public class MenuOwnerController {

	private final MenuOwnerService menuOwnerService;

	@PostMapping
	@Operation(summary = "메뉴 등록", description = "메뉴를 등록합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<MenuResponse> createMenu(@Valid @RequestBody CreateMenuRequest request, Authentication authentication) {
		MenuResponse menuResponse = menuOwnerService.createMenu(request, authentication);
		return ResponseEntity.ok(menuResponse);
	}

	@GetMapping
	@Operation(summary = "메뉴 조회", description = "매장에 등록된 전체 메뉴를 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<List<MenuResponse>> getMenus(Authentication authentication) {
		List<MenuResponse> menus = menuOwnerService.getMyMenus(authentication);
		return ResponseEntity.ok(menus);
	}
}
