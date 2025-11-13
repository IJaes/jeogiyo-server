package com.ijaes.jeogiyo.orders.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.orders.dto.request.OrderOwnerCancelRequest;
import com.ijaes.jeogiyo.orders.dto.response.OrderDetailResponse;
import com.ijaes.jeogiyo.orders.dto.response.OrderSummaryResponse;
import com.ijaes.jeogiyo.orders.entity.OrderStatus;
import com.ijaes.jeogiyo.orders.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/owner/orders")
@RequiredArgsConstructor
@Tag(name = "사장님", description = "가게 사장님 전용 주문 API")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('OWNER')")
public class OrderOwnerController {

	private final OrderService orderService;

	@Operation(summary = "가게 단위 주문 목록", description = "본인 매장의 주문을 상세 응답으로 페이징 조회합니다.")
	@GetMapping("/stores/{storeId}")
	public ResponseEntity<Page<OrderSummaryResponse>> getStoreOrdersDetail(
		@Parameter(description = "가게 ID", required = true)
		@PathVariable UUID storeId,
		@Parameter(hidden = true) Pageable pageable, // ✅ Swagger에 sort 안 노출
		@Parameter(hidden = true) Authentication auth
	) {
		return ResponseEntity.ok(orderService.getStoreOrders(storeId, auth, pageable));
	}

	@Operation(summary = "주문 상태별 조회", description = "가게의 주문상태를 페이징 조회합니다.")
	@GetMapping("/status")
	public ResponseEntity<Page<OrderSummaryResponse>> getStoreOrdersByStatus(
		@Parameter(description = "가게 ID", required = true)
		@RequestParam UUID storeId,
		@Parameter(description = "주문 상태(단건)", required = true, example = "ACCEPTED")
		@RequestParam OrderStatus status,
		@Parameter(hidden = true) Pageable pageable,
		@Parameter(hidden = true) Authentication auth
	) {
		Page<OrderSummaryResponse> result = orderService.getStoreOrdersByStatus(storeId, auth, status, pageable);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "주문 상세 조회", description = "한건의 주문을 상세 조회합니다.")
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDetailResponse> getDetail(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId,
		@Parameter(hidden = true) Authentication auth
	) {
		return ResponseEntity.ok(orderService.getDetail(orderId, auth));
	}

	@Operation(summary = "주문 상태 변경", description = "주문 상태를 변경합니다. 예: ACCEPTED → COOKING")
	@PostMapping("/{orderId}/status")
	public ResponseEntity<Void> changeStatus(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId,
		@Parameter(description = "다음 주문 상태", required = true, example = "COOKING")
		@RequestParam("status") OrderStatus nextStatus,
		@Parameter(hidden = true) Authentication auth
	) {
		orderService.changeStatusByOwner(orderId, nextStatus, auth);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "주문 거절", description = "점주가 주문을 거절합니다.")
	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<Void> cancelByOwner(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId,
		@Parameter(hidden = true) Authentication auth, OrderOwnerCancelRequest event
	) {
		orderService.cancel(orderId, auth);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "주문 거절(환불)", description = "점주가 주문을 거절할 때 환불 해야할 경우.")
	@PostMapping("/{orderId}/refund")
	public ResponseEntity<Void> refundByOwner(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId,
		@Parameter(hidden = true) Authentication auth
	) {
		orderService.refund(orderId, auth);
		return ResponseEntity.noContent().build();
	}
}
