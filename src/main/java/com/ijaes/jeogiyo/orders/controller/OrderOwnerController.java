package com.ijaes.jeogiyo.orders.controller;

import java.util.Set;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.orders.dto.response.OrderDetailResponse;
import com.ijaes.jeogiyo.orders.dto.response.OrderSummaryResponse;
import com.ijaes.jeogiyo.orders.entity.OrderStatus;
import com.ijaes.jeogiyo.orders.repository.OrderSearchCondition;
import com.ijaes.jeogiyo.orders.service.OrderQueryService;
import com.ijaes.jeogiyo.orders.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/owner/orders")
@RequiredArgsConstructor
@Tag(name = "주문", description = "가게 사장님 전용 주문 API")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('OWNER')")
public class OrderOwnerController {

	private final OrderService orderService;
	private final OrderQueryService orderQueryService;

	@Operation(summary = "주문 전체 조회", description = "본인 매장의 전체 주문을 조회합니다.")
	@GetMapping
	public ResponseEntity<Page<OrderSummaryResponse>> getStoreOrders(
		@Parameter(description = "가게 ID", required = true)
		@RequestParam UUID storeId,
		@ParameterObject
		@PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
		Pageable pageable,
		@Parameter(hidden = true) Authentication auth
	) {
		// 점주 스코프로 검색 (OrderQueryService가 소유 검증 수행)
		OrderSearchCondition cond = OrderSearchCondition.builder()
			.storeId(storeId)
			.build();
		return ResponseEntity.ok(orderQueryService.search(cond, pageable, auth));
	}

	@Operation(summary = "주문 상태별 조회", description = "가게의 특정 주문상태를 페이징 조회합니다.")
	@GetMapping("/status")
	public ResponseEntity<Page<OrderSummaryResponse>> getStoreOrdersByStatus(
		@Parameter(description = "가게 ID", required = true)
		@RequestParam UUID storeId,
		@Parameter(description = "주문 상태(단건)", required = true, example = "ACCEPTED")
		@RequestParam OrderStatus status,
		@ParameterObject
		@PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
		Pageable pageable,
		@Parameter(hidden = true) Authentication auth
	) {
		OrderSearchCondition cond = OrderSearchCondition.builder()
			.storeId(storeId)
			.statuses(Set.of(status)) // record 필드: Set<OrderStatus>
			.build();
		return ResponseEntity.ok(orderQueryService.search(cond, pageable, auth));
	}

	@Operation(summary = "주문 상세 조회", description = "한건의 주문을 상세 조회합니다.")
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDetailResponse> getDetail(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId,
		@Parameter(hidden = true) Authentication auth
	) {
		// 서비스가 requireReadable/requireOwner 등을 통해 권한 검증
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

	@Operation(summary = "주문 거절", description = "점주가 ACCEPTED상태의 주문을 거절합니다.")
	@PostMapping("/{orderId}/reject")
	public ResponseEntity<Void> rejectByOwner(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId,
		@Parameter(hidden = true) Authentication auth
	) {
		orderService.rejectByOwner(orderId, auth);
		return ResponseEntity.noContent().build();
	}
}
