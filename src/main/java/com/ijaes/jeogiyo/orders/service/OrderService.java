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
import com.ijaes.jeogiyo.orders.dto.request.OrderOwnerCancelRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderUserCancelRequest;
import com.ijaes.jeogiyo.orders.dto.response.OrderDetailResponse;
import com.ijaes.jeogiyo.orders.dto.response.OrderSummaryResponse;
import com.ijaes.jeogiyo.orders.entity.Order;
import com.ijaes.jeogiyo.orders.entity.OrderStatus;
import com.ijaes.jeogiyo.orders.repository.OrderRepository;
import com.ijaes.jeogiyo.payments.entity.CancelReason;
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
	private final StoreRepository storeRepository;
	private final ApplicationEventPublisher eventPublisher;

	// 결제 승인 요청
	public void orderProcess(UUID orderId, int amount, UUID userId) {
		orderId = UUID.fromString("82671ED9-B61A-11F0-97EA-EED0BD4D46");
		amount = 100;
		userId = UUID.fromString("08100bf7-58ea-4cc6-851e-fe48a7813654");
		eventPublisher.publishEvent(new OrderRequest(orderId, amount, userId));
	}

	//	사용자 결제 취소 요청
	public void orderCancel(UUID orderId, String paymentKey, CancelReason canCelReason, UUID userId) {
		orderId = UUID.fromString("82671ED9-B61A-11F0-97EA-EED0BD4D080");
		paymentKey = "tviva20251105105118NaR73";
		CancelReason cancelReason = CancelReason.USERCANCEL;
		userId = UUID.fromString("08100bf7-58ea-4cc6-851e-fe48a7813654");
		eventPublisher.publishEvent(new OrderUserCancelRequest(orderId, paymentKey, cancelReason, userId));
	}

	public void orderOwnerCancel(UUID orderId, String paymentKey, CancelReason canCelReason, UUID userId) {
		orderId = UUID.fromString("82671ED9-B61A-11F0-97EA-EED0BD4D35");
		paymentKey = "tviva20251106155446TxWp3";
		CancelReason cancelReason = CancelReason.STORECANCEL;
		userId = UUID.fromString("08100bf7-58ea-4cc6-851e-fe48a7813654");
		eventPublisher.publishEvent(new OrderOwnerCancelRequest(orderId, paymentKey, canCelReason, userId));
	}

	// ====== 이벤트 테스트(옵션) ======
	// public void orderProcess(UUID orderId, int amount) {
	// 	eventPublisher.publishEvent(new OrderEvent(orderId, amount));
	// }

	// ========== 생성 ==========
	@Transactional
	public OrderDetailResponse create(OrderCreateRequest req, Authentication auth) {
		UUID userId = currentUserId(auth);

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

	/** 조회 **/

	// ========== 아이디 단위 상세 조회 ==========
	@Transactional(readOnly = true)
	public OrderDetailResponse getDetail(UUID orderId, Authentication auth) {
		Order order = getAlive(orderId);
		requireReadable(auth, order);
		return OrderDetailResponse.from(order);
	}

	// ========== 아이디 단위 목록 조회 ==========
	@Transactional(readOnly = true)
	public Page<OrderSummaryResponse> getUserOrders(Authentication auth, Pageable pageable) {
		UUID userId = currentUserId(auth);
		return orderRepository.findAllByUserId(userId, pageable)
			.map(OrderSummaryResponse::from);
	}

	// ========== 가게 단위 목록 조회 ==========
	@Transactional(readOnly = true)
	public Page<OrderDetailResponse> getStoreOrders(UUID storeId, Authentication auth, Pageable pageable) {
		requireOwnerOfStore(auth, storeId);
		return orderRepository.findAllByStoreId(storeId, pageable)
			.map(OrderDetailResponse::from);
	}

	// ========== 로그인한 회원의 주문 상태 목록 조회 ==========
	@Transactional(readOnly = true)
	public Page<OrderDetailResponse> getUserOrdersByStatus(Authentication auth, OrderStatus status, Pageable pageable) {
		UUID userId = currentUserId(auth);
		return orderRepository.findByOrderStatusAndUserId(status, userId, pageable)
			.map(OrderDetailResponse::from);
	}

	// ========== 로그인한 점주의 가게 단위 목록 조회 ==========
	@Transactional(readOnly = true)
	public Page<OrderDetailResponse> getStoreOrdersByStatus(UUID storeId, Authentication auth, OrderStatus status,
		Pageable pageable) {
		requireOwnerOfStore(auth, storeId);
		return orderRepository.findByOrderStatusAndStoreId(status, storeId, pageable)
			.map(OrderDetailResponse::from);
	}

	// ========== 사용자 취소 ==========
	@Transactional
	public void cancelByUser(UUID orderId, Authentication auth) {
		UUID currentUserId = currentUserId(auth);
		Order order = getAlive(orderId);

		if (!order.getUserId().equals(currentUserId)) {
			throw new CustomException(ORDER_USER_MISMATCH);
		}
		order.cancelByUser(LocalDateTime.now());
		publishOrderEvent(order.getId(), 0);

	}

	// ========== 점주 거절 ==========
	@Transactional
	public void rejectByOwner(UUID orderId, Authentication auth/*, RejectReasonCode reason */) {
		Order order = getAlive(orderId);
		requireOwner(auth, order);

		// order.rejectByOwner(reason);
		order.rejectByOwner(order.getRejectReasonCode()); // 현재 구조 유지
		publishOrderEvent(order.getId(), 0);
		order.changeStatus(OrderStatus.REFUND); // 환불로 변경
	}

	// ========== 점주 상태 변경 ==========
	@Transactional
	public void changeStatusByOwner(UUID orderId, OrderStatus nextStatus, Authentication auth) {
		Order order = getAlive(orderId);
		requireOwner(auth, order);

		order.changeStatus(nextStatus);
		// 필요시 이벤트
		// publishOrderEvent(order.getId(), 0);
	}

	// ========== 소프트 삭제 ==========
	@Transactional
	public void softDelete(UUID orderId, Authentication auth) {
		Order order = getAlive(orderId);

		if (!isOwner(auth, order) && !isOrderUser(auth, order) && !hasAdmin(auth)) {
			throw new CustomException(ACCESS_DENIED);
		}
		order.delete();
		orderRepository.save(order);
	}

	// ====== 공통 유틸 ======
	// 소프트 되지 않은 주문을 ID로 조회
	private Order getAlive(UUID orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND));
		if (order.isDeleted())
			throw new CustomException(ORDER_ALREADY_DELETED);
		return order;
	}

	// 결제 관련 이벤트
	private void publishOrderEvent(UUID orderId, int amount) {
		try {
			// eventPublisher.publishEvent(new OrderEvexnt(orderId, amount));
		} catch (Exception e) {
			log.error("OrderEvent publish failed. orderId={}, amount={}", orderId, amount, e);
			throw new CustomException(ORDER_EVENT_FAILED);
		}
	}

	// 회원인지 아닌지 체크
	private void requireReadable(Authentication auth, Order order) {
		if (isOrderUser(auth, order))
			return;
		if (isOwner(auth, order))
			return;
		if (hasAdmin(auth))
			return;
		throw new CustomException(ACCESS_DENIED);
	}

	// 점주만 가능한 동작
	private void requireOwner(Authentication auth, Order order) {
		if (!isOwner(auth, order)) {
			throw new CustomException(ORDER_OWNER_MISMATCH);
		}
	}

	// 점주의 가게인지 검증
	private void requireOwnerOfStore(Authentication auth, UUID storeId) {
		UUID uuid = currentUserId(auth);

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

		if (store.isDeleted())
			throw new CustomException(STORE_NOT_FOUND);

		User owner = store.getOwner();
		if (owner == null || !uuid.equals(owner.getId())) {
			throw new CustomException(ORDER_OWNER_MISMATCH);
		}
	}

	// 주문한 회원인지 검증
	private boolean isOrderUser(Authentication auth, Order order) {
		UUID uuid = currentUserId(auth);
		return order.getUserId().equals(uuid);
	}

	// 사장인지 검증
	private boolean isOwner(Authentication auth, Order order) {
		UUID uuid = currentUserId(auth);

		Store store = storeRepository.findById(order.getStoreId())
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

		// (옵션) 삭제된 가게 방어
		if (store.isDeleted())
			throw new CustomException(STORE_NOT_FOUND);
		User owner = store.getOwner(); // LAZY 주의: 트랜잭션 안에서 접근
		if (owner == null)
			return false;

		return uuid.equals(owner.getId());
	}

	// 현재 로그인한 사용자의 UUID를 안전하게 꺼내기 위한 공통 헬퍼
	private static UUID currentUserId(Authentication auth) {
		// 지금 유저의 uuid를 조회
		Object p = auth.getPrincipal(); // getPrincipal로 현재 로그인한 사용자를 꺼낸다

		// 로그인하지 않았으면 접근 거부 에러
		if (p instanceof User u) {
			return u.getId();
		} else {
			throw new CustomException(ACCESS_DENIED);
		}
	}

	private boolean hasAdmin(Authentication auth) {
		return auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
}
