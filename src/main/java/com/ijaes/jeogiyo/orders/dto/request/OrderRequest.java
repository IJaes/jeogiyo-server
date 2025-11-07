package com.ijaes.jeogiyo.orders.dto.request;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
	private UUID orderId;
	private int amount;
	private UUID userId;

}
