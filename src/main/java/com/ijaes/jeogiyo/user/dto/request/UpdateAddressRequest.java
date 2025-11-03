package com.ijaes.jeogiyo.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주소 수정 요청")
public class UpdateAddressRequest {

    @Schema(description = "새로운 주소", example = "서울시 강남구 역삼동", required = true)
    @NotBlank(message = "주소는 필수입니다")
    @Size(min = 5, max = 200, message = "주소는 5-200자여야 합니다")
    private String address;
}
