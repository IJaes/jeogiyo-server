package com.ijaes.jeogiyo.store.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@PatchMapping("/{storeId}")
	@Operation(summary = "특정 매장 정보 수정", description = "MANAGER 역할의 관리자가 특정 매장 정보를 수정합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<StoreResponse> updateStore(
		@PathVariable UUID storeId,
		@Valid @RequestBody UpdateStoreRequest request) {
		StoreResponse response = storeAdminService.updateStore(storeId, request);
		return ResponseEntity.ok(response);
	}
}
