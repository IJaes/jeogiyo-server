package com.ijaes.jeogiyo.orders.service;

import static com.ijaes.jeogiyo.common.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
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
import com.ijaes.jeogiyo.payments.dto.response.PaymentApproveResponse;
import com.ijaes.jeogiyo.payments.dto.response.PaymentCancelResponse;
import com.ijaes.jeogiyo.payments.entity.CancelReason;
import com.ijaes.jeogiyo.payments.entity.PaymentStatus;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
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

	@EventListener
	// 결제 후 주문 상태 업데이트
	// 결제 상태에 따라 주문 상태 업데이트, 결제 실패시 paymentKey발급 안 됨(null 값)
	// 결제 상태는 SUCCESS 또는 FAIL 두가지로만  반환됩니다
	@Transactional
	public void createOrderStatusUpdate(PaymentApproveResponse paymentResponse) {

		Order order = getAlive(paymentResponse.getOrderId());

		// 이벤트 기반 확정: SUCCESS -> 결제 승인, FAIL -> 결제 실패(취소)
		if (PaymentStatus.SUCCESS == paymentResponse.getStatus()) {
			// 결제 승인 → ACCEPTED -> PAID
			order.markPaid(paymentResponse.getPaymentKey());
		} else {
			// 결제 실패 → 조리 전이면 취소
			order.cancelByPaymentFailure();
		}

		orderRepository.save(order);
		// 값 넘어오는지 확인
		System.out.println("payment 상태: " + paymentResponse.getStatus() + "  ");
	}

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
			.build();

		orderRepository.save(order);

		eventPublisher.publishEvent(new OrderRequest(order.getId(), order.getTotalPrice(), userId));
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

	// ========== 아이디 단위 목록 조회(일반 사용자) ==========
	@Transactional(readOnly = true)
	public Page<OrderSummaryResponse> getUserOrders(Authentication auth, Pageable pageable) {
		UUID userId = currentUserId(auth);
		return orderRepository.findAllByUserId(userId, pageable)
			.map(OrderSummaryResponse::from);
	}

	// ========== 로그인한 회원의 주문 상태 목록 조회(일반 사용자) ==========
	@Transactional(readOnly = true)
	public Page<OrderSummaryResponse> getUserOrdersByStatus(Authentication auth, OrderStatus status,
		Pageable pageable) {
		UUID userId = currentUserId(auth);
		return orderRepository.findByOrderStatusAndUserId(status, userId, pageable)
			.map(OrderSummaryResponse::from);
	}

	// ========== 가게 단위 목록 조회(점주) ==========
	@Transactional(readOnly = true)
	public Page<OrderSummaryResponse> getStoreOrders(UUID storeId, Authentication auth, Pageable pageable) {
		requireOwnerOfStore(auth, storeId);
		return orderRepository.findAllByStoreId(storeId, pageable)
			.map(OrderSummaryResponse::from);
	}

	// ========== 로그인한 점주의 가게 단위 목록 조회(점주) ==========
	@Transactional(readOnly = true)
	public Page<OrderSummaryResponse> getStoreOrdersByStatus(UUID storeId, Authentication auth, OrderStatus status,
		Pageable pageable) {
		requireOwnerOfStore(auth, storeId);
		return orderRepository.findByOrderStatusAndStoreId(status, storeId, pageable)
			.map(OrderSummaryResponse::from);
	}

	/** 취소 **/

	//사용자가 주문 생성 시 결제 성공하게 되면 상태값이 PAID이기 떄문에 결제 취소하려고 하면
	//"해당 작업은 주문 대기 상태에서만 가능합니다. " 이런 에러가 나오는데
	// 사용자 결제 취소 조건은 주문 후 5분이내이고 주문조리시작 전에는 취소 가능하도록 설정해야할 거 같습니다..!
	@Transactional
	public void cancel(UUID orderId, Authentication auth) {
		// ✅ 환불 없는 취소: 주문자 확인 + 취소만 수행
		Order order = getOrder(orderId, auth);
		order.cancelOrder(LocalDateTime.now());
	}

	@Transactional
	public void refund(UUID orderId, Authentication auth) {
		// 주문 조회 + 접근자 검증(당사자/점주)까지 내부에서 처리한다고 가정
		Order order = getOrder(orderId, auth);

		User user = (User)auth.getPrincipal();
		Role role = user.getRole();

		// 결제사 키
		String paymentKey = order.getTransactionId(); // 혹은 req/paymentGateway 응답 등

		switch (role) {
			case OWNER -> {
				// 1) 점주 환불 도메인 처리 (거절사유 포함)
				order.requestRefund(order.getRejectReasonCode()); // ★ REFUND_PENDING으로 전이
				// 2) 점주 이벤트 발행
				eventPublisher.publishEvent(
					new OrderOwnerCancelRequest(orderId, CancelReason.STORECANCEL, paymentKey)
				);
			}

			case USER -> {
				// 1) 사용자 환불 도메인 처리 (거절사유 없음)-> 이후 필요하면 추가
				order.requestRefund(null); // ★ REFUND_PENDING으로 전이
				// 2) 사용자 이벤트 발행
				eventPublisher.publishEvent(
					new OrderUserCancelRequest(orderId, paymentKey, CancelReason.USERCANCEL, user.getId())
				);
				// 3) 상태 변경이 도메인 메서드(refundOrder)에서 안 된다면 여기서 한 번만!
				// order.changeStatus(OrderStatus.REFUND);
			}

			default -> throw new CustomException(ACCESS_DENIED);
		}
	}

	// UUID 를 확인하고 아닐 경우 에러 발생시키는 메서드
	private Order getOrder(UUID orderId, Authentication auth) {
		UUID uuid = currentUserId(auth);
		// 소프트 삭제인지 확인하는 메서드
		Order order = getAlive(orderId);
		if (!order.getUserId().equals(uuid)) {
			throw new CustomException(ORDER_USER_MISMATCH);
		}
		return order;
	}

	@EventListener
	// 결제 취소 후 주문 상태 업데이트
	@Transactional
	public void cancelOrderStatusUpdate(PaymentCancelResponse paymentResponse) {
		// 값 넘어오는지 확인
		System.out.println(paymentResponse.getStatus());
		Order order = getAlive(paymentResponse.getOrderId());

		// 결제사로부터 '환불 완료'가 확인된 시점에 최종 확정
		order.markRefundCompleted();

		orderRepository.save(order);
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

	/** 소프트 삭제 */
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

	/** 공통 유틸 */
	// 소프트 삭제 되지 않은 주문을 ID로 조회
	private Order getAlive(UUID orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(RESOURCE_NOT_FOUND));
		if (order.isDeleted())
			throw new CustomException(ORDER_ALREADY_DELETED);
		return order;
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

	// 관리자 인지 체크
	private boolean hasAdmin(Authentication auth) {
		return auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
	}
}
