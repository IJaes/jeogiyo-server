package com.ijaes.jeogiyo.review.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
	//crud 및 단순 조회

	//1. 주문 아이디 기준으로 리뷰 조회(단건)
	Optional<Review> findByOrderId(UUID orderId);

	//2. 리뷰 중복 작성 방지
	boolean existsByOrderId(UUID orderId);

	//3. 사용자 아이디와 리뷰 아이디로 조회(단건)
	Optional<Review> findByReviewIdAndUserId(UUID reviewId, UUID userId);
}
