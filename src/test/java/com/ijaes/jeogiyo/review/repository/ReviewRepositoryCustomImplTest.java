package com.ijaes.jeogiyo.review.repository;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@DataJpaTest
public class ReviewRepositoryCustomImplTest {

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ReviewRepositoryCustom reviewRepositoryCustom;

	// 테스트용 데이터
	private final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID BLOCKED_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private final UUID TEST_STORE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private final UUID OTHER_STORE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private final UUID TEST_ORDER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

	private UUID softDeletedReviewId;

	@Configuration
	static class TestQuerydslConfig {
		@Autowired
		EntityManager em;

		@Bean
		public JPAQueryFactory queryFactory() {
			return new JPAQueryFactory(em);
		}
	}

	@BeforeEach
	public void setup() {
		// 데이터 정리 및 초기화
		reviewRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();

		// --- User 및 Store 데이터 생성 ---
		User normalUser = User.builder()
			.id(TEST_USER_ID)
			.username("normal_user")
			.password("pass")
			.role(Role.USER)
			.build();
		User blockedUser = User.builder()
			.id(BLOCKED_USER_ID)
			.username("blocked_user")
			.password("pass")
			.role(Role.BLOCK)
			.build();
		userRepository.saveAll(List.of(normalUser, blockedUser));

		// Store 엔티티가 있다면 StoreRepository를 통해 TEST_STORE_ID도 저장해야 합니다.
		// storeRepository.save(...);

		// --- Review 데이터 생성 ---

		// 1. 노출될 리뷰 2개 (TEST_STORE_ID, isHidden=false, isDeleted=false)
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(TEST_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("Visible 1")
			.rate(5)
			.build());
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(TEST_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("Visible 2")
			.rate(4)
			.build());

		// 2. 숨겨진 리뷰 1개
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(TEST_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("Hidden Review")
			.rate(1)
			.isHidden(true)
			.build());

		// 3. 차단된 사용자의 리뷰 1개
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(BLOCKED_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("Blocked Review")
			.rate(1)
			.build());

		// 4. 소프트 삭제된 리뷰 1개 (단건 테스트용)
		Review deletedReview = Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(TEST_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("Soft Deleted")
			.rate(3)
			.build();
		deletedReview.softDelete(); // 삭제 상태로 변경
		reviewRepository.save(deletedReview);
		softDeletedReviewId = deletedReview.getReviewId(); // ID 저장
	}

	@Test
	@DisplayName("R-1: 관리자 전체 조회는 모든 상태의 리뷰를 포함해야 한다")
	void findAllReviewsForAdmin_shouldIncludeAllStatusReviews() {
		// given: setUp에서 총 5개의 리뷰 (2정상, 1숨김, 1차단, 1다른가게) 저장됨
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

		// when
		Page<ReviewResponse> resultPage = reviewRepositoryCustom.findAllReviewsForAdmin(0, 10);

		// then
		// 모든 리뷰 5개가 조회되어야 함 (소프트 삭제된 리뷰가 있다면 그것까지 포함)
		assertThat(resultPage.getTotalElements()).as("총 5개의 리뷰가 조회되어야 한다").isEqualTo(5);
		assertThat(resultPage.getContent()).as("데이터 개수도 5개여야 한다").hasSize(5);

		// 데이터의 유효성 검증 (예: 숨겨진 리뷰의 isHidden 필드가 true인지)
		assertThat(resultPage.getContent())
			.filteredOn(r -> r.getContent().contains("숨김리뷰"))
			.extracting(ReviewResponse::getIsHidden)
			.containsOnly(true);
	}
}
