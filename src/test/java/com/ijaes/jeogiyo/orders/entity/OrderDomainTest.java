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

	// ========== 취소(CANCEL) ==========

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

		// 테스트 직전에 createAt = new - 5분 1초로 세팅
		// now 시점에 cancelByUser(now)를 실행하면 예외 발생
		// 벌생한 예외가 CustomException인지 확인
		// 발생한 예외 타입이 ORDER_CANCEL_WINDOW_EXPIRED와 같은지 검증
		assertThatThrownBy(() -> order.cancelOrder(FIXED_NOW))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode") // getter를 우선적으로 값을 가져오고 없으면 필드 반사(reflection) 접근한다.
			.isEqualTo(ORDER_CANCEL_OVERTIME);

		// 메세지 확인용
		Throwable t = catchThrowable(() -> order.cancelOrder(FIXED_NOW));
		System.out.println(t.getMessage());

		// 예외가 발생한 이후에도 상태값이 그대로 WAITING인지 확인
		assertThat(order.getOrderStatus()).isEqualTo(ACCEPTED);
	}

	@Test
	void 취소_실패_대기상태_아님_COOKING() {
		// given: 5분 이내이지만 상태는 COOKING(조리 중)
		setCreatedAtAgo(order, 4, 0, FIXED_NOW);
		ReflectionTestUtils.setField(order, "orderStatus", COOKING);

		// when & then: 대기(ACCEPTED)가 아니므로 취소 불가 → 상태오류 발생
		assertThatThrownBy(() -> order.cancelOrder(FIXED_NOW))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ORDER_NOT_ACCEPTED); // 규칙에 맞춘 에러 코드

		// 상태 불변 확인: COOKING 그대로
		assertThat(order.getOrderStatus()).isEqualTo(COOKING);
	}

	// ========== 주문 상태 전이 ==========
	@Test
	void 점주_수락_후_조리_배달_완료_해피패스() {
		// 아무런 문제나 오류 없이 ACCEPTED -> COMPLETE
		// given: 최초 상태는 ACCEPTED
		order.changeStatus(PAID);        // ACCEPTED -> PAID
		order.changeStatus(COOKING);        // PAID -> COOKING
		order.changeStatus(COOKED);            // COOKING -> COOKED
		order.changeStatus(DELIVERING);        // COOKED -> DELIVERING
		order.changeStatus(DELIVERED);        // DELIVERING -> DELIVERED
		order.changeStatus(COMPLETED);        // DELIVERED -> COMPLETED

		// when & then: COMPLETED로 전이 되었는지 체크
		assertThat(order.getOrderStatus()).isEqualTo(COMPLETED);
	}

	@Test
	void 전이_가능성_체크() {
		// 규칙: WAITING -> ACCEPTED만 허용, 그 외 전이는 금지
		assertThat(ACCEPTED.canTransitTo(PAID)).isTrue(); // 결제 완료
		assertThat(ACCEPTED.canTransitTo(CANCELED)).isTrue(); // 거절

		assertThat(ACCEPTED.canTransitTo(COOKING)).isFalse();
		assertThat(ACCEPTED.canTransitTo(COOKED)).isFalse();
		assertThat(ACCEPTED.canTransitTo(DELIVERING)).isFalse();
		assertThat(ACCEPTED.canTransitTo(COMPLETED)).isFalse();
	}
}
