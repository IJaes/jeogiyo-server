package com.ijaes.jeogiyo.orders.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.orders.dto.request.OrderEvent;

@Service

@Transactional
public class OrderService {

	private final ApplicationEventPublisher eventPublisher;

	public OrderService(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void orderProcess(UUID orderId, int amount) {
		orderId = UUID.fromString("82671ED9-B61A-11F0-97EA-EED0BD4DB110");
		amount = 1;
		eventPublisher.publishEvent(new OrderEvent(orderId, amount));

	}
}
