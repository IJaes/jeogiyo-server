package com.ijaes.jeogiyo.orders.service;

import static com.ijaes.jeogiyo.common.exception.ErrorCode.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.orders.dto.response.OrderSummaryResponse;
import com.ijaes.jeogiyo.orders.repository.OrderRepositoryCustom;
import com.ijaes.jeogiyo.orders.repository.OrderSearchCondition;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

	private final OrderRepositoryCustom orderRepositoryCustom;
	private final StoreRepository storeRepository;

	/**
	 * [실행부] 동적 검색 (권한 필터링 없음)
	 * 목적:
	 *  - 실제 검색 로직만 담당하는 내부 전용 메서드.
	 *  - 어떤 권한/스코프 검증도 수행하지 않는다.
	 *  - 재사용/가독성을 높이기 위해 사용.
	 * 왜 private?
	 *  - 외부(컨트롤러/다른 서비스)에서 직접 호출되는 것을 차단하여
	 *    권한 체크가 누락되는 사고를 방지하기 위함.
	 *  - 반드시 public 진입점에서 인증/권한을 확인한 뒤에만 호출되도록 강제.
	 */
	private Page<OrderSummaryResponse> executeSearch(OrderSearchCondition condition,
		Pageable pageable) {
		return orderRepositoryCustom.searchOrders(condition, pageable)
			.map(OrderSummaryResponse::from);
	}

	/**
	 * [진입점] 동적 검색 (로그인 여부만 검사)
	 * 목적:
	 *  - 외부에서 호출 가능한 안전한 엔드포인트.
	 *  - 일반 사용자/점주/관리자 구분 없이 "로그인 되었는지" 만 확인한 뒤
	 *    내부 실행부(executeSearch)를 호출한다.
	 * 트랜잭션:
	 *  - @Transactional(readOnly = true)는 외부 진입점에만 붙인다.
	 *    (Spring AOP 프록시 특성상 private 메서드에는 트랜잭션이 적용되지 않음)
	 * 확장성:
	 *  - 정책 변경(예: 스코프 강제, 점주 소유 검증, 관리자 전용) 시
	 *    이 진입점에서만 로직을 추가/변경하면 된다.
	 */
	@Transactional(readOnly = true)
	public Page<OrderSummaryResponse> search(OrderSearchCondition condition,
		Pageable pageable,
		Authentication auth) {
		ensureAuthenticated(auth);     // ← 여기서 '로그인 여부'만 확인
		return executeSearch(condition, pageable); // 그대로 실행 (필터링 X)
	}

	// ===== helper: 로그인 여부만 확인 =====
	private static void ensureAuthenticated(Authentication auth) throws CustomException {
		Object p = auth.getPrincipal();
		if (!(p instanceof User)) {
			throw new CustomException(
				ACCESS_DENIED
			);
		}
	}

}
