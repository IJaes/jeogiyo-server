package com.ijaes.jeogiyo.orders.entity;

import static com.ijaes.jeogiyo.common.exception.ErrorCode.*;
import static com.ijaes.jeogiyo.orders.entity.OrderStatus.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import com.ijaes.jeogiyo.common.entity.BaseEntity;
import com.ijaes.jeogiyo.common.exception.CustomException;

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
	// 1) 결제 전 취소(환불 불필요) — 5분 제한 + 조리 전만
	public void cancelOrder(LocalDateTime now) {
		if (this.getCreatedAt().plusMinutes(5).isBefore(now)) {
			throw new CustomException(ORDER_CANCEL_OVERTIME);
		}
		if (!this.orderStatus.isBeforeCooking()) {
			throw new CustomException(ORDER_NOT_ACCEPTED);
		}
		this.orderStatus = CANCELED;
	}

	// 2) 환불 "요청" (최종 REFUND 확정은 결제 이벤트 수신 시)
	public void requestRefund(RejectReasonCode reasonCode) {
		if (isTerminal())
			throw new CustomException(ORDER_INVALID_TRANSITION);
		if (!this.orderStatus.canTransitTo(REFUND_PENDING)) {
			throw new CustomException(ORDER_REFUND_INVALID_STATE);
		}
		this.rejectReasonCode = reasonCode;
		this.rejectedDate = LocalDateTime.now();
		this.orderStatus = REFUND_PENDING; // ★ 중간 상태로만 전이
	}

	// 3) 환불 "완료" (결제/PG에서 REFUNDED 이벤트 수신 시에만)
	public void markRefundCompleted() {
		if (this.orderStatus != REFUND_PENDING) {
			throw new CustomException(ORDER_REFUND_INVALID_STATE);
		}
		this.orderStatus = REFUND;
	}

	// 4) 결제 승인(결제 이벤트 수신 시만 호출)
	public void markPaid(String paymentKey) {
		// 정책: ACCEPTED → PAID (이미 ACCEPTED가 초기상태)
		if (this.orderStatus != ACCEPTED) {
			throw new CustomException(ORDER_NOT_ACCEPTED);
		}
		this.transactionId = paymentKey;
		this.orderStatus = PAID;
	}

	// 5) 결제 실패(결제 이벤트 수신 시만 호출)
	public void cancelByPaymentFailure() {
		// 결제 실패는 조리 전(ACCEPTED/PAID)에서만 취소 가능
		if (!this.orderStatus.isBeforeCooking()) {
			throw new CustomException(ORDER_NOT_ACCEPTED);
		}
		this.orderStatus = CANCELED;
	}

	// 강제 전이 차단(서비스에서 임의 상태 변경 방지)
	public void changeStatus(OrderStatus target) {
		if (isTerminal()) {
			throw new CustomException(ORDER_INVALID_TRANSITION);
		}
		if (!orderStatus.canTransitTo(target)) {
			throw new CustomException(ORDER_INVALID_TRANSITION);
		}
		this.orderStatus = target;
	}

	/** === 공통 가드 === */
	public boolean isTerminal() {
		return orderStatus.isTerminal();
	}

}

