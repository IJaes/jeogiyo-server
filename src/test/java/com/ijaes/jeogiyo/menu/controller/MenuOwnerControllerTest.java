package com.ijaes.jeogiyo.menu.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.security.core.Authentication;

import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.request.UpdateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuDetailResponse;
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

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("속이 꽌 찬 순대와 1200시간 이상 끓인 육수")
			.price(12000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.createMenu(request, authentication).getBody();

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

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("김밥")
			.description("맛있는 김밥")
			.price(5000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.createMenu(request, authentication).getBody();

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

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("우육국밥")
			.description("신선한 우육")
			.price(15000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.createMenu(request, authentication).getBody();

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

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("국밥")
			.description("집에서 정성스럽게 준비한 국밥입니다")
			.price(10000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.createMenu(request, authentication).getBody();

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

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("국밥")
			.description("맛있는 국밥")
			.price(8500)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.createMenu(request, authentication).getBody();

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

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("맛있는 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.createMenu(any(CreateMenuRequest.class), any(Authentication.class)))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.createMenu(request, authentication).getBody();

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

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
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

	@Test
	@DisplayName("메뉴 등록 API - 0원 가격 허용")
	void createMenu_zeroPriceAllowed() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("무료 메뉴")
			.description("무료로 제공하는 메뉴")
			.price(0)
			.build();

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("무료 메뉴")
			.description("무료로 제공하는 메뉴")
			.price(0)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.createMenu(request, authentication).getBody();

		// then
		assertNotNull(result);
		assertEquals(0, result.getPrice());
	}

	@Test
	@DisplayName("메뉴 등록 API - 설명 없음 (선택사항)")
	void createMenu_noDescription() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.price(12000)
			.build();

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description(null)
			.price(12000)
			.build();

		when(menuOwnerService.createMenu(request, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.createMenu(request, authentication).getBody();

		// then
		assertNotNull(result);
		assertEquals("순대국밥", result.getName());
		assertNull(result.getDescription());
		assertEquals(12000, result.getPrice());
	}

	@Test
	@DisplayName("메뉴 조회 API - 성공 (여러 메뉴)")
	void getMenus_success_multipleMenus() {
		// given
		UUID menu1Id = UUID.randomUUID();
		UUID menu2Id = UUID.randomUUID();
		UUID menu3Id = UUID.randomUUID();

		MenuDetailResponse menu1 = MenuDetailResponse.builder()
			.id(menu1Id)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		MenuDetailResponse menu2 = MenuDetailResponse.builder()
			.id(menu2Id)
			.storeId(storeId)
			.name("내장탕")
			.description("고소한 내장탕")
			.price(13000)
			.build();

		MenuDetailResponse menu3 = MenuDetailResponse.builder()
			.id(menu3Id)
			.storeId(storeId)
			.name("순대")
			.description("신선한 순대")
			.price(8000)
			.build();

		List<MenuDetailResponse> expectedMenus = Arrays.asList(menu1, menu2, menu3);

		when(menuOwnerService.getMyMenus(authentication))
			.thenReturn(expectedMenus);

		// when
		List<MenuDetailResponse> result = menuOwnerController.getMenus(authentication).getBody();

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

		verify(menuOwnerService, times(1)).getMyMenus(authentication);
	}

	@Test
	@DisplayName("메뉴 조회 API - 성공 (메뉴 없음)")
	void getMenus_success_emptyList() {
		// given
		when(menuOwnerService.getMyMenus(authentication))
			.thenReturn(Collections.emptyList());

		// when
		List<MenuDetailResponse> result = menuOwnerController.getMenus(authentication).getBody();

		// then
		assertNotNull(result);
		assertEquals(0, result.size());
		assertTrue(result.isEmpty());

		verify(menuOwnerService, times(1)).getMyMenus(authentication);
	}

	@Test
	@DisplayName("메뉴 조회 API - 성공 (단일 메뉴)")
	void getMenus_success_singleMenu() {
		// given
		MenuDetailResponse menu = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.getMyMenus(authentication))
			.thenReturn(Arrays.asList(menu));

		// when
		List<MenuDetailResponse> result = menuOwnerController.getMenus(authentication).getBody();

		// then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(menuId, result.get(0).getId());
		assertEquals("순대국밥", result.get(0).getName());
		assertEquals(storeId, result.get(0).getStoreId());

		verify(menuOwnerService, times(1)).getMyMenus(authentication);
	}

	@Test
	@DisplayName("메뉴 조회 API - 응답 상태 코드 200 OK")
	void getMenus_responseStatusOk() {
		// given
		MenuDetailResponse menu = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.getMyMenus(authentication))
			.thenReturn(Arrays.asList(menu));

		// when
		var response = menuOwnerController.getMenus(authentication);

		// then
		assertEquals(200, response.getStatusCodeValue());
		assertNotNull(response.getBody());
	}

	@Test
	@DisplayName("메뉴 조회 API - 서비스 호출 확인")
	void getMenus_serviceInvocation() {
		// given
		when(menuOwnerService.getMyMenus(any(Authentication.class)))
			.thenReturn(Collections.emptyList());

		// when
		menuOwnerController.getMenus(authentication);

		// then
		verify(menuOwnerService, times(1)).getMyMenus(authentication);
	}

	@Test
	@DisplayName("메뉴 조회 API - 응답에 필수 정보 포함")
	void getMenus_responseContainsRequiredFields() {
		// given
		MenuDetailResponse menu = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.getMyMenus(authentication))
			.thenReturn(Arrays.asList(menu));

		// when
		List<MenuDetailResponse> result = menuOwnerController.getMenus(authentication).getBody();

		// then
		assertNotNull(result);
		assertEquals(1, result.size());

		MenuDetailResponse menuDetailResponse = result.get(0);
		assertNotNull(menuDetailResponse.getId());
		assertNotNull(menuDetailResponse.getStoreId());
		assertNotNull(menuDetailResponse.getName());
		assertNotNull(menuDetailResponse.getDescription());
		assertNotNull(menuDetailResponse.getPrice());
	}

	@Test
	@DisplayName("메뉴 조회 API - 여러 메뉴 정렬 순서 유지")
	void getMenus_orderMaintained() {
		// given
		MenuDetailResponse menu1 = MenuDetailResponse.builder()
			.id(UUID.randomUUID())
			.storeId(storeId)
			.name("첫번째")
			.description("설명1")
			.price(10000)
			.build();

		MenuDetailResponse menu2 = MenuDetailResponse.builder()
			.id(UUID.randomUUID())
			.storeId(storeId)
			.name("두번째")
			.description("설명2")
			.price(20000)
			.build();

		List<MenuDetailResponse> expectedMenus = Arrays.asList(menu1, menu2);

		when(menuOwnerService.getMyMenus(authentication))
			.thenReturn(expectedMenus);

		// when
		List<MenuDetailResponse> result = menuOwnerController.getMenus(authentication).getBody();

		// then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("첫번째", result.get(0).getName());
		assertEquals("두번째", result.get(1).getName());
	}

	@Test
	@DisplayName("특정 메뉴 조회 API - 성공")
	void getMenu_success() {
		// given
		UUID targetMenuId = UUID.randomUUID();
		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(targetMenuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.getMyMenu(targetMenuId, authentication))
			.thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.getMenu(targetMenuId, authentication).getBody();

		// then
		assertNotNull(result);
		assertEquals(targetMenuId, result.getId());
		assertEquals("순대국밥", result.getName());
		assertEquals("뜨끈한 국밥", result.getDescription());
		assertEquals(12000, result.getPrice());
		assertEquals(storeId, result.getStoreId());

		verify(menuOwnerService, times(1)).getMyMenu(targetMenuId, authentication);
	}

	@Test
	@DisplayName("특정 메뉴 조회 API - 응답 상태 코드 200 OK")
	void getMenu_responseStatusOk() {
		// given
		UUID targetMenuId = UUID.randomUUID();
		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(targetMenuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.getMyMenu(targetMenuId, authentication))
			.thenReturn(expectedResponse);

		// when
		var response = menuOwnerController.getMenu(targetMenuId, authentication);

		// then
		assertEquals(200, response.getStatusCodeValue());
		assertNotNull(response.getBody());
	}

	@Test
	@DisplayName("특정 메뉴 조회 API - 서비스 호출 확인")
	void getMenu_serviceInvocation() {
		// given
		UUID targetMenuId = UUID.randomUUID();
		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(targetMenuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuOwnerService.getMyMenu(any(UUID.class), any(Authentication.class)))
			.thenReturn(expectedResponse);

		// when
		menuOwnerController.getMenu(targetMenuId, authentication);

		// then
		verify(menuOwnerService, times(1)).getMyMenu(targetMenuId, authentication);
	}

	@Test
	@DisplayName("메뉴 수정 API - 성공")
	void updateMenu_success() {
		// given
		UUID targetMenuId = UUID.randomUUID();
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.name("특제 순대국밥")
			.description("업그레이드된 설명")
			.price(15000)
			.build();

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(targetMenuId)
			.storeId(storeId)
			.name("특제 순대국밥")
			.description("업그레이드된 설명")
			.price(15000)
			.build();

		when(menuOwnerService.updateMenu(targetMenuId, request, authentication)).thenReturn(expectedResponse);

		// when
		MenuDetailResponse result = menuOwnerController.updateMenu(targetMenuId, request, authentication).getBody();

		// then
		assertNotNull(result);
		assertEquals(targetMenuId, result.getId());
		assertEquals("특제 순대국밥", result.getName());
		assertEquals(15000, result.getPrice());

		verify(menuOwnerService, times(1)).updateMenu(targetMenuId, request, authentication);
	}

	@Test
	@DisplayName("메뉴 삭제 API - 성공")
	void deleteMenu_success() {
		// given
		UUID targetMenuId = UUID.randomUUID();

		doNothing().when(menuOwnerService).deleteMenu(targetMenuId, authentication);

		// when
		var response = menuOwnerController.deleteMenu(targetMenuId, authentication);

		// then
		assertEquals(204, response.getStatusCodeValue());
		assertNull(response.getBody());

		verify(menuOwnerService, times(1)).deleteMenu(targetMenuId, authentication);
	}

	@Test
	@DisplayName("메뉴 삭제 API - 서비스 호출 확인")
	void deleteMenu_serviceInvocation() {
		// given
		UUID targetMenuId = UUID.randomUUID();

		doNothing().when(menuOwnerService).deleteMenu(any(UUID.class), any(Authentication.class));

		// when
		menuOwnerController.deleteMenu(targetMenuId, authentication);

		// then
		verify(menuOwnerService, times(1)).deleteMenu(targetMenuId, authentication);
	}
}
