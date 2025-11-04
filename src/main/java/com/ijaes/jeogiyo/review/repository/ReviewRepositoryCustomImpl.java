package com.ijaes.jeogiyo.review.repository;

import static com.ijaes.jeogiyo.store.entity.QStore.*;
import static com.ijaes.jeogiyo.user.entity.QUser.*;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.review.entity.QReview;
import com.ijaes.jeogiyo.user.entity.Role;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	//1. 작성된 리뷰 전체 조회 - 관리자
	@Override
	public Page<ReviewResponse> findAllReviewsForAdmin(int page, int size) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		// 전체 조회
		List<ReviewResponse> content = queryFactory
			.select(Projections.constructor(ReviewResponse.class,
				review.reviewId,
				review.orderId,
				review.storeId,
				review.title,
				review.content,
				review.rate,
				user.username,
				store.name,
				review.isHidden,
				review.isDeleted,
				review.createdAt,
				review.updatedAt,
				review.deletedAt
			))
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.orderBy(review.createdAt.desc()) //최신순
			.offset(offset)
			.limit(size)
			.fetch();

		Long totalCount = queryFactory
			.select(review.count())
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.fetchOne();

		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}

	//2. 사용자 아이디 기준으로 리뷰 전체 조회
	@Override
	public Page<ReviewResponse> findReviewsByUserId(UUID userId, int page, int size) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		List<ReviewResponse> content = queryFactory
			.select(Projections.constructor(ReviewResponse.class,
				review.reviewId,
				review.orderId,
				review.storeId,
				review.title,
				review.content,
				review.rate,
				user.username,
				store.name,
				review.isHidden,
				review.isDeleted,
				review.createdAt,
				review.updatedAt,
				review.deletedAt
			))
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.where(review.userId.eq(userId))
			.orderBy(review.createdAt.desc()) //최신순
			.offset(offset)
			.limit(size)
			.fetch();

		Long totalCount = queryFactory
			.select(review.count())
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.where(review.userId.eq(userId))
			.fetchOne();

		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}

	//3. 가게 아이디 기준으로 리뷰 전체 조회
	@Override
	public Page<ReviewResponse> findReviewsByStoreID(UUID storeId, int page, int size) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		// 1. 해당 페이지에서 보여줄 데이터만 조회
		// 숨김 처리된 리뷰, BLOCK된 사용자의 리뷰는 조회되지 않게 처리
		List<ReviewResponse> content = queryFactory
			.select(Projections.constructor(ReviewResponse.class,
				review.reviewId,
				review.orderId,
				review.storeId,
				review.title,
				review.content,
				review.rate,
				user.username,
				store.name,
				review.isHidden,
				review.isDeleted,
				review.createdAt,
				review.updatedAt,
				review.deletedAt
			))
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.where(
				review.storeId.eq(storeId),
				review.isHidden.eq(false), //숨김처리된 리뷰 제외
				review.isDeleted.eq(false), //삭제된 리뷰 제외
				user.role.ne(Role.BLOCK) //차단된 사용자 리뷰 제외
			)
			.orderBy(review.createdAt.desc()) //최신순
			.offset(offset)
			.limit(size)
			.fetch();

		// 2. 보여줄 데이터의 총 개수
		Long totalCount = queryFactory
			.select(review.count())
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.where(
				review.storeId.eq(storeId),
				review.isHidden.eq(false),
				review.isDeleted.eq(false),
				user.role.ne(Role.BLOCK)
			)
			.fetchOne();

		// 3. Page 객체로 반환
		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}
}
