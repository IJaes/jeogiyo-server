package com.ijaes.jeogiyo.orders.entity;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
	WAITING,      // 주문 대기 (사용자 주문 요청)
	ACCEPTED,     // 주문 수락 (사장님)
	REJECTED,     // 주문 거절 (사장님)
	COOKING,      // 조리 중 (사장님)
	COOKED,       // 조리 완료 (사장님)
	DELIVERING,   // 배달 중 (사장님)
	COMPLETED,    // 주문 완료 (사장님)
	CANCELED;     // 취소된 주문 (사장님)

	// Waiting -> COOKING 과 같이 바로 넘어가는걸 불가능하게 함
	private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_NEXT = Map.of(
		WAITING, Set.of(ACCEPTED, REJECTED, CANCELED),
		ACCEPTED, Set.of(COOKING),
		COOKING, Set.of(COOKED),
		COOKED, Set.of(DELIVERING),
		DELIVERING, Set.of(COMPLETED),
		REJECTED, Set.of(),       // 종결 상태
		COMPLETED, Set.of(),       // 종결 상태
		CANCELED, Set.of()        // 종결 상태
	);

	// 전이 가능 여부를 검사
	protected boolean canTransitTo(OrderStatus target) {
		return ALLOWED_NEXT                    // 전이 허용 표(Map<OrderStatus, Set<OrderStatus>>)
			.getOrDefault(this, Set.of())  // 현재 상태(this)에 대한 허용 목록을 꺼내고, 없으면 빈 집합
			.contains(target);                    // 그 목록에 target이 있으면 전이 허용(true)
	}

	// 전이 차단(주문거절 또는 주문완료 일 경우엔 전이를 차단한다)
	protected boolean isTerminal() {
		return this == REJECTED || this == COMPLETED;
	}

}