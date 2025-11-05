package com.ijaes.jeogiyo.orders.service;

import static com.ijaes.jeogiyo.common.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.orders.dto.request.OrderCreateRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderEvent;
import com.ijaes.jeogiyo.orders.dto.response.OrderDetailResponse;
import com.ijaes.jeogiyo.orders.entity.Order;
import com.ijaes.jeogiyo.orders.entity.OrderStatus;
import com.ijaes.jeogiyo.orders.repository.OrderRepository;
import com.ijaes.jeogiyo.orders.repository.OrderRepositoryCustom;
import com.ijaes.jeogiyo.orders.repository.OrderSearchCondition;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderRepositoryCustom orderRepositoryCustom;
	private final StoreRepository storeRepository;
	private final ApplicationEventPublisher eventPublisher;

	public void orderProcess(UUID orderId, int amount) {
		orderId = UUID.fromString("82671ED9-B61A-11F0-97EA-EED0BD4D0181");
		amount = 1;
		eventPublisher.publishEvent(new OrderEvent(orderId, amount));
	}

	// ========== 생성 ==========
	@Transactional
	public OrderDetailResponse create(OrderCreateRequest req, Authentication auth) {
		UUID userId = ((User)auth.getPrincipal()).getId();

		Store store = storeRepository.findById(req.getStoreId())
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

		Order order = Order.builder()
			.userId(userId)
			.storeId(store.getId())
			.totalPrice(req.getTotalPrice())
			.transactionId(req.getTransactionId())
			.build();

		orderRepository.save(order);
		publishOrderEvent(order.getId(), order.getTotalPrice());
		return OrderDetailResponse.from(order);
	}

	// ========== 상세 ==========
	@Transactional(readOnly = true)
	public OrderDetailResponse getDetail(UUID orderId, Authentication auth) {
		Order order = getAlive(orderId);
		requireReadable(auth, order);
		return OrderDetailResponse.from(order);
	}

	// ========== 검색 ==========
	@Transactional(readOnly = true)
	public Page<OrderDetailResponse> search(OrderSearchCondition con, Pageable page) {
		return orderRepositoryCustom.searchOrders(con, page)
			.map(OrderDetailResponse::from);
	}

	// ========== 사용자 취소 ==========
	@Transactional
	public void cancelByUser(UUID orderId, Authentication auth) {
		UUID currentUserId = ((User)auth.getPrincipal()).getId();
		Order order = getAlive(orderId);

		if (!order.getUserId().equals(currentUserId)) {
			throw new CustomException(ORDER_USER_MISMATCH);
		}

		// 엔터티가 now를 받도록 설계했다면:
		order.cancelByUser(LocalDateTime.now());
		// 만약 기존 시그니처가 cancelByUser(Clock)라면 엔터티 메서드를 LocalDateTime 기반으로 바꿔줘.

		publishOrderEvent(order.getId(), 0);
	}

	// ========== 점주 거절 ==========
	@Transactional
	public void rejectByOwner(UUID orderId, Authentication auth /*, RejectReasonCode reason */) {
		Order order = getAlive(orderId);
		requireOwner(auth, order);

		// order.rejectByOwner(reason);
		order.rejectByOwner(order.getRejectReasonCode());
		publishOrderEvent(order.getId(), 0);
	}

	// ========== 점주 상태 변경 ==========
	@Transactional
	public void changeStatusByOwner(UUID orderId, OrderStatus nextStatus, Authentication auth) {
		Order order = getAlive(orderId);
		requireOwner(auth, order);

		order.changeStatus(nextStatus);
		publishOrderEvent(order.getId(), 0);
	}

	// ========== 삭제(소프트) ==========
	@Transactional
	public void softDelete(UUID orderId, Authentication auth) {
		Order order = getAlive(orderId);

		if (!isOwner(auth, order) && !isOrderUser(auth, order)) {
			throw new CustomException(ACCESS_DENIED);
		}
		order.delete();
		// 필요 시 삭제 이벤트/색인 갱신 이벤트 발행
	}

	// ===== 공통 유틸 =====

	private Order getAlive(UUID orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND));
		if (order.isDeleted())
			throw new CustomException(ORDER_ALREADY_DELETED);
		return order;
	}

	private void publishOrderEvent(UUID orderId, int amount) {
		try {
			eventPublisher.publishEvent(new OrderEvent(orderId, amount));
		} catch (Exception e) {
			log.error("OrderEvent publish failed. orderId={}, amount={}", orderId, amount, e);
			throw new CustomException(ORDER_EVENT_FAILED);
		}
	}

	private void requireReadable(Authentication auth, Order order) {
		if (isOrderUser(auth, order))
			return;
		if (isOwner(auth, order))
			return;
		if (hasAdmin(auth))
			return;
		throw new CustomException(ACCESS_DENIED);
		// 필요하면 여기서 필드 더 확장
	}

	private void requireOwner(Authentication auth, Order order) {
		if (!isOwner(auth, order)) {
			throw new CustomException(ORDER_OWNER_MISMATCH);
		}
	}

	private boolean isOrderUser(Authentication auth, Order order) {
		UUID uid = ((User)auth.getPrincipal()).getId();
		return order.getUserId().equals(uid);
	}

	private boolean isOwner(Authentication auth, Order order) {
		UUID uid = ((User)auth.getPrincipal()).getId();
		Store store = storeRepository.findById(order.getStoreId())
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
		return store.getOwner().isOwner();
	}

	private boolean hasAdmin(Authentication auth) {
		return auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
}

