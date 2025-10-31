package com.ijaes.jeogiyo.store.repository;

import java.util.Optional;
import java.util.UUID;

import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;
import com.ijaes.jeogiyo.store.entity.QStore;
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
			.where(store.id.eq(storeId))
			.fetchOne();

		return Optional.ofNullable(result);
	}
}
