package com.ijaes.jeogiyo.review.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.ijaes.jeogiyo.review.entity.Review;

public interface ReviewRepositoryCustom {
	//복잡한 조회, 복잡한 검색 및 페이지네이션이 필요한 경우

	// Page<Review> findAll(UUID storeId, int page, int size);

	//1. 작성된 리뷰 전체 조회 - 관리자
	Page<Review> findAllReviewsByPaging(int page, int size);

	//2. 사용자 아이디 기준으로 리뷰 전체 조회
	Page<Review> findReviewsByUserId(UUID userId, int page, int size);

	//3. 가게 아이디 기준으로 리뷰 전체 조회
	Page<Review> findReviewsByStoreID(UUID storeId, int page, int size);
}
