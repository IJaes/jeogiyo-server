package com.ijaes.jeogiyo.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "인증 응답")
public class AuthResponse {
    @Schema(description = "응답 메시지", example = "회원가입이 성공했습니다.!")
    private String message;

    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "사용자 역할", example = "ROLE_USER")
    private String role;
}
