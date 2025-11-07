package com.ijaes.jeogiyo.store.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.service.StoreUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/stores")
@RequiredArgsConstructor
@Tag(name = "매장", description = "사용자가 매장과 메뉴를 조회하는 API")
public class StoreUserController {

	private final StoreUserService storeUserService;

	@GetMapping("")
	@Operation(summary = "전체 매장 조회", description = "모든 매장을 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Page<StoreResponse>> getAllStores(
		@Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
		@Parameter(description = "정렬 기준 (distance: 거리순, rate: 평점순)") @RequestParam(defaultValue = "distance") String sortBy,
		Authentication authentication
	) {
		Page<StoreResponse> stores = storeUserService.getAllStores(page, size, sortBy, authentication);
		return ResponseEntity.ok(stores);
	}

	@GetMapping("/{storeId}")
	@Operation(summary = "매장 상세 조회", description = "특정 매장의 상세 정보와 사장님 정보를 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<StoreDetailResponse> getStoreDetail(
		@Parameter(description = "매장 ID") @PathVariable UUID storeId) {
		StoreDetailResponse storeDetail = storeUserService.getStoreDetail(storeId);
		return ResponseEntity.ok(storeDetail);
	}
}
