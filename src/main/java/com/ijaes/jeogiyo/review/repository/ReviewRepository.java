package com.ijaes.jeogiyo.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ijaes.jeogiyo.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	//사용자별 작성한 리뷰 전체 목록 조회
	List<Review> findByUserId(Long id);

	//
}
