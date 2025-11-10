package com.ijaes.jeogiyo.payments.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import com.ijaes.jeogiyo.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "j_payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Payment extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID paymentId;

	@Column(nullable = false)
	private UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@CreatedDate
	@Column
	private LocalDateTime approvedAt;

	@Column
	private String log;

	@Column
	private int paymentAmount;

	@Column(name = "payment_key", length = 191)
	private String paymentKey;

	@Column(nullable = false)
	private String billingKey;

	@Enumerated(EnumType.STRING)
	@Column
	private CancelReason cancelReason;

	@Column
	private int retryCount = 0;

	public void updateApprovePaymentFail(String log, String paymentKey) {
		this.status = PaymentStatus.FAIL;
		this.log = log;
		this.paymentKey = paymentKey;
	}

	public void updateCancelPaymentFail(String log) {
		this.status = PaymentStatus.FAIL;
		this.log = log;
	}

	public void updateLog(String log) {
		this.log = log;
	}

	public void updateUserPaymentCancel() {
		this.cancelReason = CancelReason.USERCANCEL;
		this.status = PaymentStatus.CANCEL;
	}

	public void updateOwnerPaymentCancel() {
		this.cancelReason = CancelReason.STORECANCEL;
		this.status = PaymentStatus.CANCEL;
	}

	public void updatePaymentSuccess(String paymentKey) {
		this.status = PaymentStatus.SUCCESS;
		this.approvedAt = LocalDateTime.now();
		this.paymentKey = paymentKey;
	}

	public void createOrderInfo(UUID orderId, String billingKey, int amount) {
		this.orderId = orderId;
		this.billingKey = billingKey;
		this.paymentAmount = amount;
		this.status = PaymentStatus.REQUESTED;
	}

	public void increaseRetryCount() {
		this.retryCount++;
	}
}

