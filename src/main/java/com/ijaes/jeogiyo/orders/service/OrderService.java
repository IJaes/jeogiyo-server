package com.ijaes.jeogiyo.orders.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.orders.dto.request.OrderOwnerCancelRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderUserCancelRequest;
import com.ijaes.jeogiyo.payments.entity.CancelReason;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

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

}

