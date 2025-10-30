package com.ijaes.jeogiyo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {
    @Schema(description = "아이디", example = "john123")
    private String username;

    @Schema(description = "비밀번호", example = "Password123!@")
    private String password;
}