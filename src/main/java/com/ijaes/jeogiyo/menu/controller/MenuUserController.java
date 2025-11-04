package com.ijaes.jeogiyo.menu.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.menu.dto.response.MenuUserResponse;
import com.ijaes.jeogiyo.menu.service.MenuUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/menus")
@RequiredArgsConstructor
public class MenuUserController {

	private final MenuUserService menuUserService;

	@GetMapping("/stores/{storeId}")
	@Operation(summary = "전체 메뉴 조회", description = "매장에 등록된 전체 메뉴를 조회합니다 (삭제된 메뉴 제외)", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<List<MenuUserResponse>> getAllMenus(
		@Parameter(description = "매장 ID")
		@PathVariable UUID storeId) {
		List<MenuUserResponse> menus = menuUserService.getMenusByStoreId(storeId);
		return ResponseEntity.ok(menus);
	}

	@GetMapping("/{menuId}")
	@Operation(summary = "특정 메뉴 조회", description = "특정 메뉴 정보를 조회합니다 (삭제된 메뉴 불가)", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<MenuUserResponse> getMenu(
		@Parameter(description = "메뉴 ID")
		@PathVariable UUID menuId) {
		MenuUserResponse menuUserResponse = menuUserService.getMenuByMenuId(menuId);
		return ResponseEntity.ok(menuUserResponse);
	}
}
