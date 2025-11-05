package com.ijaes.jeogiyo.orders.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.orders.dto.request.OrderCreateRequest;
import com.ijaes.jeogiyo.orders.dto.response.OrderDetailResponse;
import com.ijaes.jeogiyo.orders.entity.OrderStatus;
import com.ijaes.jeogiyo.orders.repository.OrderSearchCondition;
import com.ijaes.jeogiyo.orders.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/api/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "주문", description = "주문 관련 API")
@SecurityRequirement(name = "bearer-jwt") // 이 컨트롤러의 모든 엔드포인트에 인증 요구
public class OrderController {

	private final OrderService orderService;

	@Operation(summary = "주문 생성",
		description = "새 주문을 생성합니다. 인증된 사용자 ID가 주문자(User)로 설정됩니다.")
	@PostMapping
	public ResponseEntity<OrderDetailResponse> create(
		@Valid @RequestBody OrderCreateRequest req, Authentication auth) {
		return ResponseEntity.ok(orderService.create(req, auth));
	}

	@Operation(summary = "주문 상세 조회",
		description = "주문 ID로 상세 정보를 조회합니다. 본인/해당 매장 점주/관리자만 접근 가능합니다.")
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDetailResponse> getDetail(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId, Authentication auth) {
		return ResponseEntity.ok(orderService.getDetail(orderId, auth));
	}

	@Operation(summary = "주문 목록 검색",
		description = "검색 조건(회원/가게/상태/기간 등)과 페이징/정렬을 적용하여 주문 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<Page<OrderDetailResponse>> search(
		@Parameter(description = "주문 검색 조건(쿼리스트링 바인딩)") OrderSearchCondition condition,
		@Parameter(description = "페이징/정렬 파라미터 (page, size, sort)") Pageable pageable) {
		return ResponseEntity.ok(orderService.search(condition, pageable));
	}

	@Operation(
		summary = "사용자 취소",
		description = "주문자 본인이 WAITING 상태의 주문을 생성 후 5분 이내에 취소합니다."
	)
	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<Void> cancelByUser(@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId,
		Authentication auth) {
		orderService.cancelByUser(orderId, auth);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "점주 거절",
		description = "해당 매장의 점주가 WAITING 상태의 주문을 거절합니다. (사유 코드는 서비스에서 처리)")
	@PostMapping("/{orderId}/reject")
	public ResponseEntity<Void> rejectByOwner(@Parameter(description = "주문 ID", required = true)
	@PathVariable UUID orderId, Authentication auth) {
		orderService.rejectByOwner(orderId, auth);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "점주 상태 변경",
		description = "점주가 주문 상태를 변경합니다. 예: ACCEPTED → COOKING → COOKED → DELIVERING → COMPLETED")
	@PostMapping("/{orderId}/status")
	public ResponseEntity<Void> changeStatus(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId,
		@Parameter(description = "다음 주문 상태", required = true, example = "COOKING")
		@RequestParam("status") OrderStatus nextStatus, Authentication auth) {
		orderService.changeStatusByOwner(orderId, nextStatus, auth);
		return ResponseEntity.noContent().build();
	}

	@Operation(
		summary = "주문 삭제(소프트)",
		description = "주문을 소프트 삭제합니다. 본인/해당 매장 점주만 가능하며, 기본 조회에서 제외됩니다.")
	@DeleteMapping("/{orderId}")
	public ResponseEntity<Void> softDelete(
		@Parameter(description = "주문 ID", required = true)
		@PathVariable UUID orderId, Authentication auth) {
		orderService.softDelete(orderId, auth);
		return ResponseEntity.noContent().build();
	}
}
