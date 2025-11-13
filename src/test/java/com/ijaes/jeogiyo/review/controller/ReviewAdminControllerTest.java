package com.ijaes.jeogiyo.review.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.service.ReviewAdminService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewAdminController 테스트")
class ReviewAdminControllerTest {

	@Mock
	private ReviewAdminService reviewAdminService;

	@InjectMocks
	private ReviewAdminController reviewAdminController;

	private UUID reviewId;

	@BeforeEach
	void setUp() {
		reviewId = UUID.randomUUID();
	}

	@Test
	@DisplayName("전체 리뷰 조회 성공")
	void getAllReviews_success() {
		Page<ReviewResponse> pageMock = new PageImpl<>(List.of(new ReviewResponse()));
		when(reviewAdminService.getAllReviewsForAdmin(0, 10, "")).thenReturn(pageMock);

		ResponseEntity<Page<ReviewResponse>> response = reviewAdminController.getAllReviews(0, 10, "");

		assertThat(response.getBody()).isEqualTo(pageMock);
		verify(reviewAdminService, times(1)).getAllReviewsForAdmin(0, 10, "");
	}

	@Test
	@DisplayName("리뷰 단건 조회 성공")
	void getReview_success() {
		ReviewResponse responseMock = new ReviewResponse();
		when(reviewAdminService.getReviewForAdmin(reviewId)).thenReturn(responseMock);

		ResponseEntity<ReviewResponse> response = reviewAdminController.getReview(reviewId);

		assertThat(response.getBody()).isEqualTo(responseMock);
		verify(reviewAdminService, times(1)).getReviewForAdmin(reviewId);
	}

	@Test
	@DisplayName("리뷰 단건 조회 실패 - 존재하지 않는 리뷰")
	void getReview_fail_notFound() {
		when(reviewAdminService.getReviewForAdmin(reviewId))
			.thenThrow(new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		assertThatThrownBy(() -> reviewAdminController.getReview(reviewId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());

		verify(reviewAdminService, times(1)).getReviewForAdmin(reviewId);
	}

	@Test
	@DisplayName("리뷰 삭제 성공")
	void deleteReview_success() {
		ResponseEntity<Void> response = reviewAdminController.deleteReview(reviewId);

		assertThat(response.getStatusCodeValue()).isEqualTo(204);
		verify(reviewAdminService, times(1)).deleteReviewForAdmin(reviewId);
	}

	@Test
	@DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰")
	void deleteReview_fail_notFound() {
		doThrow(new CustomException(ErrorCode.REVIEW_NOT_FOUND))
			.when(reviewAdminService).deleteReviewForAdmin(reviewId);

		assertThatThrownBy(() -> reviewAdminController.deleteReview(reviewId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());

		verify(reviewAdminService, times(1)).deleteReviewForAdmin(reviewId);
	}

	@Test
	@DisplayName("리뷰 숨김 처리 성공 - 숨김")
	void hideReview_success_hide() {
		ResponseEntity<Void> response = reviewAdminController.hideReview(reviewId, true);

		assertThat(response.getStatusCodeValue()).isEqualTo(204);
		verify(reviewAdminService, times(1)).toggleReviewHidden(reviewId, true);
	}

	@Test
	@DisplayName("리뷰 숨김 처리 성공 - 숨김 해제")
	void hideReview_success_show() {
		ResponseEntity<Void> response = reviewAdminController.hideReview(reviewId, false);

		assertThat(response.getStatusCodeValue()).isEqualTo(204);
		verify(reviewAdminService, times(1)).toggleReviewHidden(reviewId, false);
	}

	@Test
	@DisplayName("리뷰 숨김 처리 실패 - 존재하지 않는 리뷰")
	void hideReview_fail_notFound() {
		doThrow(new CustomException(ErrorCode.REVIEW_NOT_FOUND))
			.when(reviewAdminService).toggleReviewHidden(reviewId, true);

		assertThatThrownBy(() -> reviewAdminController.hideReview(reviewId, true))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());

		verify(reviewAdminService, times(1)).toggleReviewHidden(reviewId, true);
	}
}
