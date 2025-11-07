package com.ijaes.jeogiyo.orders.dto.request;

import java.util.UUID;

import com.ijaes.jeogiyo.orders.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderOwnerCancelRequest {
	private UUID orderId;
	private OrderStatus canCelReason;
	private String paymentKey;

}
