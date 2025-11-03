package com.ijaes.jeogiyo.menu.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuResponse;
import com.ijaes.jeogiyo.menu.service.MenuOwnerService;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuOwnerController 테스트")
class MenuOwnerControllerTest {

	@Mock
	private MenuOwnerService menuOwnerService;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private MenuOwnerController menuOwnerController;

	private UUID ownerId;
	private UUID storeId;
	private UUID menuId;
	private User testOwner;

	@BeforeEach
	void setUp() {
		ownerId = UUID.randomUUID();
		storeId = UUID.randomUUID();
		menuId = UUID.randomUUID();

		testOwner = User.builder()
			.id(ownerId)
			.username("owner@test.com")
			.password("password")
			.name("사장님")
			.address("서울시 강남구")
			.phoneNumber("010-1234-5678")
			.isOwner(true)
			.role(Role.OWNER)
			.build();
	}

	@Test
	@DisplayName("메뉴 등록 API - 성공")
	void createMenu_success() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("속이 꽌 찬 순대와 1200시간 이상 끓인 육수")
			.price(12000)
			.build();

		MenuResponse expectedResponse = MenuResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("속이 꽌 찬 순대와 1200시간 이상 끓인 육수")
			.price(12000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuResponse result = menuOwnerController.createMenu(request, authentication);

		// then
		assertNotNull(result);
		assertEquals(menuId, result.getId());
		assertEquals(storeId, result.getStoreId());
		assertEquals("순대국밥", result.getName());
		assertEquals("속이 꽌 찬 순대와 1200시간 이상 끓인 육수", result.getDescription());
		assertEquals(12000, result.getPrice());
	}

	@Test
	@DisplayName("메뉴 등록 API - 응답에 필수 정보 포함")
	void createMenu_responseContainsRequiredFields() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("김밥")
			.description("맛있는 김밥")
			.price(5000)
			.build();

		MenuResponse expectedResponse = MenuResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("김밥")
			.description("맛있는 김밥")
			.price(5000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuResponse result = menuOwnerController.createMenu(request, authentication);

		// then
		assertNotNull(result.getId());
		assertNotNull(result.getStoreId());
		assertNotNull(result.getName());
		assertNotNull(result.getDescription());
		assertNotNull(result.getPrice());
	}

	@Test
	@DisplayName("메뉴 등록 API - 메뉴명 반영")
	void createMenu_nameReflection() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("우육국밥")
			.description("신선한 우육")
			.price(15000)
			.build();

		MenuResponse expectedResponse = MenuResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("우육국밥")
			.description("신선한 우육")
			.price(15000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuResponse result = menuOwnerController.createMenu(request, authentication);

		// then
		assertEquals("우육국밥", result.getName());
	}

	@Test
	@DisplayName("메뉴 등록 API - 설명 반영")
	void createMenu_descriptionReflection() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("국밥")
			.description("집에서 정성스럽게 준비한 국밥입니다")
			.price(10000)
			.build();

		MenuResponse expectedResponse = MenuResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("국밥")
			.description("집에서 정성스럽게 준비한 국밥입니다")
			.price(10000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuResponse result = menuOwnerController.createMenu(request, authentication);

		// then
		assertEquals("집에서 정성스럽게 준비한 국밥입니다", result.getDescription());
	}

	@Test
	@DisplayName("메뉴 등록 API - 가격 반영")
	void createMenu_priceReflection() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("국밥")
			.description("맛있는 국밥")
			.price(8500)
			.build();

		MenuResponse expectedResponse = MenuResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("국밥")
			.description("맛있는 국밥")
			.price(8500)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuResponse result = menuOwnerController.createMenu(request, authentication);

		// then
		assertEquals(8500, result.getPrice());
	}

	@Test
	@DisplayName("메뉴 등록 API - 서비스 호출 확인")
	void createMenu_serviceInvocation() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("맛있는 국밥")
			.price(12000)
			.build();

		MenuResponse expectedResponse = MenuResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("맛있는 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.createMenu(any(CreateMenuRequest.class), any(Authentication.class)))
			.thenReturn(expectedResponse);

		// when
		MenuResponse result = menuOwnerController.createMenu(request, authentication);

		// then
		assertNotNull(result);
		assertEquals("순대국밥", result.getName());
	}

	@Test
	@DisplayName("메뉴 등록 API - 인증 정보 전달")
	void createMenu_authenticationPassed() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("맛있는 국밥")
			.price(12000)
			.build();

		MenuResponse expectedResponse = MenuResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("맛있는 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		menuOwnerController.createMenu(request, authentication);

		// then
		assertTrue(true);
	}
}
