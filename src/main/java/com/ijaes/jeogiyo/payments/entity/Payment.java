package com.ijaes.jeogiyo.payments.entity;

import java.time.LocalDateTime;
import java.util.UUID;

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

	@Column
	private Long card;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column
	private LocalDateTime approvedAt;

	@Column
	private String log;

	@Column
	private String paymentMethod;

	@Column(nullable = false)
	private int paymentAmount;

	@Column
	private String bank;

	@Column(nullable = false)
	private String paymentKey;

	public void updatePaymentApprove(LocalDateTime approvedAt, String bank, String method) {
		this.status = PaymentStatus.SUCCESS;
		this.approvedAt = approvedAt;
		this.bank = bank;
		this.paymentMethod = method;
	}

	public void updatePaymentFail(String bank, String method, String log) {
		this.bank = bank;
		this.paymentMethod = method;
		this.status = PaymentStatus.FAIL;
		this.log = log;
	}

	public void updateLog(String log) {
		this.log = log;
	}

}

