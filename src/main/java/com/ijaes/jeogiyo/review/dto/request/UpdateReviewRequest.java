package com.ijaes.jeogiyo.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 수정 요청")
public class UpdateReviewRequest {

	@Schema(description = "수정된 제목", example = "굿굿", nullable = true)
	private String title;

	@Schema(description = "수정된 내용", example = "재주문입니다. 맛있어요.", nullable = true)
	private String content;

	@Schema(description = "수정된 평점", example = "5", nullable = true)
	private Integer rate;
}
