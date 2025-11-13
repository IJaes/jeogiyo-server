package com.ijaes.jeogiyo.orders.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.orders.dto.response.OrderDetailResponse;
import com.ijaes.jeogiyo.orders.dto.response.OrderSummaryResponse;
import com.ijaes.jeogiyo.orders.repository.OrderSearchCondition;
import com.ijaes.jeogiyo.orders.service.OrderQueryService;
import com.ijaes.jeogiyo.orders.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/admin/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "관리자", description = "관리자 권한이 필요한 API")
@SecurityRequirement(name = "bearer-jwt") // 이 컨트롤러의 모든 엔드포인트에 인증 요구
public class OrderAdminController {

	private final OrderService orderService;
	private final OrderQueryService orderQueryService;

	@Operation(
		summary = "주문 검색(관리자)",
		description = "관리자가 조건(회원/가게/상태/기간/금액 등)과 페이징/정렬로 주문을 검색합니다."
	)
	@GetMapping("/search")
	public ResponseEntity<Page<OrderSummaryResponse>> search(
		@ParameterObject OrderSearchCondition condition,
		@Parameter(hidden = true) Pageable pageable,
		@Parameter(hidden = true) Authentication auth
	) {
		// OrderQueryService.search는 ADMIN이면 조건 그대로 허용하는 로직이 있어야 함
		return ResponseEntity.ok(orderQueryService.search(condition, pageable, auth));
	}

	@Operation(summary = "주문 상세(관리자)", description = "관리자가 주문 ID로 상세 정보를 조회합니다.")
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDetailResponse> getDetail(
		@PathVariable UUID orderId,
		@Parameter(hidden = true) Authentication auth
	) {
		// 기존 상세 메서드는 관리자 접근 허용(requireReadable에서 hasAdmin 허용)
		return ResponseEntity.ok(orderService.getDetail(orderId, auth));
	}

	@Operation(summary = "주문 소프트 삭제(관리자)", description = "관리자가 주문을 소프트 삭제합니다.")
	@DeleteMapping("/{orderId}")
	public ResponseEntity<Void> softDelete(
		@PathVariable UUID orderId,
		@Parameter(hidden = true) Authentication auth
	) {
		orderService.softDelete(orderId, auth); // 관리자 전용 삭제
		return ResponseEntity.noContent().build();
	}
}

