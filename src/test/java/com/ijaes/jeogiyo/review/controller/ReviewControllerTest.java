package com.ijaes.jeogiyo.review.controller;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.security.core.Authentication;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.dto.request.CreateReviewRequest;
import com.ijaes.jeogiyo.review.dto.request.UpdateReviewRequest;
import com.ijaes.jeogiyo.review.dto.response.CreateReviewResponse;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.service.ReviewService;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController 테스트")
class ReviewControllerTest {

	@Mock
	private ReviewService reviewService;

	@InjectMocks
	private ReviewController reviewController;

	@Mock
	private Authentication authentication;

	private UUID userId;
	private UUID storeId;
	private UUID reviewId;
	private User user;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		storeId = UUID.randomUUID();
		reviewId = UUID.randomUUID();

		user = User.builder()
			.id(userId)
			.username("test_user")
			.role(Role.USER)
			.build();
	}

	@Test
	@DisplayName("리뷰 생성 성공")
	void createReview_success() {
		CreateReviewRequest request = new CreateReviewRequest(UUID.randomUUID(), storeId, "제목", "내용", 5);
		CreateReviewResponse responseMock = new CreateReviewResponse(UUID.randomUUID(), request.getOrderId(), storeId,
			null);

		when(reviewService.createReview(authentication, request)).thenReturn(responseMock);

		ResponseEntity<CreateReviewResponse> response = reviewController.createReview(authentication, request);

		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(responseMock.getReviewId(), response.getBody().getReviewId());
		verify(reviewService, times(1)).createReview(authentication, request);
	}

	@Test
	@DisplayName("리뷰 생성 실패 - 중복 리뷰")
	void createReview_fail_duplicate() {
		CreateReviewRequest request = new CreateReviewRequest(UUID.randomUUID(), storeId, "제목", "내용", 5);

		when(reviewService.createReview(authentication, request))
			.thenThrow(new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS));

		assertThatThrownBy(() -> reviewController.createReview(authentication, request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.REVIEW_ALREADY_EXISTS.getMessage());

		verify(reviewService, times(1)).createReview(authentication, request);
	}

	@Test
	@DisplayName("리뷰 단건 조회 성공")
	void getReview_success() {
		ReviewResponse responseMock = ReviewResponse.builder()
			.reviewId(reviewId)
			.storeId(storeId)
			.title("테스트 제목")
			.content("테스트 내용")
			.rate(5)
			.reviewerName("test_user")
			.storeName("테스트 가게")
			.build();

		when(reviewService.getReview(reviewId)).thenReturn(responseMock);

		ResponseEntity<ReviewResponse> response = reviewController.getReview(reviewId);

		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("테스트 제목", response.getBody().getTitle());
		verify(reviewService, times(1)).getReview(reviewId);
	}

	@Test
	@DisplayName("리뷰 단건 조회 실패 - 리뷰가 존재하지 않을 때")
	void getReview_fail_notFound() {
		when(reviewService.getReview(reviewId))
			.thenThrow(new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

		CustomException exception = assertThrows(CustomException.class, () ->
			reviewController.getReview(reviewId)
		);

		assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
		verify(reviewService, times(1)).getReview(reviewId);
	}

	@Test
	@DisplayName("사용자별 리뷰 목록 조회 성공")
	void getUserReviews_success() {
		Page<ReviewResponse> mockPage = new PageImpl<>(List.of(new ReviewResponse()));
		when(reviewService.getUserReviews(authentication, userId, 0, 10, "ALL", "LATEST")).thenReturn(mockPage);

		ResponseEntity<Page<ReviewResponse>> response = reviewController.getUserReviews(authentication, userId, 0, 10,
			"ALL", "LATEST");

		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().getTotalElements());
		verify(reviewService, times(1)).getUserReviews(authentication, userId, 0, 10, "ALL", "LATEST");
	}

	@Test
	@DisplayName("사용자별 리뷰 조회 실패 - 본인이 아닐 경우")
	void getUserReviews_fail_accessDenied() {
		UUID anotherUserId = UUID.randomUUID();
		when(reviewService.getUserReviews(authentication, anotherUserId, 0, 10, "ALL", "LATEST"))
			.thenThrow(new CustomException(ErrorCode.ACCESS_DENIED));

		assertThatThrownBy(() -> reviewController.getUserReviews(authentication, anotherUserId, 0, 10, "ALL", "LATEST"))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());

		verify(reviewService, times(1)).getUserReviews(authentication, anotherUserId, 0, 10, "ALL", "LATEST");
	}

	@Test
	@DisplayName("가게별 리뷰 목록 조회 성공")
	void getStoreReviews_success() {
		Page<ReviewResponse> mockPage = new PageImpl<>(List.of(new ReviewResponse()));
		when(reviewService.getStoreReviews(storeId, 0, 10, "")).thenReturn(mockPage);

		ResponseEntity<Page<ReviewResponse>> response = reviewController.getStoreReviews(storeId, 0,
			10, "");

		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals(1, response.getBody().getTotalElements());
		verify(reviewService, times(1)).getStoreReviews(storeId, 0, 10, "");
	}

	@Test
	@DisplayName("리뷰 수정")
	void updateReview_success() {
		UpdateReviewRequest request = new UpdateReviewRequest("new title", "new content", 5);
		ReviewResponse responseMock = ReviewResponse.builder()
			.reviewId(reviewId)
			.title("new title")
			.content("new content")
			.rate(5)
			.build();

		when(reviewService.updateReview(authentication, reviewId, request)).thenReturn(responseMock);

		ResponseEntity<ReviewResponse> response = reviewController.updateReview(authentication, reviewId, request);

		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());
		assertEquals("new title", response.getBody().getTitle());
		verify(reviewService, times(1)).updateReview(authentication, reviewId, request);
	}

	@Test
	@DisplayName("리뷰 삭제 성공")
	void deleteReview_success() {
		doNothing().when(reviewService).deleteReview(authentication, reviewId);

		ResponseEntity<Void> response = reviewController.deleteReview(authentication, reviewId);

		assertNotNull(response);
		assertEquals(204, response.getStatusCodeValue());
		verify(reviewService, times(1)).deleteReview(authentication, reviewId);
	}

	@Test
	@DisplayName("리뷰 삭제 실패 - 작성자가 아닐 경우")
	void deleteReview_fail_accessDenied() {
		doThrow(new CustomException(ErrorCode.ACCESS_DENIED))
			.when(reviewService).deleteReview(authentication, reviewId);

		assertThatThrownBy(() -> reviewController.deleteReview(authentication, reviewId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());

		verify(reviewService, times(1)).deleteReview(authentication, reviewId);
	}
}
