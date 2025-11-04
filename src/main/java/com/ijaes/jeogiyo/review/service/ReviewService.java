package com.ijaes.jeogiyo.review.service;

import java.util.List;
import java.util.UUID;

import org.springdoc.api.AbstractOpenApiResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.dto.request.CreateReviewRequest;
import com.ijaes.jeogiyo.review.dto.request.UpdateReviewRequest;
import com.ijaes.jeogiyo.review.dto.response.CreateReviewResponse;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.review.repository.ReviewRepository;
import com.ijaes.jeogiyo.review.repository.ReviewRepositoryCustomImpl;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final ReviewRepositoryCustomImpl reviewRepositoryCustomImpl;
	private final AbstractOpenApiResource abstractOpenApiResource;

	//1. 리뷰 생성
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

	//2. 리뷰 단건 조회
	public ReviewResponse getReview(Authentication authentication, UUID reviewId) {
		//리뷰 조회
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

		//리뷰 작성자 이름 조회(리뷰에 저장된 userid 사용해서 username 가져오기)
		User reviewer = userRepository.findById(review.getUserId()).orElse(null);

		//리뷰 작성된 가게 이름 조회(리뷰에 저장된 storeid 사용해서 가게의 name 가져오기)
		String storeName = storeRepository.findById(review.getStoreId())
			.map(Store::getName)
			.orElse("폐점된 가게");

		//리뷰 숨김 여부 판단
		boolean isHiddenByAdmin = review.isHidden();
		boolean isHiddenByBlock = reviewer != null && reviewer.getRole().equals(Role.BLOCK);

		//관리자에 의해 숨겨진 리뷰일 경우
		if (isHiddenByAdmin || isHiddenByBlock) {

			String hiddenReviewerName = (reviewer != null && reviewer.getRole().equals(Role.BLOCK))
				? "차단된 사용자"
				: "숨김 처리된 사용자";

			return ReviewResponse.builder()
				.reviewId(review.getReviewId())
				.orderId(review.getOrderId())
				.storeId(review.getStoreId())
				.reviewerName(hiddenReviewerName)
				.storeName(storeName)
				.title("관리 정책 위반으로 내용이 숨겨졌습니다.")
				.content("이 리뷰는 관리 정책 위반 혹은 관리자의 결정에 따라 내용이 공개되지 않습니다.")
				.rate(0)
				.createdAt(review.getCreatedAt())
				.updatedAt(review.getUpdatedAt())
				.build();
		}

		//리뷰어가 없을 때는 탈퇴한 사용자
		String reviewerName = (reviewer == null) ? "탈퇴한 사용자" : reviewer.getUsername();

		return ReviewResponse.of(review, reviewerName, storeName);
	}

	//3. 사용자별 리뷰 전체 목록 조회(사용자가 자신이 작성한 리뷰 목록 조회)
	public Page<ReviewResponse> getUserReviews(Authentication authentication, UUID userId, int page, int size) {
		UUID currentUserId = ((User)authentication.getPrincipal()).getId();
		String currentUserName = ((User)authentication.getPrincipal()).getName();

		//자신이 작성한 리뷰 목록만 조회 가능
		if (!currentUserId.equals(userId)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		Page<Review> reviewPage = reviewRepositoryCustomImpl.findReviewsByUserId(userId, page, size);

		List<ReviewResponse> reviews = reviewPage.getContent().stream()
			.map(review -> {

				//작성자 이름은 현재 유저 이름으로
				String reviewerName = currentUserName;

				//매장 이름 조회하기
				String storeName = storeRepository.findById(review.getStoreId())
					.map(Store::getName)
					.orElse("폐점된 가게");

				return ReviewResponse.of(review, reviewerName, storeName);
			}).toList();

		return new PageImpl<>(
			reviews, reviewPage.getPageable(), reviewPage.getTotalElements()
		);
	}

	//4. 가게별 리뷰 전체 목록 조회
	public Page<ReviewResponse> getStoreReviews(Authentication authentication, UUID storeId, int page, int size) {
		//db에서 페이지 데이터 조회
		Page<Review> reviewPage = reviewRepositoryCustomImpl.findReviewsByStoreID(storeId, page, size);

		List<ReviewResponse> reviews = reviewPage.getContent().stream()
			.map(review -> {
				//각 리뷰별 작성자 이름 가져오기
				String reviewerName = userRepository.findById(review.getUserId())
					.map(User::getUsername)
					.orElse("탈퇴한 사용자");

				//매장 이름 가져오기(한 매장에 대한 리뷰이므로 동일)
				String storeName = storeRepository.findById(storeId)
					.map(Store::getName)
					.orElse("폐점된 가게");

				return ReviewResponse.of(review, reviewerName, storeName);
			}).toList();

		return new PageImpl<>(
			reviews, reviewPage.getPageable(), reviewPage.getTotalElements()
		);
	}

	//5. 리뷰 수정
	public ReviewResponse updateReview(Authentication authentication, UUID reviewId,
		UpdateReviewRequest request) {
		UUID currentUserId = ((User)authentication.getPrincipal()).getId();

		//리뷰 있는지 확인
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

		//작성자 본인인지 확인
		if (!review.getUserId().equals(currentUserId)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		//데이터 업데이트
		if (request.getTitle() != null) {
			review.updateTitle(request.getTitle());
		}
		if (request.getContent() != null) {
			review.updateContent(request.getContent());
		}
		if (request.getRate() != null) {
			review.updateRate(request.getRate());

			//여기에 가게 평균 평점을 재계산하는 이벤트 처리
		}

		reviewRepository.save(review);

		String reviewerName = ((User)authentication.getPrincipal()).getName();

		String storeName = storeRepository.findById(review.getStoreId())
			.map(Store::getName)
			.orElse("폐점된 가게");

		return ReviewResponse.of(review, reviewerName, storeName);
	}

	//6. 리뷰 삭제
	public void deleteReview(Authentication authentication, UUID reviewId) {
		UUID currentUserId = ((User)authentication.getPrincipal()).getId();

		//리뷰 있는지 확인
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

		//작성자 본인인지 확인
		if (!review.getUserId().equals(currentUserId)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		//삭제
		reviewRepository.delete(review);

		//여기도 가게 평점 재계산 이벤트 처리
	}
}
