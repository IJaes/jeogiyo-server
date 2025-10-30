package com.ijaes.jeogiyo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 수정 요청")
public class UserUpdateRequest {

    @Schema(description = "주소", example = "서울시 강남구 역삼동")
    private String address;

    @Schema(description = "전화번호 (XXX-XXXX-XXXX)", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "현재 비밀번호 (비밀번호 변경 시 필수)", example = "OldPassword123!@")
    private String currentPassword;

    @Schema(description = "새 비밀번호 (비밀번호 변경 시 입력)", example = "NewPassword123!@")
    private String newPassword;
}
