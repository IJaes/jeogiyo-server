package com.ijaes.jeogiyo.store.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;
import com.ijaes.jeogiyo.store.entity.QStore;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoreRepositoryCustomImpl implements StoreRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<StoreDetailResponse> findStoreDetailById(UUID storeId) {
		QStore store = QStore.store;
		QUser user = QUser.user;

		StoreDetailResponse result = queryFactory
			.select(Projections.constructor(
				StoreDetailResponse.class,
				store.id,
				store.businessNumber,
				store.name,
				store.address,
				store.description,
				store.category.stringValue(),
				store.rate,
				Projections.constructor(
					StoreDetailResponse.OwnerInfo.class,
					user.id,
					user.name,
					user.username,
					user.phoneNumber,
					user.address
				)
			))
			.from(store)
			.innerJoin(user).on(store.ownerId.eq(user.id))
			.where(store.id.eq(storeId), store.isDeleted.eq(false))
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public Optional<Store> findByOwnerId(UUID ownerId) {
		QStore store = QStore.store;

		Store result = queryFactory
			.selectFrom(store)
			.where(
				store.ownerId.eq(ownerId),
				store.isDeleted.eq(false)
			)
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public boolean existsByOwnerId(UUID ownerId) {
		QStore store = QStore.store;

		return queryFactory
			.selectOne()
			.from(store)
			.where(
				store.ownerId.eq(ownerId),
				store.isDeleted.eq(false)
			)
			.fetchFirst() != null;
	}

	@Override
	public Optional<Store> findByIdNotDeleted(UUID id) {
		QStore store = QStore.store;

		Store result = queryFactory
			.selectFrom(store)
			.where(
				store.id.eq(id),
				store.isDeleted.eq(false)
			)
			.fetchOne();

		return Optional.ofNullable(result);
	}

	@Override
	public Page<Store> findAllNotDeleted(Pageable pageable) {
		QStore store = QStore.store;

		var query = queryFactory
			.selectFrom(store)
			.where(store.isDeleted.eq(false));

		long total = query.fetch().size();

		var stores = queryFactory
			.selectFrom(store)
			.where(store.isDeleted.eq(false))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(store.createdAt.desc())
			.fetch();

		return new PageImpl<>(stores, pageable, total);
	}

	@Override
	public Page<Store> findAllIncludingDeleted(Pageable pageable) {
		QStore store = QStore.store;

		var query = queryFactory
			.selectFrom(store);

		long total = query.fetch().size();

		var stores = queryFactory
			.selectFrom(store)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(store.createdAt.desc())
			.fetch();

		return new PageImpl<>(stores, pageable, total);
	}
}
