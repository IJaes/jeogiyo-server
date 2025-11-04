package com.ijaes.jeogiyo.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ijaes.jeogiyo.review.entity.Review;

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
@Schema(description = "리뷰 단건 조회 요청 응답")
public class ReviewResponse {

	@Schema(description = "리뷰 식별 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID reviewId;

	@Schema(description = "리뷰가 작성된 주문 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID orderId;

	@Schema(description = "리뷰 대상 가게 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID storeId;

	@Schema(description = "리뷰 제목", example = "최고의 음식", required = true)
	private String title;

	@Schema(description = "리뷰 내용", example = "맛있어요", required = true)
	private String content;

	@Schema(description = "평점", example = "4", required = true)
	private Integer rate;

	@Schema(description = "리뷰 작성자 이름(아이디)", example = "gildong", required = true)
	private String reviewerName;

	@Schema(description = "리뷰 작성된 가게 이름", example = "신선설농탕", required = true)
	private String storeName;

	@Schema(description = "리뷰 생성 시각")
	LocalDateTime createdAt;

	@Schema(description = "리뷰 수정 시각")
	LocalDateTime updatedAt;

	public static ReviewResponse of(Review review, String reviewerName, String storeName) {
		return ReviewResponse.builder()
			.reviewId(review.getReviewId())
			.orderId(review.getOrderId())
			.storeId(review.getStoreId())
			.title(review.getTitle())
			.content(review.getContent())
			.rate(review.getRate())
			.reviewerName(reviewerName)
			.storeName(storeName)
			.createdAt(review.getCreatedAt())
			.updatedAt(review.getUpdatedAt())
			.build();
	}
}
