package com.ijaes.jeogiyo.review.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.review.repository.ReviewRepository;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewAdminService {

	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;

	//1. 전체 리뷰 조회(삭제된 리뷰, 숨겨진 리뷰 등 전체 포함)
	public Page<ReviewResponse> getAllReviewsForAdmin(int page, int size) {

		//최신순으로 정렬
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		//전체 리뷰 가져오기
		Page<Review> reviewPage = reviewRepository.findAll(pageable);

		Page<ReviewResponse> response = reviewPage.map(review -> {

			//작성자 이름 조회
			String reviewerName = userRepository.findById(review.getUserId())
				.map(User::getUsername)
				.orElse("데이터 없음");

			//가게 이름 조회
			String storeName = storeRepository.findById(review.getStoreId())
				.map(Store::getName)
				.orElse("데이터 없음");

			return ReviewResponse.of(review, reviewerName, storeName);
		});

		return response;
	}

	//2. 특정 리뷰 조회
	public ReviewResponse getReviewForAdmin(UUID reviewId) {

		//리뷰 존재 여부 확인
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		//작성자 이름 조회
		String reviewerName = userRepository.findById(review.getUserId())
			.map(User::getUsername)
			.orElse("데이터 없음");

		//가게 이름 조회
		String storeName = storeRepository.findById(review.getStoreId())
			.map(Store::getName)
			.orElse("데이터 없음");

		return ReviewResponse.of(review, reviewerName, storeName);
	}

	//3. 관리자 권한 리뷰 삭제(소프트 delete)
	@Transactional
	public void deleteReviewForAdmin(UUID reviewId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		review.softDelete();
		reviewRepository.save(review);
	}

	//4. 리뷰 숨김 처리
	@Transactional
	public void toggleReviewHidden(UUID reviewId, boolean hide) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		if (hide) {
			review.hide();
		} else {
			review.show();
		}

		reviewRepository.save(review);
	}
}