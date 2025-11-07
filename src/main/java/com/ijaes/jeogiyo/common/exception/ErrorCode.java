package com.ijaes.jeogiyo.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 인증 관련 (A-xxx)
	WRONG_ID_PW(HttpStatus.UNAUTHORIZED, "A-001", "아이디 혹은 비밀번호가 올바르지 않습니다."),
	USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A-002", "사용자를 찾을 수 없습니다."),
	DUPLICATE_USERNAME(HttpStatus.CONFLICT, "A-003", "이미 존재하는 아이디입니다."),
	INVALID_USERNAME(HttpStatus.BAD_REQUEST, "A-004", "아이디는 소문자(a~z), 숫자(0~9)를 포함하는 4자~10자의 문자열이어야 합니다."),
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "A-005", "비밀번호는 대소문자(a~z, A~Z), 숫자(0~9), 특수문자를 포함하는 8~15자의 문자열이어야 합니다."),
	INVALID_NAME(HttpStatus.BAD_REQUEST, "A-006", "이름은 3자~10자의 문자열이어야 합니다."),
	INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "A-007", "전화번호는 XXX-XXXX-XXXX 양식이어야 합니다."),
	BLOCKED_USER(HttpStatus.FORBIDDEN, "A-008", "정지된 계정입니다. 관리자에게 문의하세요."),

	// JWT 관련 (J-xxx)
	JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "J-001", "JWT 토큰이 만료되었습니다."),
	INVALID_JWT(HttpStatus.UNAUTHORIZED, "J-002", "유효하지 않은 JWT 토큰입니다."),
	MALFORMED_JWT(HttpStatus.UNAUTHORIZED, "J-003", "잘못된 형식의 JWT 토큰입니다."),
	UNSUPPORTED_JWT(HttpStatus.UNAUTHORIZED, "J-004", "지원하지 않는 JWT 토큰입니다."),
	JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "J-005", "JWT 서명이 유효하지 않습니다."),
	BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "J-006", "로그아웃된 토큰입니다."),

	// 인가 관련 (Z-xxx)
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "Z-001", "접근 권한이 없습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Z-002", "인증이 필요합니다."),
	OWNER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "Z-003", "OWNER 권한이 필요합니다."),

	// 리소스 관련 (R-xxx)
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "R-001", "요청하신 리소스를 찾을 수 없습니다."),
	DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "R-002", "이미 존재하는 리소스입니다."),

	// 비즈니스 로직 관련 (B-xxx)
	BUSINESS_ERROR(HttpStatus.BAD_REQUEST, "B-001", "비즈니스 로직 오류가 발생했습니다."),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "B-002", "잘못된 요청입니다."),

	// 사용자 정보 수정 관련 (U-xxx)
	INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "U-001", "주소는 100자 이내여야 합니다."),
	EMPTY_ADDRESS(HttpStatus.BAD_REQUEST, "U-002", "주소를 입력해주세요."),
	EMPTY_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "U-003", "현재 비밀번호를 입력해주세요."),
	INVALID_ROLE(HttpStatus.BAD_REQUEST, "U-004", "유효하지 않은 권한입니다."),
	DUPLICATE_PASSWORD(HttpStatus.BAD_REQUEST, "U-005", "새 비밀번호는 현재 비밀번호와 달라야 합니다."),

	// 서버 에러 (X-xxx)
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "X-001", "서버 오류가 발생했습니다. 관리자에게 문의하세요."),

	// 매장 관련 (S-xxx)
	DUPLICATE_STORE(HttpStatus.CONFLICT, "S-001", "이미 등록한 매장이 있습니다. 한 명의 OWNER는 하나의 매장만 등록할 수 있습니다."),
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S-002", "매장을 찾을 수 없습니다. 먼저 매장을 등록해주세요."),
	INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "S-003", "유효하지 않은 카테고리입니다. (KOREAN, JAPANESE, CHINESE, ITALIAN)"),

	// 리뷰 관련 (W-xxx)
	REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "W-001", "이미 해당 주문에 대한 리뷰가 작성되었습니다."),
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "W-002", "리뷰를 찾을 수 없습니다. 먼저 리뷰를 등록해주세요."),

	// 메뉴 관련 (M-xxx)
	MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M-001", "메뉴를 찾을 수 없습니다."),
	MENU_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "M-002", "이미 삭제된 메뉴입니다."),

	// 주문 관련 (O-xxx)
	ORDER_NOT_WAITING(HttpStatus.BAD_REQUEST, "O-001", "해당 작업은 주문 대기 상태에서만 가능합니다."),
	ORDER_ALREADY_SAME_STATUS(HttpStatus.BAD_REQUEST, "O-002", "이미 해당 상태입니다."),
	ORDER_INVALID_TRANSITION(HttpStatus.BAD_REQUEST, "O-003", "허용되지 않은 상태 전이입니다."),
	ORDER_CANCEL_OVERTIME(HttpStatus.BAD_REQUEST, "O-004", "주문 후 5분이 지나 취소할 수 없습니다."),
	ORDER_USER_MISMATCH(HttpStatus.FORBIDDEN, "O-005", "본인 주문만 취소할 수 있습니다."),
	ORDER_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "O-006", "해당 매장 주문만 처리할 수 있습니다."),
	ORDER_TOTAL_PRICE_INVALID(HttpStatus.BAD_REQUEST, "0-007", "합계 금액은 0원 이상이어야 합니다."),

	// 결제 관련 (P-xxx)
	PAYMENT_KEY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P-001", "결제 키 발급 중 오류가 발생했습니다."),
	PAYMENT_CONFIRMATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P-002", "결제 승인 중 오류가 발생했습니다."),
	INVALID_PAYMENT_REQUEST(HttpStatus.BAD_REQUEST, "P-003", "잘못된 결제 요청입니다."),
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P-004", "해당 결제 정보를 찾을 수 없습니다."),
	PAYMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "P-005", "결제 검증에 실패했습니다."),
	PAYMENT_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P-006", "결제 취소 중 오류가 발생했습니다."),
	PAYMENT_DB_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P-007", "결제 DB 저장 중 오류가 발생했습니다."),

	// 위치 관련 (G-xxx)
	ADDRESS_NOT_FOUND(HttpStatus.BAD_REQUEST, "G-001", "주소를 찾을 수 없습니다. 올바른 주소를 입력해주세요."),
	GEOCODING_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G-002", "지오코딩 API 호출 중 오류가 발생했습니다."),
	USER_COORDINATES_NOT_FOUND(HttpStatus.BAD_REQUEST, "G-003", "사용자의 좌표 정보가 없습니다. 주소를 먼저 설정해주세요.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
