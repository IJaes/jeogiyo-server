package com.ijaes.jeogiyo.review.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.review.dto.request.CreateReviewRequest;
import com.ijaes.jeogiyo.review.dto.request.UpdateReviewRequest;
import com.ijaes.jeogiyo.review.dto.response.CreateReviewResponse;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰", description = "리뷰 관련 API")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("")
	@Operation(summary = "새 리뷰 작성", description = "새로운 리뷰를 작성합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<CreateReviewResponse> createReview(Authentication authentication,
		@Valid @RequestBody CreateReviewRequest request) {
		CreateReviewResponse response = reviewService.createReview(authentication, request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{reviewId}")
	@Operation(summary = "리뷰 단건 조회", description = "특정 id에 해당하는 리뷰를 조회합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<ReviewResponse> getReview(Authentication authentication, @PathVariable UUID reviewId) {
		ReviewResponse response = reviewService.getReview(authentication, reviewId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{userId}")
	@Operation(summary = "사용자별 리뷰 목록 조회", description = "특정 id가 작성한 전체 리뷰 목록을 조회합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Page<ReviewResponse>> getUserReviews(Authentication authentication,
		@PathVariable UUID userId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Page<ReviewResponse> response = reviewService.getUserReviews(authentication, userId, page, size);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{storeId}")
	@Operation(summary = "가게별 리뷰 목록 조회", description = "특정 store에 작성된 전체 리뷰 목록을 조회합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Page<ReviewResponse>> getStoreReviews(Authentication authentication,
		@PathVariable UUID storeId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Page<ReviewResponse> response = reviewService.getStoreReviews(authentication, storeId, page, size);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{reviewId}")
	@Operation(summary = "리뷰 수정", description = "작성자가 본인의 리뷰를 수정합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<ReviewResponse> updateReview(Authentication authentication,
		@PathVariable UUID reviewId,
		@Valid @RequestBody UpdateReviewRequest request
	) {
		ReviewResponse response = reviewService.updateReview(authentication, reviewId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{reviewId}")
	@Operation(summary = "리뷰 삭제", description = "작성자가 본인의 리뷰를 삭제합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Void> deleteReview(Authentication authentication, @PathVariable UUID reviewId) {
		reviewService.deleteReview(authentication, reviewId);
		return ResponseEntity.noContent().build();
	}
}
