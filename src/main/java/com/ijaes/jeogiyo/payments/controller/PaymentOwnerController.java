package com.ijaes.jeogiyo.payments.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.orders.dto.request.OrderOwnerCancelRequest;
import com.ijaes.jeogiyo.orders.service.OrderService;
import com.ijaes.jeogiyo.payments.service.PaymentUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Configuration
@EnableAsync
@Tag(name = "가게 결제 취소", description = "가게에서 주문 거절로 인한 결제 취소 관련 API")
public class PaymentOwnerController {

	private final PaymentUserService paymentUserService;
	private final OrderService orderService;

	@PostMapping("/cancel-owner")
	@Operation(summary = "결제 취소 요청", description = "가게에서 결제 취소 요청을 합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<?> cancelOwnerPayments(OrderOwnerCancelRequest event) {
		orderService.orderOwnerCancel(event.getOrderId(), event.getPaymentKey(), event.getCanCelReason(),
			event.getUsername());
		return ResponseEntity.ok("결제취소");
	}

}
