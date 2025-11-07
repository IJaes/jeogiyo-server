package com.ijaes.jeogiyo.payments.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.orders.service.OrderService;
import com.ijaes.jeogiyo.payments.service.PaymentUserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Configuration
@EnableAsync
@Tag(name = "사용자 결제", description = "사용자 결제 승인, 취소 등 사용자 결제 관련 API")
public class PaymentController {

	private final PaymentUserService paymentUserService;
	private final OrderService orderService;

	// @PostMapping("/cancel")
	// @Operation(summary = "결제 취소 요청", description = "사용자가 결제 취소 요청을 합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	// public ResponseEntity<?> cancelPayments(OrderUserCancelRequest event) {
	// 	orderService.orderCancel(event.getOrderId(), event.getPaymentKey(), event.getCanCelReason(),
	// 		event.getUserId());
	// 	return ResponseEntity.ok("결제취소");
	// }

}
