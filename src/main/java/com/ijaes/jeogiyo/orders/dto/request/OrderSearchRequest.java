package com.ijaes.jeogiyo.orders.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ijaes.jeogiyo.orders.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@Schema(description = "주문 검색 요청(동적 조건)")
public class OrderSearchRequest {

	@Schema(description = "사용자 ID(일반 회원 필터)", example = "9b0db9d7-1d02-4d8e-9b51-2a0a0c1b2c3d")
	private final UUID userId;

	@Schema(description = "가게 ID(사장님 필터)", example = "3ea661d6-1513-4c94-9111-a676a1c3e903")
	private final UUID storeId;

	@Schema(
		description = "주문 상태",
		example = "COOKING",
		allowableValues = {"WAITING", "ACCEPTED", "REJECTED", "COOKING", "COOKED", "DELIVERING", "COMPLETED",
			"CANCELED"}
	)
	private final OrderStatus status;

	@Schema(description = "조회 시작 시각(이상)", type = "string", format = "date-time", example = "2025-11-01T00:00:00")
	private final LocalDateTime from;

	@Schema(description = "조회 종료 시각(미만)", type = "string", format = "date-time", example = "2025-11-06T00:00:00")
	private final LocalDateTime to;

	@Schema(description = "정렬 필드(createdAt,totalPrice,status 중 하나)", example = "createdAt")
	private final String sortBy;

	@Schema(description = "내림차순 여부", example = "true", defaultValue = "true")
	private final Boolean desc;
}
