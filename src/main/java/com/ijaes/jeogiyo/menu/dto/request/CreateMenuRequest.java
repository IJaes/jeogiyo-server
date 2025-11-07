package com.ijaes.jeogiyo.menu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메뉴 등록")
public class CreateMenuRequest {

	@NotBlank(message = "메뉴명은 필수입니다.")
	@Schema(description = "메뉴명", example = "순대국밥")
	private String name;

	@Nullable
	@Schema(description = "메뉴 설명", example = "속이 꽌 찬 순대와 1200시간 이상 끓인 육수로 최고의 건강과 맛을 선사합니다.")
	private String description;

	@NotNull(message = "가격은 필수입니다.")
	@PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
	@Schema(description = "메뉴 가격", example = "12000")
	private Integer price;

	@Builder.Default
	@Schema(description = "메뉴 설명 AI 생성 여부 (기본값: false)", example = "false")
	private boolean aiDescription = false;
}
