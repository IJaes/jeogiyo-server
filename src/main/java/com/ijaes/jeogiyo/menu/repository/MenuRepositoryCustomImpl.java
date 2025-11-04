package com.ijaes.jeogiyo.menu.repository;

import java.util.List;
import java.util.UUID;

import com.ijaes.jeogiyo.menu.entity.Menu;
import com.ijaes.jeogiyo.menu.entity.QMenu;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MenuRepositoryCustomImpl implements MenuRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Menu> findByOwnerId(UUID ownerId) {
		QMenu menu = QMenu.menu;

		return queryFactory
			.selectFrom(menu)
			.where(menu.store.owner.id.eq(ownerId))
			.fetch();
	}
}
