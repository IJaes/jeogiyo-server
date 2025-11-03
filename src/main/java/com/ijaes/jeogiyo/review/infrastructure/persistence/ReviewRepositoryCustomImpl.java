package com.ijaes.jeogiyo.review.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.ijaes.jeogiyo.review.domain.QReview;
import com.ijaes.jeogiyo.review.domain.Review;
import com.ijaes.jeogiyo.review.domain.ReviewRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	//1. 작성된 리뷰 전체 조회 - 관리자
	@Override
	public Page<Review> findAllReviewsByPaging(int page, int size) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		// 전체 조회
		List<Review> content = queryFactory
			.selectFrom(review)
			.orderBy(review.reviewId.desc())
			.offset(offset)
			.limit(size)
			.fetch();

		Long totalCount = queryFactory
			.select(review.count())
			.from(review)
			.fetchOne();

		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}

	//2. 사용자 아이디 기준으로 리뷰 전체 조회
	@Override
	public Page<Review> findReviewsByUserId(UUID userId, int page, int size) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		List<Review> content = queryFactory
			.selectFrom(review)
			.where(review.userId.eq(userId))
			.orderBy(review.reviewId.desc())
			.offset(offset)
			.limit(size)
			.fetch();

		Long totalCount = queryFactory
			.select(review.count())
			.from(review)
			.where(review.userId.eq(userId))
			.fetchOne();

		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}

	//3. 가게 아이디 기준으로 리뷰 전체 조회
	@Override
	public Page<Review> findReviewsByStoreID(UUID storeId, int page, int size) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		// 1. 해당 페이지에서 보여줄 데이터만 조회
		List<Review> content = queryFactory
			.selectFrom(review)
			.where(review.storeId.eq(storeId))
			.orderBy(review.reviewId.desc()) //최신순
			.offset(offset)
			.limit(size)
			.fetch();

		// 2. 보여줄 데이터의 총 개수
		Long totalCount = queryFactory
			.select(review.count())
			.from(review)
			.where(review.storeId.eq(storeId))
			.fetchOne();

		// 3. Page 객체로 반환
		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}
}
