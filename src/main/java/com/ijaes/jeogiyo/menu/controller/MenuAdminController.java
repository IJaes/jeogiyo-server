package com.ijaes.jeogiyo.menu.controller;

import java.util.UUID;

import javax.crypto.spec.DESedeKeySpec;

import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuDetailResponse;
import com.ijaes.jeogiyo.menu.service.MenuAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/admin/menus")
@RequiredArgsConstructor
@Tag(name = "관리자", description = "관리자 권한이 필요한 API")
public class MenuAdminController {

	private final MenuAdminService menuAdminService;

	@PostMapping("/{storeId}")
	@Operation(summary = "메뉴 등록", description = "관리자 권한으로 메뉴를 등록합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<MenuDetailResponse> createMenu(
		@Parameter(description = "가게 ID")
		@PathVariable UUID storeId,
		@Valid @RequestBody CreateMenuRequest request
	) {
		MenuDetailResponse menuDetailResponse = menuAdminService.createMenu(storeId, request);
		return ResponseEntity.ok(menuDetailResponse);
	}

	@GetMapping("")
	@Operation(summary = "전체 메뉴 조회 (삭제된 메뉴 포함)", description = "관리자 권한으로 모든 메뉴를 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Page<MenuDetailResponse>> getAllMenus(
		@Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
		@Parameter(description = "정렬 기준 (예: name, rate)") @RequestParam(defaultValue = "rate") String sortBy,
		@Parameter(description = "정렬 방향 (ASC 또는 DESC)") @RequestParam(defaultValue = "DESC") String direction
	) {
		Page<MenuDetailResponse> menus = menuAdminService.getAllMenus(page, size, sortBy, direction);
		return ResponseEntity.ok(menus);
	}

	@GetMapping("/{menuId}")
	@Operation(summary = "메뉴 상세 조회 (삭제된 메뉴도 가능)", description = "관리자 권한으로 메뉴 상세 정보를 조회합니다", security = @SecurityRequirement(name = "bearer_jwt"))
	public ResponseEntity<MenuDetailResponse> getMenu(
		@Parameter(description = "메뉴 ID")
		@PathVariable UUID menuId
	) {
		MenuDetailResponse menuDetailResponse = menuAdminService.getMenu(menuId);
		return ResponseEntity.ok(menuDetailResponse);
	}

	@DeleteMapping("/{menuId}")
	@Operation(summary = "메뉴 삭제 처리", description = "관리자 권한으로 메뉴를 삭제 처리 합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Void> deleteMenu(
		@Parameter(description = "메뉴 ID")
		@PathVariable UUID menuId
	) {
		menuAdminService.deleteMenu(menuId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
