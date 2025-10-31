package com.ijaes.jeogiyo.user.controller;

import com.ijaes.jeogiyo.user.dto.request.UpdateAddressRequest;
import com.ijaes.jeogiyo.user.dto.request.UpdatePasswordRequest;
import com.ijaes.jeogiyo.user.dto.request.UpdatePhoneNumberRequest;
import com.ijaes.jeogiyo.user.dto.response.SimpleUpdateResponse;
import com.ijaes.jeogiyo.user.dto.response.UserInfoResponse;
import com.ijaes.jeogiyo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "사용자 정보 수정 등 사용자 관련 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {
        UserInfoResponse response = userService.getUserInfo(authentication);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/address")
    @Operation(summary = "주소 수정", description = "사용자의 주소를 수정합니다", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<SimpleUpdateResponse> updateAddress(Authentication authentication, @RequestBody UpdateAddressRequest request) {
        SimpleUpdateResponse response = userService.updateAddress(authentication, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/phone")
    @Operation(summary = "전화번호 수정", description = "사용자의 전화번호를 수정합니다", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<SimpleUpdateResponse> updatePhoneNumber(Authentication authentication, @RequestBody UpdatePhoneNumberRequest request) {
        SimpleUpdateResponse response = userService.updatePhoneNumber(authentication, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 수정", description = "사용자의 비밀번호를 수정합니다", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<SimpleUpdateResponse> updatePassword(Authentication authentication, @RequestBody UpdatePasswordRequest request) {
        SimpleUpdateResponse response = userService.updatePassword(authentication, request);
        return ResponseEntity.ok(response);
    }
}
