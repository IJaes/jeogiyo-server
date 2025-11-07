package com.ijaes.jeogiyo.orders.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ijaes.jeogiyo.orders.entity.Order;

public interface OrderRepositoryCustom {
	// 복합 조건, 동적 검색 및 정렬이 필요하여 Custom 생성

	// 복합 검색 기능 - 여러가지를 조합하여 검색
	Page<Order> searchOrders(OrderSearchCondition condition, Pageable pageable);
}