package com.ijaes.jeogiyo.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.ReviewSortType;
import com.ijaes.jeogiyo.review.service.ReviewOwnerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/owner/reviews")
@RequiredArgsConstructor
@Tag(name = "사장님")
public class ReviewOwnerController {

	private final ReviewOwnerService reviewOwnerService;

	@GetMapping
	@Operation(summary = "리뷰 조회", description = "본인 매장에 등록된 리뷰를 조회합니다", security = @SecurityRequirement(name = "bearer-jwt"))
	public ResponseEntity<Page<ReviewResponse>> getMyStoreReviews(
		Authentication authentication,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@Parameter(
			description = "정렬 기준 타입. 입력하지 않으면 최신순",
			schema = @Schema(implementation = ReviewSortType.class)
		)
		@RequestParam(defaultValue = "LATEST") String sortType
	) {
		Page<ReviewResponse> response = reviewOwnerService.getMyStoreReviews(authentication, page, size, sortType);
		return ResponseEntity.ok(response);
	}
}
