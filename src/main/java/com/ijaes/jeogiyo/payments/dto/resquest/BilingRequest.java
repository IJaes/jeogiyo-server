package com.ijaes.jeogiyo.payments.dto.resquest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter

@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authkey, BillingKey 발급 요청")
public class BilingRequest {
	private String authKey;
	private String billingKey;
}
