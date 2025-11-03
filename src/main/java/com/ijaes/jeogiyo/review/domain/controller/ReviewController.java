package com.ijaes.jeogiyo.review.domain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.review.domain.service.ReviewService;
import com.ijaes.jeogiyo.review.infrastructure.persistence.dto.request.CreateReviewRequest;
import com.ijaes.jeogiyo.review.infrastructure.persistence.dto.response.CreateReviewResponse;

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
}
