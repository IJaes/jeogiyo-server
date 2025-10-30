package com.ijaes.jeogiyo.store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.store.dto.CreateStoreRequest;
import com.ijaes.jeogiyo.store.dto.StoreResponse;
import com.ijaes.jeogiyo.store.service.StoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/stores")
@RequiredArgsConstructor
@Tag(name = "가게", description = "가게 관리 API")
public class StoreController {

	private final StoreService storeService;

	@PostMapping
	@Operation(summary = "가게 생성", description = "가게를 생성합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<StoreResponse> createStore(Authentication authentication, @RequestBody CreateStoreRequest request) {
		StoreResponse storeResponse = storeService.createStore(authentication, request);
		return ResponseEntity.ok(storeResponse);
	}
}
