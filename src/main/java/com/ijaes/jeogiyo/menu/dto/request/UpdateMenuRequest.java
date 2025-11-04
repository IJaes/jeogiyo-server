package com.ijaes.jeogiyo.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메뉴 수정 요청")
public class UpdateMenuRequest {

	@Nullable
	@Schema(description = "메뉴명", example = "순대국밥")
	private String name;

	@Nullable
	@Schema(description = "메뉴 설명", example = "속이 꽉 찬 순대와 12000시간 이상 끓인 육수의 깊은 맛")
	private String description;

	@Nullable
	@PositiveOrZero(message = "가격은 0이상이어야 합니다")
	@Schema(description = "메뉴 가격", example = "12000")
	private Integer price;
}
