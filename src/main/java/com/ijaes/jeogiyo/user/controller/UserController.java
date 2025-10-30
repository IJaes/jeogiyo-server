package com.ijaes.jeogiyo.user.controller;

import com.ijaes.jeogiyo.user.dto.*;
import com.ijaes.jeogiyo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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

    @PatchMapping("/address")
    @Operation(summary = "주소 수정", description = "사용자의 주소를 수정합니다")
    public ResponseEntity<UserUpdateResponse> updateAddress(
            @RequestBody UpdateAddressRequest request,
            Authentication authentication) {

        UserUpdateResponse response = userService.updateAddress(authentication, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/phone")
    @Operation(summary = "전화번호 수정", description = "사용자의 전화번호를 수정합니다")
    public ResponseEntity<UserUpdateResponse> updatePhoneNumber(
            @RequestBody UpdatePhoneNumberRequest request,
            Authentication authentication) {

        UserUpdateResponse response = userService.updatePhoneNumber(authentication, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 수정", description = "사용자의 비밀번호를 수정합니다")
    public ResponseEntity<UserUpdateResponse> updatePassword(
            @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {

        UserUpdateResponse response = userService.updatePassword(authentication, request);
        return ResponseEntity.ok(response);
    }
}
