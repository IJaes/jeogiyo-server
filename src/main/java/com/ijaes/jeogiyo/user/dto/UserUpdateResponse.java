package com.ijaes.jeogiyo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 정보 수정 응답")
public class UserUpdateResponse {

    @Schema(description = "응답 메시지", example = "사용자 정보가 수정되었습니다.")
    private String message;

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "사용자 아이디", example = "john123")
    private String username;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "주소", example = "서울시 강남구 역삼동")
    private String address;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;
}
