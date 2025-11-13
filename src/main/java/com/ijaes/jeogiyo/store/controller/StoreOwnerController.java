package com.ijaes.jeogiyo.store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.store.dto.request.CreateStoreRequest;
import com.ijaes.jeogiyo.store.dto.request.UpdateStoreRequest;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.service.StoreOwnerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/owner/stores")
@RequiredArgsConstructor
@Tag(name = "사장님", description = "매장 관리 API")
public class StoreOwnerController {

	private final StoreOwnerService storeService;

	@PostMapping
	@Operation(summary = "매장 생성", description = "매장을 생성합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<StoreResponse> createStore(
		Authentication authentication,
		@RequestBody CreateStoreRequest request
	) {
		StoreResponse storeResponse = storeService.createStore(authentication, request);
		return ResponseEntity.ok(storeResponse);
	}

	@GetMapping
	@Operation(summary = "매장 조회", description = "본인의 매장을 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<StoreResponse> myStore(
		Authentication authentication
	) {
		StoreResponse storeResponse = storeService.myStore(authentication);
		return ResponseEntity.ok(storeResponse);
	}

	@PatchMapping
	@Operation(summary = "매장 정보 수정", description = "본인의 매장 정보를 부분적으로 수정합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<StoreResponse> updateStore(
		Authentication authentication,
		@RequestBody UpdateStoreRequest request) {
		StoreResponse storeResponse = storeService.updateStore(authentication, request);
		return ResponseEntity.ok(storeResponse);
	}
}
