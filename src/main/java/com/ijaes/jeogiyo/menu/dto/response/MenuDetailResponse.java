package com.ijaes.jeogiyo.menu.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메뉴 정보 상세 응답")
public class MenuDetailResponse {

	@Schema(description = "메뉴 ID")
	private UUID id;

	@Schema(description = "가게 ID")
	private UUID storeId;

	@Schema(description = "메뉴 이름", example = "순대국밥")
	private String name;

	@Schema(description = "메뉴 설명", example = "속이 꽌 찬 순대와 1200시간 이상 끓인 육수로 최고의 건강과 맛을 선사합니다.")
	private String description;

	@Schema(description = "메뉴 가격", example = "12000")
	private Integer price;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
}

