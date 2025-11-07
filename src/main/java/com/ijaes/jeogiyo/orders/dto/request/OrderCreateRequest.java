package com.ijaes.jeogiyo.orders.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized // 역직렬화 어노테이션
@Schema(description = "주문 생성 요청")
public class OrderCreateRequest {

	@NotNull(message = "가게 ID는 필수입니다.")
	@Schema(description = "가게 ID", example = "6f6f3a5b-8e06-4c3c-a6f3-5c6d9a8b7c1e", requiredMode = Schema.RequiredMode.REQUIRED)
	private final UUID storeId;

	@PositiveOrZero(message = "총 결제금액은 0 이상이어야 합니다.")
	@Schema(description = "총 결제금액(원)", example = "27000", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
	private final int totalPrice;

	// @NotBlank(message = "PG 거래 ID는 필수입니다.")
	// @Schema(description = "PG 거래 ID", example = "pg-2025-11-05-00001", requiredMode = Schema.RequiredMode.REQUIRED)
	// private final String transactionId;

}
