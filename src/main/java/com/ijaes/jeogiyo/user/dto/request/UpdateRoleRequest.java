package com.ijaes.jeogiyo.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 권한 변경 요청")
public class UpdateRoleRequest {

	@Schema(description = "변경할 권한", example = "BLOCK")
	private String role;
}
