package com.ijaes.jeogiyo.orders.entity;

import static com.ijaes.jeogiyo.common.exception.ErrorCode.*;
import static com.ijaes.jeogiyo.orders.entity.OrderStatus.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.ijaes.jeogiyo.common.exception.CustomException;

class OrderDomainTest {
	// 일반 사용자 취소 로직

	/** 주문이 생성되고 5분 이내에만 취소 가능하다. */
	private Order order;           // 각 테스트마다 새 인스턴스
	// 테스트의 재현성/결정성 확보를 위해 임의의 고정 시각(2030-01-01 12:00:00) 사용
	private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2030, 1, 1, 12, 0, 0);     // 기준 시각

	private static final String CREATED_AT = "createdAt";

	@BeforeEach
	void setUp() {
		order = Order.builder()
			.userId(UUID.randomUUID())
			.storeId(UUID.randomUUID())
			.totalPrice(27_000)
			.transactionId("pg-123")
			.build();
	}

	// 취소 성공에서 사용할 (현재 시각 - 설정 시각) 로직
	// ===== 헬퍼 =====
	private static void setCreatedAt(Object target, LocalDateTime createdAt) {
		ReflectionTestUtils.setField(target, CREATED_AT, createdAt);
	}

	private static void setCreatedAtAgo(Object target, long minutesAgo, long secondsAgo, LocalDateTime base) {
		setCreatedAt(target, base.minusMinutes(minutesAgo).minusSeconds(secondsAgo));
	}

	// ========== 취소(CANCEL) - 일반 사용자 ==========

	@Test
	void 취소_성공_정확히5분() {
		// now - 5분 0초 → 경계 OK (isBefore 사용 로직상 허용)
		setCreatedAtAgo(order, 5, 0, FIXED_NOW);

		order.cancelOrder(FIXED_NOW);

		assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
	}

	@Test
	void 취소_성공_5분이내() {
		// now - 4분 0초 → 성공
		setCreatedAtAgo(order, 4, 0, FIXED_NOW);

		order.cancelOrder(FIXED_NOW);

		assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
	}

	@Test
	void 취소_실패_정확히_5분_1초() {
		// now - 5분 1초 → 실패(301초 경과)
		setCreatedAtAgo(order, 5, 1, FIXED_NOW);

		assertThatThrownBy(() -> order.cancelOrder(FIXED_NOW))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ORDER_CANCEL_OVERTIME);

		// 예외가 발생한 이후에도 상태값이 그대로 ACCEPTED인지 확인
		assertThat(order.getOrderStatus()).isEqualTo(ACCEPTED);
	}

	// ========== 거절(REJECT/환불) - 사장님 ==========
	@Test
	void 거절_성공() {
		// 주문 대기(=ACCEPTED) 상태에서 사장님이 거절 성공하는 케이스
		assertThat(order.getOrderStatus()).isEqualTo(ACCEPTED);

		// when
		order.refundOrder(RejectReasonCode.CLOSED_EARLY);

		// then: 결제건 거절은 환불(REFUND)로 전이
		assertThat(order.getOrderStatus()).isEqualTo(REFUND);
	}

	// ========== 주문 상태 전이 ==========
	@Test
	void 점주_수락_후_조리_배달_완료_해피패스() {
		// given: 최초 상태는 ACCEPTED
		order.changeStatus(PAID);           // ACCEPTED -> PAID
		order.changeStatus(COOKING);        // PAID -> COOKING
		order.changeStatus(COOKED);         // COOKING -> COOKED
		order.changeStatus(DELIVERING);     // COOKED -> DELIVERING
		order.changeStatus(DELIVERED);      // DELIVERING -> DELIVERED
		order.changeStatus(COMPLETED);      // DELIVERED -> COMPLETED

		assertThat(order.getOrderStatus()).isEqualTo(COMPLETED);
	}

	@Test
	void 전이_가능성_체크() {
		// 규칙: ACCEPTED -> PAID/REFUND/CANCELED(도메인 메서드에서 최종 전이 허용)만 허용, 그 외 전이는 금지
		assertThat(ACCEPTED.canTransitTo(PAID)).isTrue();      // 결제 완료
		assertThat(ACCEPTED.canTransitTo(REFUND)).isTrue();    // 사장 거절(결제건) → 환불
		assertThat(ACCEPTED.canTransitTo(CANCELED)).isTrue();  // 사용자 취소(미결제 등) → 취소

		assertThat(ACCEPTED.canTransitTo(COOKING)).isFalse();
		assertThat(ACCEPTED.canTransitTo(COOKED)).isFalse();
		assertThat(ACCEPTED.canTransitTo(DELIVERING)).isFalse();
		assertThat(ACCEPTED.canTransitTo(COMPLETED)).isFalse();
	}
}
