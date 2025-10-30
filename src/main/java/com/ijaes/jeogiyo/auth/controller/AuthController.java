package com.ijaes.jeogiyo.auth.controller;

import com.ijaes.jeogiyo.auth.dto.AuthResponse;
import com.ijaes.jeogiyo.auth.dto.LoginRequest;
import com.ijaes.jeogiyo.auth.dto.SignUpRequest;
import com.ijaes.jeogiyo.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인 등 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest request) {
        AuthResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인합니다")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃합니다")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String bearerToken) {
        AuthResponse response = authService.logout(bearerToken);
        return ResponseEntity.ok(response);
    }
}

