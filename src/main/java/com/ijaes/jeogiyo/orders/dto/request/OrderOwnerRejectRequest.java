package com.ijaes.jeogiyo.orders.dto.request;

import java.util.UUID;

import com.ijaes.jeogiyo.orders.entity.RejectReasonCode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@Schema(description = "사장님 주문 거절 요청")
public class OrderOwnerRejectRequest {

	@NotNull(message = "주문 ID는 필수입니다.")
	@Schema(description = "주문 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4b3a96ff-2e5a-44a0-9f3a-1a2b3c4d5e6f")
	private final UUID orderId;

	@NotNull(message = "거절 사유 코드는 필수입니다.")
	@Schema(description = "거절 사유 코드", requiredMode = Schema.RequiredMode.REQUIRED, example = "OUT_OF_STOCK")
	private final RejectReasonCode reasonCode;

	@Schema(description = "거절 상세 사유(선택)", example = "재고 소진으로 금일 주문 불가")
	private final String reasonDetail;
}
