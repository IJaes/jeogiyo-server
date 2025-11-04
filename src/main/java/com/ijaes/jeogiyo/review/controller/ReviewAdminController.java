package com.ijaes.jeogiyo.review.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.service.ReviewAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 관련 관리자", description = "관리자 권한이 필요한 리뷰 API")
public class ReviewAdminController {

	private final ReviewAdminService reviewAdminService;

	@GetMapping("")
	@Operation(summary = "전체 리뷰 조회", description = "관리자 권한으로 작성된 리뷰 전체를 조회합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	@PreAuthorize("hasRole('MANAGER')")
	public ResponseEntity<Page<ReviewResponse>> getAllReviews(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Page<ReviewResponse> response = reviewAdminService.getAllReviewsForAdmin(page, size);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{reviewId}")
	@Operation(summary = "리뷰 단건 조회", description = "관리자 권한으로 리뷰 하나를 조회합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	@PreAuthorize("hasRole('MANAGER')")
	public ResponseEntity<ReviewResponse> getReview(
		@PathVariable UUID reviewId
	) {
		ReviewResponse response = reviewAdminService.getReviewForAdmin(reviewId);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{reviewId}")
	@Operation(summary = "리뷰 삭제", description = "관리자 권한으로 리뷰 하나를 삭제합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	@PreAuthorize("hasRole('MANAGER')")
	public ResponseEntity<Void> deleteReview(
		@PathVariable UUID reviewId
	) {
		reviewAdminService.deleteReviewForAdmin(reviewId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{reviewId}/hide")
	@Operation(summary = "리뷰 숨김 처리", description = "관리자 권한으로 리뷰를 숨김 처리 합니다.", security = @SecurityRequirement(name = "bearer-jwt"))
	@PreAuthorize("hasRole('MANAGER')")
	public ResponseEntity<Void> hideReview(
		@PathVariable UUID reviewId,
		@RequestParam boolean hide
	) {
		reviewAdminService.toggleReviewHidden(reviewId, hide);
		return ResponseEntity.noContent().build();
	}
}