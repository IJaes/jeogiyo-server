package com.ijaes.jeogiyo.review.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import com.ijaes.jeogiyo.review.entity.Review;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@DataJpaTest
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
@Transactional
class ReviewRepositoryTest {

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private EntityManager em;

	@Test
	@DisplayName("existsByOrderId() - 특정 주문에 대한 리뷰가 존재하면 true를 반환한다")
	void existsByOrderId_whenReviewExists() {
		// given
		User user = User.builder()
			.username("user1")
			.password("encoded")
			.name("홍길동")
			.address("서울시 강남구")
			.phoneNumber("010-1111-2222")
			.role(Role.USER)
			.isOwner(false)
			.build();
		em.persist(user);

		Store store = Store.builder()
			.businessNumber("123-45-67890")
			.name("테스트가게")
			.address("서울시 서초구")
			.description("테스트용 가게입니다.")
			.category(Category.KOREAN)
			.rate(4.5)
			.owner(user)
			.build();
		em.persist(store);

		UUID orderId = UUID.randomUUID();

		Review review = Review.builder()
			.orderId(orderId)
			.storeId(store.getId())
			.userId(user.getId())
			.title("테스트 리뷰")
			.content("맛있어요!")
			.rate(5)
			.isHidden(false)
			.build();
		em.persist(review);
		em.flush();
		em.clear();

		// when
		boolean exists = reviewRepository.existsByOrderId(orderId);

		// then
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("existsByOrderId() - 특정 주문에 대한 리뷰가 존재하지 않으면 false를 반환한다")
	void existsByOrderId_whenReviewNotExists() {
		// given
		UUID randomOrderId = UUID.randomUUID();

		// when
		boolean exists = reviewRepository.existsByOrderId(randomOrderId);

		// then
		assertThat(exists).isFalse();
	}
}
