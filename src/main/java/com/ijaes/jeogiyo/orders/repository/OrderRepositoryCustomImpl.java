package com.ijaes.jeogiyo.orders.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.ijaes.jeogiyo.orders.entity.Order;
import com.ijaes.jeogiyo.orders.entity.QOrder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

	// QueryDSL의 시작점: 스프링 설정으로 주입된 JPAQueryFactory
	private final JPAQueryFactory query;

	/**
	 * 주문 검색(동적 조건 + 페이징 + 정렬)
	 * 규칙:
	 * - condition에 있는 값만 추가하고, null값은 건너뛴다.(동적 쿼리)
	 * - 정렬 우선순위: condition.sort → pageable.sort → 기본값(createdAt DESC)
	 * - soft-delete: includeDeleted == true면 삭제 포함, 그 외는 삭제 제외
	 *
	 * @param cond 서비스가 정제한 내부 전용 검색 조건
	 * @param page  page/size/sort 정보 (sort는 보조 우선순위)
	 */
	@Override
	public Page<Order> searchOrders(OrderSearchCondition cond, Pageable page) {
		// 컬럼 접근은 전부 여기서
		// final - 실수로 다른 객로 갈아 끼우는걸 방지
		final QOrder o = QOrder.order;
		// 동적 where 조건 누적 - 조건 조립기.
		// final - 실수로 다른 객로 갈아 끼우는걸 방지
		final BooleanBuilder where = new BooleanBuilder();

		// =============== 동적 WHERE 구성 ===============
		// 1) 식별/범위 필터
		if (cond.userId() != null)
			where.and(o.userId.eq(cond.userId()));
		if (cond.storeId() != null)
			where.and(o.storeId.eq(cond.storeId()));

		// 2) 상태 in 검색 (statuses가 null/empty면 미적용)
		// 상태를 다중 검색 할 시엔 in을 사용 -> COCKING, COOKED 묶어서 검색하고 싶음
		// 다중검색이 아닐 경우엔 eq 사용
		if (cond.statuses() != null && !cond.statuses().isEmpty()) {
			where.and(o.orderStatus.in(cond.statuses()));
		}

		// 3) 기간 조건 (팀 규약: from >=, to <=)
		// goe, loe 는 비교 연산자(goe = 이상, loe = 이하)
		if (cond.from() != null)
			where.and(o.createdAt.goe(cond.from())); // >= from
		if (cond.to() != null)
			where.and(o.createdAt.loe(cond.to()));   // <= to

		// 4) 금액 범위 조건 - 최소 금액 이상, 최대 금액 이하
		if (cond.minTotalPrice() != null)
			where.and(o.totalPrice.goe(cond.minTotalPrice()));
		if (cond.maxTotalPrice() != null)
			where.and(o.totalPrice.loe(cond.maxTotalPrice()));

		// 5) 소프트 삭제 정책
		// includeDeleted == true 이면 삭제건 포함, 그 외(null/false)는 제외
		if (Boolean.TRUE.equals(cond.includeDeleted())) {
			// 아무 것도 하지 않음 << 가독성 향상을 위해 !조건이 아니라 일반 조건문으로 걸었음.
		} else {
			// deleted == false만
			where.and(o.deletedAt.isNull());
		}

		// =============== 정렬 구성 ===============
		// 우선순위: condition.sort → pageable.getSort() → 기본 createdAt DESC
		OrderSpecifier<?>[] orderSpecifiers = resolveOrderSpecifiers(
			cond.sort(), page.getSort(), o
		);

		// =============== 본문 조회 쿼리 ===============
		final List<Order> content = query
			.selectFrom(o)
			.where(where)
			.orderBy(orderSpecifiers)       // 정렬
			.offset(page.getOffset())       // 시작 위치 = (page-1) * size
			.limit(page.getPageSize())      // 개수 = size
			.fetch();

		// =============== 전체 건수 쿼리 (페이징 total) ===============
		final Long total = query
			.select(o.count())
			.from(o)
			.where(where)
			.fetchOne();                    // null 가능성 대비

		// PageImpl은 이미 페이징된 결과를 담아 전달하는 용도
		// DB 조회 결과를 알아서 잘라 주지 않는다.
		// 이미 잘라온 리스트(content) + 페이지 정보(page) + 전체 갯수(total or 0L);
		return new PageImpl<>(content, page, total == null ? 0L : total);
	}

	/**
	 * 정렬 우선순위 처리:
	 *  1) condition.sort(가장 우선)
	 *  2) pageable.sort
	 *  3) 기본값 createdAt DESC
	 * 화이트리스트: createdAt, totalPrice, orderStatus(status)만 허용
	 */
	private OrderSpecifier<?>[] resolveOrderSpecifiers(Sort condSort, Sort pageSort, QOrder o) {
		// 1) condition.sort가 있다면 그것부터 변환
		OrderSpecifier<?>[] fromCond = toOrderSpecifiers(condSort, o);
		if (fromCond.length > 0)
			return fromCond;

		// 2) pageable.sort가 있다면 그다음 적용
		OrderSpecifier<?>[] fromPageable = toOrderSpecifiers(pageSort, o);
		if (fromPageable.length > 0)
			return fromPageable;

		// 3) 아무 정렬도 없다면 기본값: 최신순 (createdAt DESC)
		return new OrderSpecifier<?>[] {o.createdAt.desc()};
	}

	/**
	 * Spring의 Sort → QueryDSL OrderSpecifier[]로 변환
	 * Spring의 Sort정보를 QueryDsl이 이해하는 정렬 명령으로 바꿔주는 기능- 어뎁터 역할
	 * - 허용된 필드만 스위치 문으로 매핑(보안/안정성)
	 * - 허용: "createdAt", "totalPrice", "orderStatus" (또는 "status")
	 * - 허용 외 속성은 무시 (예상치 못한 컬럼 인젝션 방지)
	 */
	private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort, QOrder o) {
		// OrderSpecifier<?> 여기에서 <?>는 제네릭 와일드 카드이다. -> 어떤 타입이 와도 상관없음
		List<OrderSpecifier<?>> list = new ArrayList<>();
		if (sort == null || sort.isUnsorted()) // sort가 null 이거나 sort가 정렬값이 없을 때
			return new OrderSpecifier<?>[0]; // 기본정렬인 최신순 반환

		for (Sort.Order s : sort) {
			String prop = s.getProperty(); // 정렬할 필드명: "createdAt" or "totalPrice" or "orderStatus"
			boolean desc = s.isDescending(); // 오름차순 내림차순 정렬할건지

			switch (prop) {
				case "createdAt":
					list.add(desc ? o.createdAt.desc() : o.createdAt.asc());
					break;
				case "totalPrice":
					list.add(desc ? o.totalPrice.desc() : o.totalPrice.asc());
					break;
				case "orderStatus": // 필드명 그대로 오는 경우
				case "status":      // 클라이언트가 status로 보낸 경우까지 허용
					// 유연성을 위해 orderStatus 혹은 status로 들어오는거 모두 허용
					list.add(desc ? o.orderStatus.desc() : o.orderStatus.asc());
					break;
				default:
					// 무시: 허용하지 않은 필드로 정렬 요청이 들어와도 안전
			}
		}
		return list.toArray(new OrderSpecifier<?>[0]);
	}
}
