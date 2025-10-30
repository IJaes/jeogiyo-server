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
@Schema(description = "비밀번호 수정 요청")
public class UpdatePasswordRequest {

    @Schema(description = "현재 비밀번호", example = "OldPassword123!@", required = true)
    private String currentPassword;

    @Schema(description = "새 비밀번호 (대소문자, 숫자, 특수문자, 8~15자)", example = "NewPassword456!@", required = true)
    private String newPassword;
}
