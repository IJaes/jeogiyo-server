package com.ijaes.jeogiyo.review.domain.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.domain.Review;
import com.ijaes.jeogiyo.review.domain.ReviewRepository;
import com.ijaes.jeogiyo.review.infrastructure.persistence.dto.request.CreateReviewRequest;
import com.ijaes.jeogiyo.review.infrastructure.persistence.dto.response.CreateReviewResponse;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;

	public CreateReviewResponse createReview(Authentication authentication, CreateReviewRequest request) {
		User user = (User)authentication.getPrincipal();
		UUID currentUserId = user.getId();

		// 리뷰 중복 작성 방지
		if (reviewRepository.existsByOrderId(request.getOrderId())) {
			throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
		}

		// 리뷰 객체 생성
		Review newReview = Review.builder()
			.reviewId(UUID.randomUUID())
			.orderId(request.getOrderId())
			.userId(currentUserId)
			.storeId(request.getStoreId())
			.title(request.getTitle())
			.content(request.getContent())
			.rate(request.getRate())
			.build();

		// db 저장
		Review savedReview = reviewRepository.save(newReview);

		// 여기서 이벤트 발행 : 리뷰 작성 완료 알려서 store에서 평점 업데이트 하도록

		return CreateReviewResponse.builder()
			.reviewId(savedReview.getReviewId())
			.orderId(savedReview.getOrderId())
			.storeId(savedReview.getStoreId())
			.createdAt(savedReview.getCreatedAt())
			.build();
	}
}
