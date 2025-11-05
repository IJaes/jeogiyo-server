package com.ijaes.jeogiyo.menu.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ijaes.jeogiyo.menu.dto.response.MenuUserResponse;
import com.ijaes.jeogiyo.menu.service.MenuUserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuUserController 테스트")
class MenuUserControllerTest {

	@Mock
	private MenuUserService menuUserService;

	@InjectMocks
	private MenuUserController menuUserController;

	private UUID storeId;
	private UUID menuId;

	@BeforeEach
	void setUp() {
		storeId = UUID.randomUUID();
		menuId = UUID.randomUUID();
	}

	@Test
	@DisplayName("매장 메뉴 목록 조회 API - 성공 (여러 메뉴)")
	void getAllMenus_success_multipleMenus() {
		// given
		UUID menu1Id = UUID.randomUUID();
		UUID menu2Id = UUID.randomUUID();
		UUID menu3Id = UUID.randomUUID();

		MenuUserResponse menu1 = MenuUserResponse.builder()
			.id(menu1Id)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		MenuUserResponse menu2 = MenuUserResponse.builder()
			.id(menu2Id)
			.storeId(storeId)
			.name("내장탕")
			.description("고소한 내장탕")
			.price(13000)
			.build();

		MenuUserResponse menu3 = MenuUserResponse.builder()
			.id(menu3Id)
			.storeId(storeId)
			.name("순대")
			.description("신선한 순대")
			.price(8000)
			.build();

		List<MenuUserResponse> expectedMenus = Arrays.asList(menu1, menu2, menu3);

		when(menuUserService.getMenusByStoreId(storeId)).thenReturn(expectedMenus);

		// when
		List<MenuUserResponse> result = menuUserController.getAllMenus(storeId).getBody();

		// then
		assertNotNull(result);
		assertEquals(3, result.size());

		assertEquals(menu1Id, result.get(0).getId());
		assertEquals("순대국밥", result.get(0).getName());
		assertEquals(12000, result.get(0).getPrice());

		assertEquals(menu2Id, result.get(1).getId());
		assertEquals("내장탕", result.get(1).getName());
		assertEquals(13000, result.get(1).getPrice());

		assertEquals(menu3Id, result.get(2).getId());
		assertEquals("순대", result.get(2).getName());
		assertEquals(8000, result.get(2).getPrice());

		verify(menuUserService, times(1)).getMenusByStoreId(storeId);
	}

	@Test
	@DisplayName("매장 메뉴 목록 조회 API - 성공 (메뉴 없음)")
	void getAllMenus_success_emptyList() {
		// given
		when(menuUserService.getMenusByStoreId(storeId)).thenReturn(Collections.emptyList());

		// when
		List<MenuUserResponse> result = menuUserController.getAllMenus(storeId).getBody();

		// then
		assertNotNull(result);
		assertEquals(0, result.size());
		assertTrue(result.isEmpty());

		verify(menuUserService, times(1)).getMenusByStoreId(storeId);
	}

	@Test
	@DisplayName("매장 메뉴 목록 조회 API - 응답 상태 코드 200 OK")
	void getAllMenus_responseStatusOk() {
		// given
		MenuUserResponse menu = MenuUserResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuUserService.getMenusByStoreId(storeId)).thenReturn(Arrays.asList(menu));

		// when
		var response = menuUserController.getAllMenus(storeId);

		// then
		assertEquals(200, response.getStatusCodeValue());
		assertNotNull(response.getBody());
	}

	@Test
	@DisplayName("매장 메뉴 목록 조회 API - 서비스 호출 확인")
	void getAllMenus_serviceInvocation() {
		// given
		when(menuUserService.getMenusByStoreId(any(UUID.class))).thenReturn(Collections.emptyList());

		// when
		menuUserController.getAllMenus(storeId);

		// then
		verify(menuUserService, times(1)).getMenusByStoreId(storeId);
	}

	@Test
	@DisplayName("매장 메뉴 목록 조회 API - 응답에 필수 정보 포함")
	void getAllMenus_responseContainsRequiredFields() {
		// given
		MenuUserResponse menu = MenuUserResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuUserService.getMenusByStoreId(storeId)).thenReturn(Arrays.asList(menu));

		// when
		List<MenuUserResponse> result = menuUserController.getAllMenus(storeId).getBody();

		// then
		assertNotNull(result);
		assertEquals(1, result.size());

		MenuUserResponse menuResponse = result.get(0);
		assertNotNull(menuResponse.getId());
		assertNotNull(menuResponse.getStoreId());
		assertNotNull(menuResponse.getName());
		assertNotNull(menuResponse.getDescription());
		assertNotNull(menuResponse.getPrice());
	}

	@Test
	@DisplayName("특정 메뉴 조회 API - 성공")
	void getMenu_success() {
		// given
		MenuUserResponse expectedResponse = MenuUserResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuUserService.getMenuByMenuId(menuId)).thenReturn(expectedResponse);

		// when
		MenuUserResponse result = menuUserController.getMenu(menuId).getBody();

		// then
		assertNotNull(result);
		assertEquals(menuId, result.getId());
		assertEquals("순대국밥", result.getName());
		assertEquals("뜨끈한 국밥", result.getDescription());
		assertEquals(12000, result.getPrice());
		assertEquals(storeId, result.getStoreId());

		verify(menuUserService, times(1)).getMenuByMenuId(menuId);
	}

	@Test
	@DisplayName("특정 메뉴 조회 API - 응답 상태 코드 200 OK")
	void getMenu_responseStatusOk() {
		// given
		MenuUserResponse expectedResponse = MenuUserResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuUserService.getMenuByMenuId(menuId)).thenReturn(expectedResponse);

		// when
		var response = menuUserController.getMenu(menuId);

		// then
		assertEquals(200, response.getStatusCodeValue());
		assertNotNull(response.getBody());
	}

	@Test
	@DisplayName("특정 메뉴 조회 API - 서비스 호출 확인")
	void getMenu_serviceInvocation() {
		// given
		MenuUserResponse expectedResponse = MenuUserResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuUserService.getMenuByMenuId(any(UUID.class))).thenReturn(expectedResponse);

		// when
		menuUserController.getMenu(menuId);

		// then
		verify(menuUserService, times(1)).getMenuByMenuId(menuId);
	}

	@Test
	@DisplayName("특정 메뉴 조회 API - 응답에 필수 정보 포함")
	void getMenu_responseContainsRequiredFields() {
		// given
		MenuUserResponse expectedResponse = MenuUserResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuUserService.getMenuByMenuId(menuId)).thenReturn(expectedResponse);

		// when
		MenuUserResponse result = menuUserController.getMenu(menuId).getBody();

		// then
		assertNotNull(result);
		assertNotNull(result.getId());
		assertNotNull(result.getStoreId());
		assertNotNull(result.getName());
		assertNotNull(result.getDescription());
		assertNotNull(result.getPrice());
	}
}
