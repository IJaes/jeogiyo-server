package com.ijaes.jeogiyo.orders.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.orders.dto.request.OrderOwnerCancelRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderUserCancelRequest;
import com.ijaes.jeogiyo.payments.entity.CanCelReason;

@Service

@Transactional
public class OrderService {

	private final ApplicationEventPublisher eventPublisher;

	public OrderService(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	// 결제 승인 요청
	public void orderProcess(UUID orderId, int amount, String username) {
		orderId = UUID.fromString("82671ED9-B61A-11F0-97EA-EED0BD4D019");
		amount = 1;
		username = "test2";
		eventPublisher.publishEvent(new OrderRequest(orderId, amount, username));
	}

	//	사용자 결제 취소 요청
	public void orderCancel(UUID orderId, String paymentKey, CanCelReason canCelReason, String username) {
		orderId = UUID.fromString("82671ED9-B61A-11F0-97EA-EED0BD4D018");
		paymentKey = "tviva20251105153412HZBK4";
		CanCelReason cancelReason = CanCelReason.USERCANCEL;
		username = "test2";
		eventPublisher.publishEvent(new OrderUserCancelRequest(orderId, paymentKey, canCelReason, username));
	}

	public void orderOwnerCancel(UUID orderId, String paymentKey, CanCelReason canCelReason, String username) {
		orderId = UUID.fromString("82671ED9-B61A-11F0-97EA-EED0BD4D019");
		paymentKey = "tviva20251105153702Ikrt3";
		CanCelReason cancelReason = CanCelReason.STORECANCEL;
		username = "owner";
		eventPublisher.publishEvent(new OrderOwnerCancelRequest(orderId, paymentKey, canCelReason, username));
	}

}

