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
			.limit(1)
			.fetchFirst();

		return Optional.ofNullable(result);
	}

	@Override
	public boolean existsByOwnerId(UUID ownerId) {
		QStore store = QStore.store;

		UUID firstId = queryFactory
			.select(store.id)
			.from(store)
			.where(
				store.ownerId.eq(ownerId),
				store.isDeleted.eq(false)
			)
			.limit(1)
			.fetchFirst();

		return firstId != null;
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

		Long total = queryFactory
			.select(store.count())
			.from(store)
			.where(store.isDeleted.eq(false))
			.fetchOne();

		var content = queryFactory
			.selectFrom(store)
			.where(store.isDeleted.eq(false))
			.orderBy(store.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}

	@Override
	public Page<Store> findAllIncludingDeleted(Pageable pageable) {
		QStore store = QStore.store;

		Long total = queryFactory
			.select(store.count())
			.from(store)
			.fetchOne();

		var content = queryFactory
			.selectFrom(store)
			.orderBy(store.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}
}
