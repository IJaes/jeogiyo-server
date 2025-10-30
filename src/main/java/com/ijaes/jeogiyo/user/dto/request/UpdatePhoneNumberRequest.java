package com.ijaes.jeogiyo.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전화번호 수정 요청")
public class UpdatePhoneNumberRequest {

    @Schema(description = "새로운 전화번호 (XXX-XXXX-XXXX)", example = "010-5678-9012", required = true)
    private String phoneNumber;
}
