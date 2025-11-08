package com.ijaes.jeogiyo.payments.dto.response;

import java.util.UUID;

import com.ijaes.jeogiyo.payments.entity.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter

@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 응답")
public class PaymentApproveResponse {
	private UUID orderId;
	private PaymentStatus status;
	private String paymentKey;
	
}