package com.ijaes.jeogiyo.store.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "가게 상세 정보 응답 (사장님 정보 포함)")
public class StoreDetailResponse {

	@Schema(description = "가게 ID")
	private UUID id;

	@Schema(description = "사업자번호", example = "123-45-67890")
	private String businessNumber;

	@Schema(description = "가게명", example = "소문난 국밥집")
	private String name;

	@Schema(description = "주소", example = "서울시 강남구 역삼동")
	private String address;

	@Schema(description = "설명", example = "뜨근뜨끈한 국물 한 사발 먹고 가세요")
	private String description;

	@Schema(description = "카테고리", example = "KOREAN")
	private String category;

	@Schema(description = "평점", example = "4.5")
	private Double rate;

	@Schema(description = "사장님 정보")
	private OwnerInfo owner;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Schema(description = "사장님 정보")
	public static class OwnerInfo {
		@Schema(description = "사장님 ID")
		private UUID id;

		@Schema(description = "사장님 이름")
		private String name;

		@Schema(description = "사장님 아이디", example = "owner123")
		private String username;

		@Schema(description = "사장님 전화번호", example = "010-1234-5678")
		private String phoneNumber;

		@Schema(description = "사장님 주소")
		private String address;
	}
}
