package com.ijaes.jeogiyo.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.dto.request.CreateReviewRequest;
import com.ijaes.jeogiyo.review.dto.request.UpdateReviewRequest;
import com.ijaes.jeogiyo.review.dto.response.CreateReviewResponse;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.review.event.ReviewEvent;
import com.ijaes.jeogiyo.review.repository.ReviewRepository;
import com.ijaes.jeogiyo.review.repository.ReviewRepositoryCustomImpl;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;

public class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private ReviewRepositoryCustomImpl reviewRepositoryCustomImpl;
	@Mock
	private Authentication authentication;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private ReviewService reviewService;

	private User user;
	private UUID userId;
	private UUID storeId;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		userId = UUID.randomUUID();
		storeId = UUID.randomUUID();

		user = User.builder()
			.id(userId)
			.username("test_user")
			.password("1234")
			.role(Role.USER)
			.build();

		when(authentication.getPrincipal()).thenReturn(user);
	}

	// 1-1. 리뷰 생성 성공
	@Test
	@DisplayName("리뷰 생성 성공 - 중복 작성 시도가 아닐 때")
	void createReview_success() {
		// given
		CreateReviewRequest request = new CreateReviewRequest(UUID.randomUUID(), storeId, "제목", "내용", 5);
		when(reviewRepository.existsByOrderId(any())).thenReturn(false);
		when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		CreateReviewResponse response = reviewService.createReview(authentication, request);
		System.out.println(response.getReviewId());
		System.out.println(response.getOrderId());
		System.out.println(response.getStoreId());
		System.out.println(response.getCreatedAt());

		// then
		assertThat(response).isNotNull();
		assertThat(response.getStoreId()).isEqualTo(storeId);
		verify(reviewRepository).save(any(Review.class));
		verify(eventPublisher).publishEvent(any(ReviewEvent.class));
	}

	//1-2. 리뷰 생성 실패
	@Test
	@DisplayName("리뷰 생성 실패 - 같은 주문으로 이미 리뷰가 존재할 때 예외 발생")
	void createReview_fail_duplicateOrder() {
		// given
		CreateReviewRequest request = new CreateReviewRequest(UUID.randomUUID(), storeId, "제목", "내용", 5);
		when(reviewRepository.existsByOrderId(any())).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(authentication, request))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.REVIEW_ALREADY_EXISTS.getMessage());
	}

	//2-1. 리뷰 단건 조회 성공
	@Test
	@DisplayName("리뷰 단건 조회 성공")
	void getReview_success() {
		UUID reviewId = UUID.randomUUID();
		Review review = Review.builder()
			.reviewId(reviewId)
			.storeId(storeId)
			.userId(userId)
			.title("테스트 제목")
			.content("테스트 내용")
			.rate(5)
			.build();

		Store store = Store.builder().id(storeId).name("테스트 가게").build();

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		ReviewResponse response = reviewService.getReview(authentication, reviewId);

		assertThat(response.getReviewerName()).isEqualTo("test_user");
		assertThat(response.getStoreName()).isEqualTo("테스트 가게");
		assertThat(response.getTitle()).isEqualTo("테스트 제목");
	}

	//2-2. 리뷰 조회 실패
	@Test
	@DisplayName("리뷰 조회 실패 - 리뷰가 존재하지 않을 때")
	void getReview_fail_notFound() {
		when(reviewRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewService.getReview(authentication, UUID.randomUUID()))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.RESOURCE_NOT_FOUND.getMessage());
	}

	// 3-1. 사용자별 리뷰 목록 조회 성공
	@Test
	@DisplayName("자신의 리뷰 목록을 조회할 수 있다")
	void getUserReviews_success() {
		Page<ReviewResponse> mockPage = new PageImpl<>(List.of(new ReviewResponse()));
		when(reviewRepositoryCustomImpl.findReviewsByUserId(userId, 0, 10)).thenReturn(mockPage);

		Page<ReviewResponse> result = reviewService.getUserReviews(authentication, userId, 0, 10);

		assertThat(result.getTotalElements()).isEqualTo(1);
		verify(reviewRepositoryCustomImpl).findReviewsByUserId(userId, 0, 10);
	}

	//3-2. 사용자별 리뷰 목록 조회 실패
	@Test
	@DisplayName("다른 사용자의 리뷰 목록은 조회할 수 없다")
	void getUserReviews_fail_accessDenied() {
		UUID anotherUserId = UUID.randomUUID();

		assertThatThrownBy(() -> reviewService.getUserReviews(authentication, anotherUserId, 0, 10))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
	}

	//4. 리뷰 수정
	@Test
	@DisplayName("리뷰 수정 성공 - 제목과 내용 수정")
	void updateReview_success() {
		UUID reviewId = UUID.randomUUID();
		Review review = Review.builder()
			.reviewId(reviewId)
			.userId(userId)
			.storeId(storeId)
			.title("old title")
			.content("old content")
			.rate(3)
			.build();

		UpdateReviewRequest request = new UpdateReviewRequest("new title", "new content", 5);
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(Store.builder().id(storeId).name("가게").build()));

		ReviewResponse response = reviewService.updateReview(authentication, reviewId, request);

		assertThat(response.getTitle()).isEqualTo("new title");
		assertThat(response.getRate()).isEqualTo(5);
		verify(reviewRepository).save(any(Review.class));
		verify(eventPublisher).publishEvent(any(ReviewEvent.class));
	}

	//5-1. 리뷰 삭제 성공
	@Test
	@DisplayName("리뷰 삭제 성공")
	void deleteReview_success() {
		UUID reviewId = UUID.randomUUID();
		Review review = Review.builder()
			.reviewId(reviewId)
			.userId(userId)
			.storeId(storeId)
			.title("삭제 전 제목")
			.content("삭제 전 내용")
			.build();

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		reviewService.deleteReview(authentication, reviewId);

		assertThat(review.isDeleted()).isTrue();
		verify(reviewRepository).save(any(Review.class));
		verify(eventPublisher).publishEvent(any(ReviewEvent.class));
	}

	//5-2. 리뷰 삭제 실패
	@Test
	@DisplayName("리뷰 삭제 실패 - 작성자가 아닐 경우")
	void deleteReview_fail_accessDenied() {
		UUID reviewId = UUID.randomUUID();
		Review review = Review.builder()
			.reviewId(reviewId)
			.userId(UUID.randomUUID()) // 다른 유저
			.storeId(storeId)
			.build();

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		assertThatThrownBy(() -> reviewService.deleteReview(authentication, reviewId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
	}
}
