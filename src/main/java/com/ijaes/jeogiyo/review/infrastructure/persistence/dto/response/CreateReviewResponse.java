package com.ijaes.jeogiyo.review.infrastructure.persistence.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 생성 요청 응답")
public class CreateReviewResponse {

	@Schema(description = "새로 생성된 리뷰 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID reviewId;

	@Schema(description = "리뷰가 작성된 주문 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID orderId;

	@Schema(description = "리뷰 대상 가게 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID storeId;

	@Schema(description = "리뷰 생성 시각")
	LocalDateTime createdAt;
}
