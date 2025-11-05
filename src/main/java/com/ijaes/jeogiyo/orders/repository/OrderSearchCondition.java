package com.ijaes.jeogiyo.orders.repository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.ijaes.jeogiyo.orders.entity.OrderStatus;

public record OrderSearchCondition(
	// record 클래스는 자바 16버전 이상부터 사용 가능
	// 이번 프로젝트에서는 시도해보고 다음번엔 일반 class로 생성해보기
	UUID userId,                // 주문자 필터 (일반회원: 본인 userId만; null이면 조건 미적용)
	UUID storeId,               // 가게 필터 (사장님: 내 storeId; null이면 조건 미적용)

	Set<OrderStatus> statuses,  // 상태 다건 검색 (IN 절). null이면 상태 조건 미적용

	LocalDateTime from,         // 생성일 하한 (createdAt >= from). null이면 하한 없음
	LocalDateTime to,           // 생성일 상한 (createdAt <= to). null이면 상한 없음

	Integer minTotalPrice,      // 최소 총액 (totalPrice >= minTotalPrice). null이면 하한 없음
	Integer maxTotalPrice,      // 최대 총액 (totalPrice <= maxTotalPrice). null이면 상한 없음

	Boolean includeDeleted,     // soft-delete 포함 여부 (true면 포함, false/null이면 제외)

	Sort sort                   // 정렬 정보 (예: Sort.by("createdAt").descending()).
	// null/unsorted면 레포지토리에서 기본값(createdAt DESC) 적용)
) {
}
