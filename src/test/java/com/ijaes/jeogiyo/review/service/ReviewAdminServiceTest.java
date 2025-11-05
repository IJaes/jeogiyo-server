package com.ijaes.jeogiyo.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.review.repository.ReviewRepository;
import com.ijaes.jeogiyo.review.repository.ReviewRepositoryCustomImpl;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;

public class ReviewAdminServiceTest {

	@Mock
	private ReviewRepository reviewRepository;
	@Mock
	private ReviewRepositoryCustomImpl reviewRepositoryCustomImpl;
	@Mock
	private UserRepository userRepository;
	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private ReviewAdminService reviewAdminService;

	private UUID reviewId;
	private UUID userId;
	private UUID storeId;

	private User user;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		reviewId = UUID.randomUUID();
		userId = UUID.randomUUID();
		storeId = UUID.randomUUID();

		user = User.builder()
			.id(userId)
			.username("admin_user")
			.role(Role.MANAGER)
			.build();
	}

	// 1. 전체 리뷰 조회
	@Test
	@DisplayName("관리자 전체 리뷰 조회 성공")
	void getAllReviewsForAdmin_success() {
		Page<ReviewResponse> mockPage = new PageImpl<>(List.of(new ReviewResponse()));
		when(reviewRepositoryCustomImpl.findAllReviewsForAdmin(0, 10)).thenReturn(mockPage);

		Page<ReviewResponse> result = reviewAdminService.getAllReviewsForAdmin(0, 10);

		assertThat(result.getTotalElements()).isEqualTo(1);
		verify(reviewRepositoryCustomImpl).findAllReviewsForAdmin(0, 10);
	}

	// 2-1. 특정 리뷰 조회 성공
	@Test
	@DisplayName("관리자 특정 리뷰 조회 성공")
	void getReviewForAdmin_success() {
		Review review = Review.builder()
			.reviewId(reviewId)
			.userId(userId)
			.storeId(storeId)
			.title("테스트 제목")
			.content("테스트 내용")
			.rate(5)
			.build();

		Store store = Store.builder().id(storeId).name("테스트 가게").build();

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		ReviewResponse response = reviewAdminService.getReviewForAdmin(reviewId);

		assertThat(response.getReviewerName()).isEqualTo("admin_user");
		assertThat(response.getStoreName()).isEqualTo("테스트 가게");
		assertThat(response.getTitle()).isEqualTo("테스트 제목");
	}

	// 2-2. 특정 리뷰 조회 실패
	@Test
	@DisplayName("관리자 특정 리뷰 조회 실패 - 존재하지 않는 리뷰")
	void getReviewForAdmin_fail_notFound() {
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewAdminService.getReviewForAdmin(reviewId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());
	}

	// 3-1. 관리자 리뷰 삭제 성공
	@Test
	@DisplayName("관리자 리뷰 삭제 성공")
	void deleteReviewForAdmin_success() {
		Review review = Review.builder()
			.reviewId(reviewId)
			.build();

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		reviewAdminService.deleteReviewForAdmin(reviewId);

		assertThat(review.isDeleted()).isTrue(); // soft delete
		verify(reviewRepository).save(review);
	}

	// 3-2. 관리자 리뷰 삭제 실패
	@Test
	@DisplayName("관리자 리뷰 삭제 실패 - 리뷰 존재하지 않음")
	void deleteReviewForAdmin_fail_notFound() {
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewAdminService.deleteReviewForAdmin(reviewId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());
	}

	// 4-1. 리뷰 숨김 처리
	@Test
	@DisplayName("관리자 리뷰 숨김 처리 성공")
	void toggleReviewHidden_hide_success() {
		Review review = Review.builder()
			.reviewId(reviewId)
			.build();

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		reviewAdminService.toggleReviewHidden(reviewId, true);

		assertThat(review.isHidden()).isTrue();
		verify(reviewRepository).save(review);
	}

	// 4-2. 리뷰 숨김 해제
	@Test
	@DisplayName("관리자 리뷰 숨김 해제 성공")
	void toggleReviewHidden_show_success() {
		Review review = Review.builder()
			.reviewId(reviewId)
			.build();

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		reviewAdminService.toggleReviewHidden(reviewId, false);

		assertThat(review.isHidden()).isFalse();
		verify(reviewRepository).save(review);
	}

	// 4-3. 리뷰 숨김 처리 실패
	@Test
	@DisplayName("관리자 리뷰 숨김 처리 실패 - 리뷰 존재하지 않음")
	void toggleReviewHidden_fail_notFound() {
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewAdminService.toggleReviewHidden(reviewId, true))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());
	}
}
