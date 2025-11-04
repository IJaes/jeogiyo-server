package com.ijaes.jeogiyo.review.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 생성 요청")
public class CreateReviewRequest {

	@Schema(description = "리뷰를 작성할 주문 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID orderId;

	@Schema(description = "리뷰 대상 가게 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID storeId;

	@Schema(description = "새로운 리뷰 제목", example = "최고의 음식", required = true)
	private String title;

	@Schema(description = "새로운 리뷰 내용", example = "맛있어요", required = true)
	private String content;

	@Schema(description = "새로운 리뷰 평점", example = "4", required = true)
	private int rate;
}
