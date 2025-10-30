package com.ijaes.jeogiyo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 정보 조회 응답")
public class UserInfoResponse {

	@Schema(description = "아이디", example = "john123")
	private String username;

	@Schema(description = "이름", example = "홍길동")
	private String name;

	@Schema(description = "주소", example = "서울시 강남구 역삼동")
	private String address;

	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phoneNumber;

	@Schema(description = "권한", example = "ROLE_USER")
	private String role;
}
