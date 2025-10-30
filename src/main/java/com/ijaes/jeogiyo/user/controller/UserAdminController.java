package com.ijaes.jeogiyo.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.user.dto.UserInfoResponse;
import com.ijaes.jeogiyo.user.service.UserAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Tag(name = "관리자", description = "관리자 권한이 필요한 API")
public class UserAdminController {

	private final UserAdminService userAdminService;

	@GetMapping("/users")
	@Operation(summary = "전체 사용자 조회", description = "관리자 권한으로 모든 사용자를 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<List<UserInfoResponse>> getAllUsers() {
		List<UserInfoResponse> userInfoResponses = userAdminService.getAllUsers();
		return ResponseEntity.ok(userInfoResponses);
	}

	@GetMapping("/users/{userId}")
	@Operation(summary = "특정 사용자 조회", description = "관리자 권한으로 특정 사용자의 정보를 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<UserInfoResponse> getUserById(@PathVariable UUID userId) {
		UserInfoResponse userInfoResponse = userAdminService.getUserById(userId);
		return ResponseEntity.ok(userInfoResponse);
	}
}
