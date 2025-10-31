package com.ijaes.jeogiyo.store.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.store.dto.request.UpdateStoreRequest;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.service.StoreAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/admin/stores")
@RequiredArgsConstructor
@Tag(name = "관리자", description = "관리자 권한이 필요한 API")
public class StoreAdminController {

	private final StoreAdminService storeAdminService;

	@GetMapping("")
	@Operation(summary = "전체 매장 조회 (삭제된 매장 포함)", description = "MANAGER 역할의 관리자가 삭제된 매장을 포함한 모든 매장을 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Page<StoreResponse>> getAllStores(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "DESC") String direction) {

		Page<StoreResponse> response = storeAdminService.getAllStores(page, size, sortBy, direction);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{storeId}")
	@Operation(summary = "특정 매장 정보 수정", description = "MANAGER 역할의 관리자가 특정 매장 정보를 수정합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<StoreResponse> updateStore(
		@PathVariable UUID storeId,
		@Valid @RequestBody UpdateStoreRequest request) {
		StoreResponse response = storeAdminService.updateStore(storeId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{storeId}")
	@Operation(summary = "특정 매장 소프트 삭제", description = "MANAGER 역할의 관리자가 특정 매장을 소프트 삭제합니다 (실제 데이터는 유지)", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Void> deleteStore(@PathVariable UUID storeId) {
		storeAdminService.deleteStore(storeId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
