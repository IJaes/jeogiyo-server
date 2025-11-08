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
	// 주문 취소 - 환불이 필요 없는 경우
	public void cancelOrder(LocalDateTime now) {
		// 5분이 지나기 전에만 취소가 가능하게 설정
		if (this.getCreatedAt().plusMinutes(5).isBefore(now))
			throw new CustomException(ORDER_CANCEL_OVERTIME);
		//
		if (!this.orderStatus.isBeforeCooking()) {
			throw new CustomException(ORDER_NOT_ACCEPTED);
		}
		this.orderStatus = CANCELED;
	}

	// 주문 취소 - 환불이 필요한 경우
	public void refundOrder(RejectReasonCode reasonCode) {
		// 주문 상태가 ACCEPTED 아닌 경우
		if (!this.orderStatus.isBeforeCooking()) {
			throw new CustomException(ORDER_NOT_ACCEPTED);
		}
		this.rejectReasonCode = reasonCode;
		this.rejectedDate = LocalDateTime.now();
		this.orderStatus = REFUND;
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
	// 전이 차단(주문거절 또는 주문완료 일 경우엔 전이를 차단한다)
	public boolean isTerminal() {
		return orderStatus.isTerminal();
	}

	// 결제 성공 시 주문 상태 업데이트
	public void updateOrderStatus(String paymentKey) {
		if (orderStatus.equals(ACCEPTED)) {
			this.orderStatus = PAID;
			this.transactionId = paymentKey;
		}
		throw new CustomException(ORDER_NOT_ACCEPTED);
	}
}

