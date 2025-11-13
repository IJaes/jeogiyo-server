package com.ijaes.jeogiyo.store.dto.response;

import java.util.UUID;

import com.ijaes.jeogiyo.store.entity.Store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "가게 정보 응답")
public class StoreResponse {

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

	@Schema(description = "사장님 ID")
	private UUID ownerId;

	@Schema(description = "사용자로부터의 거리 (km)", example = "1.5")
	private Double distance;

	public static StoreResponse fromEntity(Store store, Double distance) {
		return StoreResponse.builder()
			.id(store.getId())
			.businessNumber(store.getBusinessNumber())
			.name(store.getName())
			.address(store.getAddress())
			.description(store.getDescription())
			.category(store.getCategory().name())
			.rate(store.getRate())
			.ownerId(store.getOwner().getId())
			.distance(distance)
			.build();
	}

	public static StoreResponse fromEntity(Store store) {
		return StoreResponse.builder()
			.id(store.getId())
			.businessNumber(store.getBusinessNumber())
			.name(store.getName())
			.address(store.getAddress())
			.description(store.getDescription())
			.category(store.getCategory().name())
			.rate(store.getRate())
			.ownerId(store.getOwner().getId())
			.build();
	}
}
