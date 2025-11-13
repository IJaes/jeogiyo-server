package com.ijaes.jeogiyo.user.dto.response;

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
}
