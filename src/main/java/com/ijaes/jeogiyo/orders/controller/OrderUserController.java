package com.ijaes.jeogiyo.orders.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.orders.dto.request.OrderCreateRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderUserCancelRequest;
import com.ijaes.jeogiyo.orders.dto.response.OrderDetailResponse;
import com.ijaes.jeogiyo.orders.dto.response.OrderSummaryResponse;
import com.ijaes.jeogiyo.orders.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "주문", description = "일반 사용자 전용 API ")
public class OrderUserController {

	private final OrderService orderService;

	@Operation(summary = "주문 생성", description = "새 주문을 생성합니다.")
	@PostMapping
	public ResponseEntity<OrderDetailResponse> create(
		Authentication authentication,
		@Valid @RequestBody OrderCreateRequest req) {
		return ResponseEntity.ok(orderService.create(req, authentication));
	}

	@Operation(summary = "전체 주문 조회", description = "로그인 된 회원의 전체 주문 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<Page<OrderSummaryResponse>> getAllOrders(
		@ParameterObject
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
		Pageable pageable,
		@Parameter(hidden = true) Authentication auth
	) {
		return ResponseEntity.ok(orderService.getUserOrders(auth, pageable));
	}

	@Operation(summary = "주문 상세 조회", description = "주문 ID로 상세 정보를 조회합니다.")
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDetailResponse> getDetail(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId, Authentication auth) {
		return ResponseEntity.ok(orderService.getDetail(orderId, auth));
	}

	@Operation(summary = "주문 취소", description = "일반 회원이 ACCEPTED 상태의 주문을 5분이내에는 취소 가능합니다.")
	@PostMapping("/{orderId}/reject")
	public ResponseEntity<Void> reject(
		@Parameter(description = "주문ID", required = true)
		@PathVariable UUID orderId, Authentication auth, OrderUserCancelRequest event) {
		orderService.cancelByUser(orderId, auth, event.getPaymentKey(), event.getCancelReason(),
			event.getUserId());
		return ResponseEntity.ok().build();
	}

}
