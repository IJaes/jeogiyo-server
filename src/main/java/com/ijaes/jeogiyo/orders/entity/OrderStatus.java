package com.ijaes.jeogiyo.orders.entity;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
	ACCEPTED,
	PAID,
	COOKING,
	COOKED,
	DELIVERING,
	DELIVERED,
	COMPLETED,
	CANCELED,  // 최종(미결제 취소)
	REFUND_PENDING, // ★ 추가: 환불 진행 중(비동기 대기)
	REFUND;    // 최종(결제 취소-환불)

	// 정상 진행 전이 (취소/환불 특례는 도메인 메서드에서 직접 처리)
	private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_NEXT = Map.of(
		ACCEPTED, Set.of(PAID, CANCELED, REFUND_PENDING),
		PAID, Set.of(COOKING, CANCELED, REFUND_PENDING),
		COOKING, Set.of(COOKED),
		COOKED, Set.of(DELIVERING),
		DELIVERING, Set.of(DELIVERED),
		DELIVERED, Set.of(COMPLETED),
		COMPLETED, Set.of(),
		CANCELED, Set.of(),
		REFUND_PENDING, Set.of(REFUND), // ★ 추가: 대기 → 완료
		REFUND, Set.of()
	);

	// 다음 단계로 넘어갈 수 있는지 체크
	public boolean canTransitTo(OrderStatus target) {
		return ALLOWED_NEXT.getOrDefault(this, Set.of()).contains(target);
	}

	// 사용자, 사장님 취소 가능: COOKING 이전 단계에서만 가능(ACCEPTED 또는 PAID만 가능)
	public boolean isBeforeCooking() {
		return this == ACCEPTED || this == PAID;
	}

	// 주문완료, 취소, 환불에서는 다른 상태로 변경 불가.
	public boolean isTerminal() {
		return this == COMPLETED || this == CANCELED || this == REFUND;
	}
}

