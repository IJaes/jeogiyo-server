package com.ijaes.jeogiyo.payments.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ijaes.jeogiyo.payments.entity.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter

@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 요청")
public class PaymentDTO {
	private UUID paymentId;

	private UUID orderId;

	private Long card;

	private PaymentStatus status;

	private LocalDateTime approvedAt;

	private String log;

	private String paymentMethod;

	private int paymentAmount;

	private String bank;

}
