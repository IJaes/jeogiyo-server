package com.ijaes.jeogiyo.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

    @Schema(description = "새 비밀번호 (대소문자, 숫자, 특수문자, 8~15자)", example = "NewPassword456!@", required = true)
    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[a-zA-Z\\d@$!%*?&]{8,15}$",
             message = "비밀번호는 대소문자, 숫자, 특수문자를 포함한 8~15자여야 합니다")
    private String newPassword;
}
