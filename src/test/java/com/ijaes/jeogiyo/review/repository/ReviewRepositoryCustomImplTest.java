package com.ijaes.jeogiyo.review.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import com.ijaes.jeogiyo.common.config.QueryDslConfig;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import({ReviewRepositoryCustomImpl.class, QueryDslConfig.class})
class ReviewRepositoryCustomImplTest {

	@Autowired
	private EntityManager em;

	private ReviewRepositoryCustomImpl reviewRepositoryCustom;

	private UUID userId;
	private UUID storeId;

	@BeforeEach
	void setUp() {
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		reviewRepositoryCustom = new ReviewRepositoryCustomImpl(queryFactory);

		// given: 유저 생성
		User user = User.builder()
			.id(UUID.randomUUID())
			.username("테스트유저")
			.role(Role.USER)
			.build();
		em.persist(user);
		userId = user.getId();

		// given: 가게 생성
		Store store = Store.builder()
			.id(UUID.randomUUID())
			.name("테스트가게")
			.build();
		em.persist(store);
		storeId = store.getId();

		// given: 리뷰 3개 생성
		for (int i = 1; i <= 3; i++) {
			Review review = Review.builder()
				.reviewId(UUID.randomUUID())
				.orderId(UUID.randomUUID())
				.storeId(storeId)
				.userId(userId)
				.title("리뷰 제목 " + i)
				.content("리뷰 내용 " + i)
				.rate(5)
				.isHidden(false)
				.build();
			em.persist(review);
		}

		em.flush();
		em.clear();
	}

	@Test
	@DisplayName("관리자는 전체 리뷰를 최신순으로 조회할 수 있다")
	void findAllReviewsForAdmin() {
		// when
		Page<ReviewResponse> result = reviewRepositoryCustom.findAllReviewsForAdmin(0, 10);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(3);
		assertThat(result.getContent().get(0).getTitle()).isEqualTo("리뷰 제목 3");
	}

	@Test
	@DisplayName("특정 유저의 리뷰를 최신순으로 조회할 수 있다")
	void findReviewsByUserId() {
		// when
		Page<ReviewResponse> result = reviewRepositoryCustom.findReviewsByUserId(userId, 0, 10);

		// then
		assertThat(result.getTotalElements()).isEqualTo(3);
		assertThat(result.getContent().get(0).getReviewerName()).isEqualTo("테스트유저");
	}

	@Test
	@DisplayName("가게별 리뷰 조회 시 숨김, 삭제, 차단된 사용자의 리뷰는 제외된다")
	void findReviewsByStoreID() {
		// given: 숨김 처리된 리뷰 추가
		Review hiddenReview = Review.builder()
			.reviewId(UUID.randomUUID())
			.orderId(UUID.randomUUID())
			.storeId(storeId)
			.userId(userId)
			.title("숨김 리뷰")
			.content("숨김된 리뷰 내용")
			.rate(4)
			.isHidden(true)
			.build();
		em.persist(hiddenReview);

		em.flush();
		em.clear();

		// when
		Page<ReviewResponse> result = reviewRepositoryCustom.findReviewsByStoreID(storeId, 0, 10);

		// then
		assertThat(result.getTotalElements()).isEqualTo(3); // 숨김 리뷰 제외됨
		assertThat(result.getContent())
			.noneMatch(r -> r.getTitle().equals("숨김 리뷰"));
	}

	@Test
	@DisplayName("차단된 사용자의 리뷰는 가게 리뷰 조회에서 제외된다")
	void findReviewsByStoreID_ExcludeBlockedUser() {
		// given: 차단된 유저
		User blockedUser = User.builder()
			.id(UUID.randomUUID())
			.username("차단유저")
			.role(Role.BLOCK)
			.build();
		em.persist(blockedUser);

		// 해당 차단 유저의 리뷰 생성
		Review blockedReview = Review.builder()
			.reviewId(UUID.randomUUID())
			.orderId(UUID.randomUUID())
			.storeId(storeId)
			.userId(blockedUser.getId())
			.title("차단된 리뷰")
			.content("이건 보이면 안됨")
			.rate(1)
			.isHidden(false)
			.build();
		em.persist(blockedReview);

		em.flush();
		em.clear();

		// when
		Page<ReviewResponse> result = reviewRepositoryCustom.findReviewsByStoreID(storeId, 0, 10);

		// then
		assertThat(result.getTotalElements()).isEqualTo(3); // 차단된 유저 리뷰 제외됨
		assertThat(result.getContent())
			.noneMatch(r -> r.getTitle().equals("차단된 리뷰"));
	}
}
