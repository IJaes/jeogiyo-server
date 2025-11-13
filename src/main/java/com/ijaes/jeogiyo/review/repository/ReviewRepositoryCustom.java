package com.ijaes.jeogiyo.review.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;

public interface ReviewRepositoryCustom {
	//복잡한 조회, 복잡한 검색 및 페이지네이션이 필요한 경우

	//1. 작성된 리뷰 전체 조회 - 관리자
	Page<ReviewResponse> findAllReviewsForAdmin(int page, int size, String filterType);

	//2. 사용자 아이디 기준으로 리뷰 전체 조회
	Page<ReviewResponse> findReviewsByUserId(UUID userId, int page, int size, String filterType, String sortType);

	//3. 가게 아이디 기준으로 리뷰 전체 조회
	Page<ReviewResponse> findReviewsByStoreID(UUID storeId, int page, int size, String sortType);

	//4. 가게별 리뷰 평점 계산
	Double calculateAverageRateByStoreId(UUID storeId);
}
