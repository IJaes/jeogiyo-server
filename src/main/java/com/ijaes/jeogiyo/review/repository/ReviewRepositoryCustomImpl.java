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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	//1. 작성된 리뷰 전체 조회 - 관리자
	@Override
	public Page<ReviewResponse> findAllReviewsForAdmin(int page, int size, String filterType) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		//기본 필터 조건
		BooleanExpression baseFilter = createFilterCondition(filterType, review);
		if (baseFilter == null) {
			baseFilter = Expressions.asBoolean(true).isTrue();
		}

		//차단된 사용자 리뷰만 검색할 경우
		if (filterType != null && filterType.equalsIgnoreCase("BLOCKED")) {
			baseFilter = user.role.eq(Role.BLOCK);
		}

		// 전체 조회
		JPAQuery<ReviewResponse> query = queryFactory
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
				review.deletedAt.isNotNull(),
				review.createdAt,
				review.updatedAt,
				review.deletedAt
			))
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.where(baseFilter)
			.orderBy(review.createdAt.desc());

		List<ReviewResponse> content = query
			.offset(offset)
			.limit(size)
			.fetch();

		Long totalCount = queryFactory
			.select(review.count())
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.where(baseFilter)
			.fetchOne();

		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}

	//2. 사용자 아이디 기준으로 리뷰 전체 조회
	@Override
	public Page<ReviewResponse> findReviewsByUserId(UUID userId, int page, int size, String filterType,
		String sortType) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		//기본 조건 - 본인 리뷰만
		BooleanExpression baseCondition = review.userId.eq(userId);

		//필터링 조건 추가
		BooleanExpression filterCondition = createFilterCondition(filterType, review);

		//기본 조건에 결합
		if (filterCondition != null) {
			baseCondition = baseCondition.and(filterCondition);
		}

		JPAQuery<ReviewResponse> query = queryFactory
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
				review.deletedAt.isNotNull(),
				review.createdAt,
				review.updatedAt,
				review.deletedAt
			))
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.where(baseCondition);

		applySorting(query, sortType, review);

		List<ReviewResponse> content = query
			.offset(offset)
			.limit(size)
			.fetch();

		Long totalCount = queryFactory
			.select(review.count())
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.innerJoin(store).on(review.storeId.eq(store.id))
			.where(baseCondition)
			.fetchOne();

		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}

	//3. 가게 아이디 기준으로 리뷰 전체 조회
	@Override
	public Page<ReviewResponse> findReviewsByStoreID(UUID storeId, int page, int size, String sortType) {
		long offset = (long)page * size;
		QReview review = QReview.review;

		// 1. 해당 페이지에서 보여줄 데이터만 조회
		// 숨김 처리된 리뷰, BLOCK된 사용자의 리뷰는 조회되지 않게 처리
		JPAQuery<ReviewResponse> query = queryFactory
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
				review.deletedAt.isNotNull(),
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
				review.deletedAt.isNull(), //삭제된 리뷰 제외
				user.role.ne(Role.BLOCK) //차단된 사용자 리뷰 제외
			);

		applySorting(query, sortType, review);

		List<ReviewResponse> content = query
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
				review.deletedAt.isNull(),
				user.role.ne(Role.BLOCK)
			)
			.fetchOne();

		// 3. Page 객체로 반환
		return new PageImpl<>(content, PageRequest.of(page, size), totalCount);
	}

	//4. 가게별 전체 평점의 평균 구하기
	@Override
	public Double calculateAverageRateByStoreId(UUID storeId) {
		QReview review = QReview.review;

		return queryFactory
			.select(review.rate.avg())
			.from(review)
			.innerJoin(user).on(review.userId.eq(user.id))
			.where(
				review.storeId.eq(storeId),
				review.deletedAt.isNull(),
				review.isHidden.eq(false),
				user.role.ne(Role.BLOCK)
			)
			.fetchOne();
	}

	//필터 - 숨김, 삭제된 리뷰
	private BooleanExpression createFilterCondition(String filterType, QReview review) {

		//전체 조회
		if (filterType == null || filterType.equalsIgnoreCase("ALL")) {
			return null;
		}

		switch (filterType.toUpperCase()) {
			case "HIDDEN": //숨겨진 리뷰만 볼 때
				return review.isHidden.eq(true).and(review.deletedAt.isNull());
			case "DELETED": //삭제된 리뷰만 볼 때
				return review.deletedAt.isNotNull();
			case "NORMAL": //일반 리뷰만 볼 때
				return review.isHidden.eq(false).and(review.deletedAt.isNull());
			default:
				return null;
		}
	}

	//정렬 - 최신순, 평점 높/낮은순
	private void applySorting(JPAQuery<?> query, String sortType, QReview review) {

		//기본적으로는 최신순
		if (sortType == null) {
			query.orderBy(review.createdAt.desc());
			return;
		}

		switch (sortType.toUpperCase()) {
			case "HIGH_RATE": //평점 높은순, 최신순
				query.orderBy(review.rate.desc(), review.createdAt.desc());
				break;
			case "LOW_RATE": //평점 낮은순, 최신순
				query.orderBy(review.rate.asc(), review.createdAt.desc());
				break;
			case "LATEST": //최신순
			default:
				query.orderBy(review.createdAt.desc());
				break;
		}
	}
}
