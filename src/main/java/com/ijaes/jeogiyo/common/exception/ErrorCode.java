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

	//리뷰 관련 (W-xxx)
	REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "W-001", "이미 해당 주문에 대한 리뷰가 작성되었습니다."),

	// 서버 에러 (S-xxx)
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S-001", "서버 오류가 발생했습니다. 관리자에게 문의하세요."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
