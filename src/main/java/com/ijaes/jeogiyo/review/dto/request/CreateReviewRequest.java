package com.ijaes.jeogiyo.review.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

	@NotNull(message = "주문 ID는 필수입니다.")
	@Schema(description = "리뷰를 작성할 주문 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID orderId;

	@NotNull(message = "가게 ID는 필수입니다.")
	@Schema(description = "리뷰 대상 가게 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef0123456789", required = true)
	private UUID storeId;

	@NotBlank(message = "제목은 필수입니다.")
	@Size(min = 1, max = 30, message = "제목은 1자 이상 30자 이하여야 합니다.")
	@Schema(description = "새로운 리뷰 제목", example = "최고의 음식", required = true)
	private String title;

	@NotBlank(message = "내용은 필수입니다.")
	@Size(min = 10, message = "내용은 10자 이상이어야 합니다.")
	@Schema(description = "새로운 리뷰 내용", example = "맛있어요", required = true)
	private String content;

	@NotNull(message = "평점은 필수입니다.")
	@Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
	@Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
	@Schema(description = "새로운 리뷰 평점", example = "4", required = true)
	private Integer rate;
}
