package com.ijaes.jeogiyo.orders.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ijaes.jeogiyo.orders.entity.Order;
import com.ijaes.jeogiyo.orders.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "주문 목록 아이템 응답(요약)")
public class OrderSummaryResponse {

	@Schema(description = "주문 ID")
	private final UUID orderId;

	@Schema(description = "사용자 ID")
	private final UUID userId;

	@Schema(description = "가게 ID")
	private final UUID storeId;

	@Schema(description = "상태")
	private final OrderStatus status;

	@Schema(description = "총 금액")
	private final int totalPrice;

	@Schema(description = "생성 시각", type = "string", format = "date-time")
	private final LocalDateTime createdAt;

	public static OrderSummaryResponse from(Order order) {
		return OrderSummaryResponse.builder()
			.orderId(order.getId())
			.userId(order.getUserId())
			.storeId(order.getStoreId())
			.status(order.getOrderStatus())
			.totalPrice(order.getTotalPrice())
			.createdAt(order.getCreatedAt())
			.build();
	}
}
