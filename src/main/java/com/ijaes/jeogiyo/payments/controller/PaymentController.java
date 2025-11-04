package com.ijaes.jeogiyo.payments.controller;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.orders.dto.request.OrderEvent;
import com.ijaes.jeogiyo.orders.service.OrderService;
import com.ijaes.jeogiyo.payments.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Configuration
@EnableAsync
@Tag(name = "사용자 결제", description = "사용자 결제 승인, 취소 등 사용자 결제 관련 API")
public class PaymentController {

	private final PaymentService paymentService;
	private final OrderService orderService;

	@PostMapping("")
	@Operation(summary = "결제 요청", description = "주문완료 건에 대해 결제 요청을 합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<?> getPayments(Authentication authentication, OrderEvent event) {
		orderService.orderProcess(event.getOrderId(), event.getAmount());
		return ResponseEntity.ok("payments");
	}

	@GetMapping("/resp/success")
	public ResponseEntity<String> paymentSuccess(
		@RequestParam String paymentKey,
		@RequestParam UUID orderId,
		@RequestParam int amount) {
		try {
			// Toss 서버에 결제 승인 요청
			paymentService.confirmPayment(paymentKey, orderId, amount);

			return ResponseEntity.ok("결제 성공 처리 완료");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("결제 승인 실패");
		}
	}

}
