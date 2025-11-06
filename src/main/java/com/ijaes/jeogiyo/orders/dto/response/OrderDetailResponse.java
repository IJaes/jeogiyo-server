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
@Schema(description = "주문 상세 응답")
public class OrderDetailResponse {

	@Schema(description = "주문 ID")
	private final UUID orderId;

	@Schema(description = "사용자 ID")
	private final UUID userId;

	@Schema(description = "가게 ID")
	private final UUID storeId;

	@Schema(description = "상태")
	private final OrderStatus status;

	@Schema(description = "총 결제금액(원)")
	private final int totalPrice;

	@Schema(description = "toss 카드 ID")
	private final String transactionId;

	@Schema(description = "생성 시각", type = "string", format = "date-time")
	private final LocalDateTime createdAt;

	@Schema(description = "수정 시각", type = "string", format = "date-time")
	private final LocalDateTime updatedAt;

	@Schema(description = "삭제 여부(soft delete)")
	private final boolean deleted;

	public static OrderDetailResponse from(Order order) {
		return OrderDetailResponse.builder()
			.orderId(order.getId())
			.userId(order.getUserId())
			.storeId(order.getStoreId())
			.status(order.getOrderStatus())
			.totalPrice(order.getTotalPrice())
			.transactionId(order.getTransactionId())
			.createdAt(order.getCreatedAt())
			.updatedAt(order.getUpdatedAt())
			.deleted(order.isDeleted())
			.build();
	}
}
