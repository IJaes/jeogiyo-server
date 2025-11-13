package com.ijaes.jeogiyo.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 5, max = 200, message = "주소는 5-200자여야 합니다")
    private String address;

    @Schema(description = "전화번호 (010-1234-5678 형식)", example = "010-1234-5678")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
    private String phoneNumber;

    @Schema(description = "현재 비밀번호 (비밀번호 변경 시 필수)", example = "OldPassword123!@")
    private String currentPassword;

    @Schema(description = "새 비밀번호 (비밀번호 변경 시 입력)", example = "NewPassword123!@")
    private String newPassword;
}
