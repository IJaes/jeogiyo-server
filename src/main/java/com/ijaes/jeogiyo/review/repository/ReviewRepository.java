package com.ijaes.jeogiyo.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ijaes.jeogiyo.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	//사용자별 작성한 리뷰 전체 목록 조회
	List<Review> findByUserId(Long id);

	//주문 아이디 기준으로 리뷰 조회
	Optional<Review> findByOrderId(Long orderId);

}
