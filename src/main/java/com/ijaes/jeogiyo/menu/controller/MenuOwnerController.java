package com.ijaes.jeogiyo.menu.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.request.UpdateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuDetailResponse;
import com.ijaes.jeogiyo.menu.service.MenuOwnerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
	public ResponseEntity<MenuDetailResponse> createMenu(
		@Valid @RequestBody CreateMenuRequest request,
		Authentication authentication) {
		MenuDetailResponse menuDetailResponse = menuOwnerService.createMenu(request, authentication);
		return ResponseEntity.ok(menuDetailResponse);
	}

	@GetMapping
	@Operation(summary = "본인 매장의 전체 메뉴 조회", description = "본인 매장에 등록된 전체 메뉴를 조회합니다 (삭제된 메뉴 포함)", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<List<MenuDetailResponse>> getMenus(
		Authentication authentication) {
		List<MenuDetailResponse> menus = menuOwnerService.getMyMenus(authentication);
		return ResponseEntity.ok(menus);
	}

	@GetMapping("/{menuId}")
	@Operation(summary = "특정 메뉴 조회", description = "매장에 등록된 특정 메뉴를 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<MenuDetailResponse> getMenu(
		@Parameter(description = "메뉴 ID")
		@PathVariable UUID menuId, Authentication authentication) {
		MenuDetailResponse menuDetailResponse = menuOwnerService.getMyMenu(menuId, authentication);
		return ResponseEntity.ok(menuDetailResponse);
	}

	@PatchMapping("/{menuId}")
	@Operation(summary = "메뉴 수정", description = "메뉴 정보를 수정합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<MenuDetailResponse> updateMenu(
		@Parameter(description = "메뉴 ID")
		@PathVariable UUID menuId,
		@Valid @RequestBody UpdateMenuRequest request,
		Authentication authentication) {
		MenuDetailResponse menuDetailResponse = menuOwnerService.updateMenu(menuId, request, authentication);
		return ResponseEntity.ok(menuDetailResponse);
	}

	@DeleteMapping("/{menuId}")
	@Operation(summary = "메뉴 삭제", description = "메뉴를 삭제합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Void> deleteMenu(
		@Parameter(description = "메뉴 ID")
		@PathVariable UUID menuId,
		Authentication authentication) {
		menuOwnerService.deleteMenu(menuId, authentication);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
