package com.ijaes.jeogiyo.orders.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ijaes.jeogiyo.orders.entity.Order;
import com.ijaes.jeogiyo.orders.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {
	//간단한 CRUD, 페이징 쿼리(동적 쿼리x)

	/** 기본 CRUD 조회 */
	// 사용자 ID 기준으로 주문 조회
	List<Order> findByUserId(UUID userId);

	// 가게 ID 기준으로 주문 조회
	List<Order> findByStoreId(UUID storeId);

	/** 상태 기반 조회 */
	// 주문 상태에 따라 주문 조회
	List<Order> findByOrderStatus(OrderStatus status);

	// 특정 사용자(userId)의 주문 상태에 따라 주문 조회
	List<Order> findByOrderStatusAndUserId(OrderStatus status, UUID userId);

	// 특정 가게(storeId)의 주문 상태에 따라 주문 조회
	List<Order> findByOrderStatusAndStoreId(OrderStatus status, UUID storeId);

	/** 페이징 기능 */
	// 사용자 ID 기준으로 주문 내역을 페이지 단위로 조회
	Page<Order> findAllByUserId(UUID userId, Pageable pageable);

	// 가게 ID 기준으로 주문 내역을 페이지 단위로 조회
	Page<Order> findAllByStoreId(UUID storeId, Pageable pageable);

}