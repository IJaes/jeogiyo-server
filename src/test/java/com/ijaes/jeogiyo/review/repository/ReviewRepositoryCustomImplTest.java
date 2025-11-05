package com.ijaes.jeogiyo.review.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import com.ijaes.jeogiyo.common.config.QueryDslConfig;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import({ReviewRepositoryCustomImpl.class, QueryDslConfig.class})
@TestPropertySource(properties = {
	"spring.jpa.hibernate.ddl-auto=create",
	"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1"
})
@EntityScan(basePackages = {
	"com.ijaes.jeogiyo.review.entity",
	"com.ijaes.jeogiyo.store.entity",
	"com.ijaes.jeogiyo.user.entity"
})
@EnableJpaRepositories(basePackages = "com.ijaes.jeogiyo.review.repository")
class ReviewRepositoryCustomImplTest {

	@Autowired
	private EntityManager em;

	private ReviewRepositoryCustomImpl reviewRepositoryCustom;

	private UUID user1Id;
	private UUID user2Id;
	private UUID store1Id;
	private UUID store2Id;

	@BeforeEach
	void setUp() {
		em.createQuery("DELETE FROM Review").executeUpdate();
		em.createQuery("DELETE FROM Store").executeUpdate();
		em.createQuery("DELETE FROM User").executeUpdate();
		em.flush();

		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		reviewRepositoryCustom = new ReviewRepositoryCustomImpl(queryFactory);

		// given: 일반 유저 1
		User user1 = User.builder()
			.username("user1")
			.password("encoded_password")
			.name("일반유저1")
			.address("서울시 강남구")
			.phoneNumber("010-1111-2222")
			.isOwner(false)
			.role(Role.USER)
			.build();
		em.persist(user1);
		user1Id = user1.getId();

		// given: 일반 유저 2
		User user2 = User.builder()
			.username("user2")
			.password("encoded_password")
			.name("일반유저2")
			.address("서울시 용산구")
			.phoneNumber("010-3333-4444")
			.isOwner(false)
			.role(Role.USER)
			.build();
		em.persist(user2);
		user2Id = user2.getId();

		// given: 차단 유저
		User blockedUser = User.builder()
			.username("blockedUser")
			.password("encoded_password")
			.name("차단유저")
			.address("서울시 서초구")
			.phoneNumber("010-5555-6666")
			.isOwner(false)
			.role(Role.BLOCK)
			.build();
		em.persist(blockedUser);

		// given: 가게 1
		Store store1 = Store.builder()
			.businessNumber("123-45-67890")
			.name("가게1")
			.address("서울시 강남구")
			.description("테스트용 가게1입니다.")
			.category(Category.KOREAN)
			.rate(4.5)
			.owner(user1)
			.build();
		em.persist(store1);
		store1Id = store1.getId();

		// given: 가게 2
		Store store2 = Store.builder()
			.businessNumber("987-65-43210")
			.name("가게2")
			.address("서울시 용산구")
			.description("테스트용 가게2입니다.")
			.category(Category.CHINESE)
			.rate(4.2)
			.owner(user2)
			.build();
		em.persist(store2);
		store2Id = store2.getId();

		// given: 가게1에 리뷰 3개 (일반 유저)
		for (int i = 1; i <= 3; i++) {
			Review review = Review.builder()
				.orderId(UUID.randomUUID())
				.storeId(store1Id)
				.userId(user1Id)
				.title("가게1 리뷰 제목 " + i)
				.content("가게1 리뷰 내용 " + i)
				.rate(5)
				.isHidden(false)
				.build();
			em.persist(review);
		}

		// given: 가게2에 리뷰 2개 (일반 유저2)
		for (int i = 1; i <= 2; i++) {
			Review review = Review.builder()
				.orderId(UUID.randomUUID())
				.storeId(store2Id)
				.userId(user2Id)
				.title("가게2 리뷰 제목 " + i)
				.content("가게2 리뷰 내용 " + i)
				.rate(4)
				.isHidden(false)
				.build();
			em.persist(review);
		}

		// given: 숨김 리뷰 (store1)
		Review hiddenReview = Review.builder()
			.orderId(UUID.randomUUID())
			.storeId(store1Id)
			.userId(user1Id)
			.title("숨김 리뷰")
			.content("이건 관리자만 봐야 함")
			.rate(3)
			.isHidden(true)
			.build();
		em.persist(hiddenReview);

		// given: 차단 유저의 리뷰 (store2)
		Review blockedReview = Review.builder()
			.orderId(UUID.randomUUID())
			.storeId(store2Id)
			.userId(blockedUser.getId())
			.title("차단된 리뷰")
			.content("이건 관리자만 봐야 함")
			.rate(1)
			.isHidden(false)
			.build();
		em.persist(blockedReview);

		em.flush();
		em.clear();
	}

	@Test
	@DisplayName("관리자는 전체 리뷰를 최신순으로 조회할 수 있다")
	void findAllReviewsForAdmin() {
		// when
		Page<ReviewResponse> result = reviewRepositoryCustom.findAllReviewsForAdmin(0, 10);

		printReviews("관리자 전체 리뷰 조회 결과", result);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(7); // 모든 리뷰 포함 (3+2+숨김+차단)
		assertThat(result.getContent().get(0).getTitle()).startsWith("차단된 리뷰");
	}

	@Test
	@DisplayName("사용자가 자신의 리뷰를 최신순으로 조회할 수 있다 - 숨김 리뷰 포함")
	void findReviewsByUserId() {
		Page<ReviewResponse> result = reviewRepositoryCustom.findReviewsByUserId(user1Id, 0, 10);
		Page<ReviewResponse> result2 = reviewRepositoryCustom.findReviewsByUserId(user2Id, 0, 10);

		printReviews("유저별 리뷰 조회 결과 (user1)", result);
		printReviews("유저별 리뷰 조회 결과 (user2)", result2);

		assertThat(result.getTotalElements()).isEqualTo(4);
		assertThat(result.getContent().get(0).getReviewerName()).isEqualTo("user1");

	}

	@Test
	@DisplayName("가게별 리뷰 조회 시 숨김, 삭제, 차단된 사용자의 리뷰는 제외된다")
	void findReviewsByStoreID() {
		Page<ReviewResponse> result1 = reviewRepositoryCustom.findReviewsByStoreID(store1Id, 0, 10);
		Page<ReviewResponse> result2 = reviewRepositoryCustom.findReviewsByStoreID(store2Id, 0, 10);

		printReviews("가게1 리뷰 조회 결과", result1);
		printReviews("가게2 리뷰 조회 결과", result2);

		assertThat(result1.getTotalElements()).isEqualTo(3); // 숨김 제외
		assertThat(result1.getContent()).noneMatch(r -> r.getTitle().equals("숨김 리뷰"));

		assertThat(result2.getTotalElements()).isEqualTo(2); // 차단 리뷰 제외
		assertThat(result2.getContent()).noneMatch(r -> r.getTitle().equals("차단된 리뷰"));

	}

	private void printReviews(String message, Page<ReviewResponse> result) {
		System.out.println("\n==== " + message + " ====");
		result.getContent().forEach(r ->
			System.out.println("제목: " + r.getTitle() + " | 작성자: " + r.getReviewerName() + " | 평점: " + r.getRate())
		);
		System.out.println("==========================\n");
	}
}
