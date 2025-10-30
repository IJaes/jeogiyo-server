package com.ijaes.jeogiyo.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
public class SignUpRequest {
    @Schema(description = "아이디 (소문자, 숫자, 4~10자)", example = "john123")
    private String username;

    @Schema(description = "비밀번호 (대소문자, 숫자, 특수문자, 8~15자)", example = "Password123!@")
    private String password;

    @Schema(description = "이름 (3~10자)", example = "홍길동")
    private String name;

    @Schema(description = "주소", example = "서울시 강남구")
    private String address;

    @Schema(description = "전화번호 (XXX-XXXX-XXXX)", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "사장님 여부", example = "false")
    private boolean owner;
}
