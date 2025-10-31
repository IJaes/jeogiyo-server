package com.ijaes.jeogiyo.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "가게 수정 요청")
public class UpdateStoreRequest {

	@Size(min = 1, max = 50, message = "가게명은 1자 이상 50자 이하여야 합니다.")
	@Schema(description = "가게명", example = "소문난 국밥집")
	private String name;

	@Size(min = 1, max = 100, message = "주소는 1자 이상 100자 이하여야 합니다.")
	@Schema(description = "주소", example = "서울시 강남구 역삼동")
	private String address;

	@Size(min = 1, max = 500, message = "설명은 1자 이상 500자 이하여야 합니다.")
	@Schema(description = "설명", example = "뜨근뜨끈한 국물 한 사발 먹고 가세요")
	private String description;

	@Schema(description = "카테고리", example = "KOREAN")
	private String category;
}