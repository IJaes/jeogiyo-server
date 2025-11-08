package com.ijaes.jeogiyo.orders.entity;

import static com.ijaes.jeogiyo.common.exception.ErrorCode.*;
import static com.ijaes.jeogiyo.orders.entity.OrderStatus.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import com.ijaes.jeogiyo.common.entity.BaseEntity;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "j_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "store_id")
	private UUID storeId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private OrderStatus orderStatus = ACCEPTED;

	// 거절 사유 저장
	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private RejectReasonCode rejectReasonCode;

	@CreatedDate
	private LocalDateTime rejectedDate; // 거절 시간

	@Column(name = "total_price")
	private int totalPrice;

	@Column(name = "transaction_id")
	private String transactionId;

	public void delete() {
		this.setDeletedAt(LocalDateTime.now());
	}

	@Builder
	private Order(UUID userId, UUID storeId, int totalPrice, String transactionId) {
		this.userId = Objects.requireNonNull(userId);
		this.storeId = Objects.requireNonNull(storeId);
		// totalPrice가 0원 미만일 경우(음수)
		if (totalPrice < 0)
			throw new CustomException(ORDER_TOTAL_PRICE_INVALID);
		this.totalPrice = totalPrice;
		this.transactionId = transactionId;
	}

	/** == 도메인 규칙 == */
	// 일반 사용자가 취소하는 로직
	public void cancelByUser(LocalDateTime now) {
		// ACCEPTED 상태가 아닌데 변경하려고 하는 경우
		requireACCEPTED(ORDER_NOT_ACCEPTED);
		// 5분이 지나기 전에만 취소가 가능하게 설정
		if (this.getCreatedAt().plusMinutes(5).isBefore(now))
			throw new CustomException(ORDER_CANCEL_OVERTIME);

		this.orderStatus = CANCELED;
	}

	// 사장님이 주문 거절을 원할 때(REJECTED)
	public void rejectByOwner(RejectReasonCode reasonCode) {
		// 주문 상태가 ACCEPTED 아닌 경우
		requireACCEPTED(ORDER_NOT_ACCEPTED);
		this.rejectReasonCode = reasonCode;
		this.rejectedDate = LocalDateTime.now();
		this.orderStatus = REJECTED;
	}

	// 강제로 주문 상태가 넘어가는 부분 막기
	public void changeStatus(OrderStatus target) {
		// 강제 전이 차단
		if (isTerminal()) {
			throw new CustomException(ORDER_INVALID_TRANSITION);
		}
		// orderStatus에 있는 검증로직
		if (!orderStatus.canTransitTo(target))
			throw new CustomException(ORDER_INVALID_TRANSITION);
		this.orderStatus = target;
	}

	/** === 공통 가드(헬퍼) === */
	// 주문 상태가 WAITING(주문 대기) 인지 검사
	private void requireACCEPTED(ErrorCode code) {
		requireStatus(ACCEPTED, code);
	}

	// 현재 주문 상태와 예상한값(excepted)가 다를 경우 - 확장성 있게 설계하기 위해 requireWaiting 메서드와 분리.
	private void requireStatus(OrderStatus expected, ErrorCode code) {
		if (this.orderStatus != expected) {
			throw new CustomException(code); // ErrorCode의 message 사용
		}
	}

	// 결제 성공 시 주문 상태 업데이트
	public void updateOrderStatus(String paymentKey) {
		this.orderStatus = PAID;
		this.transactionId = paymentKey;
	}

	// 전이 차단(주문거절 또는 주문완료 일 경우엔 전이를 차단한다)
	public boolean isTerminal() {
		return orderStatus.isTerminal();
	}
}

