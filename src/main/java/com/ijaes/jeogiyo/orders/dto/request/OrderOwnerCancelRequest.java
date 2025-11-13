package com.ijaes.jeogiyo.orders.dto.request;

import java.util.UUID;

import com.ijaes.jeogiyo.payments.entity.CancelReason;

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
	private CancelReason cancelReason;
	private String paymentKey;

}
