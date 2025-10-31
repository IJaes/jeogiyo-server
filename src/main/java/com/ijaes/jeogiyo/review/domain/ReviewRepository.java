package com.ijaes.jeogiyo.review.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
	//crud 및 단순 조회

	//1. 리뷰 아이디 기준으로 리뷰 조회(단건)

	//2. 주문 아이디 기준으로 리뷰 조회(단건)
	Optional<Review> findByOrderId(UUID orderId);

	//사용자별 작성한 리뷰 전체 목록 조회
	// List<Review> findByUserId(UUID userId);
}
