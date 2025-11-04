package com.ijaes.jeogiyo.review.repository;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
import com.ijaes.jeogiyo.user.repository.UserRepository;

@DataJpaTest
public class ReviewRepositoryTest {

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	UserRepository userRepository;

	// 테스트에 필요한 공통 UUID 정의 (클래스 레벨에서 정의했다고 가정)
	private final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID BLOCKED_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private final UUID TEST_STORE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private final UUID OTHER_STORE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private final UUID TEST_ORDER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

	//테스트 데이터 초기화
	@BeforeEach
	void setUp() {
		// 1. 데이터베이스 정리 (매번 깨끗한 상태로 시작)
		reviewRepository.deleteAll();
		userRepository.deleteAll();

		// 2. 테스트용 User 객체 생성 및 저장 (FK 제약조건 만족)
		// 2-1. 정상 사용자 (ROLE.USER)
		User normalUser = User.builder()
			.id(TEST_USER_ID)
			.username("normal_user")
			.password("pass")
			.name("정상")
			.role(Role.USER)
			.build();
		userRepository.save(normalUser);

		// 2-2. 차단된 사용자 (ROLE.BLOCK)
		User blockedUser = User.builder()
			.id(BLOCKED_USER_ID)
			.username("blocked_user")
			.password("pass")
			.name("차단")
			.role(Role.BLOCK)
			.build();
		userRepository.save(blockedUser);

		// 3. 테스트용 Review 객체 생성 및 저장

		// A. 정상적인 리뷰 2개 (TEST_STORE_ID에 포함)
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(TEST_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("굿리뷰1")
			.title("제목1")
			.rate(5)
			.build());
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(TEST_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("굿리뷰2")
			.title("제목2")
			.rate(4)
			.build());

		// B. 숨겨진 리뷰 1개 (관리자 숨김 테스트용)
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(TEST_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("숨김리뷰")
			.title("관리자")
			.rate(1)
			.isHidden(true)
			.build());

		// C. 차단된 사용자의 리뷰 1개 (차단 사용자 제외 테스트용)
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(BLOCKED_USER_ID)
			.storeId(TEST_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("차단리뷰")
			.title("차단")
			.rate(1)
			.build());

		// D. 다른 가게 리뷰 1개 (필터링 테스트용)
		reviewRepository.save(Review.builder()
			.reviewId(UUID.randomUUID())
			.userId(TEST_USER_ID)
			.storeId(OTHER_STORE_ID)
			.orderId(UUID.randomUUID())
			.content("다른가게")
			.title("다른")
			.rate(5)
			.build());
	}

	@Test
	@DisplayName("R-1: 주문 ID로 리뷰 존재 여부를 정확히 확인해야 한다")
	void existsByOrderId_shouldReturnTrueWhenExists() {
		// given
		Review review = Review.builder()
			.reviewId(UUID.randomUUID())
			.orderId(TEST_ORDER_ID)
			.userId(TEST_USER_ID)
			.storeId(TEST_STORE_ID)
			.content("테스트 내용").title("테스트 제목").rate(5)
			.build();
		reviewRepository.save(review);

		// when
		boolean exists = reviewRepository.existsByOrderId(TEST_ORDER_ID);
		boolean notExists = reviewRepository.existsByOrderId(UUID.randomUUID()); // 존재하지 않는 ID

		// then
		assertThat(exists).isTrue();
		assertThat(notExists).isFalse();
	}
}
