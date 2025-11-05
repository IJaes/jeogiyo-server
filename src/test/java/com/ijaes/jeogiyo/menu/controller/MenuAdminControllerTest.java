package com.ijaes.jeogiyo.menu.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.request.UpdateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuDetailResponse;
import com.ijaes.jeogiyo.menu.service.MenuAdminService;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuAdminController 테스트")
class MenuAdminControllerTest {

	@Mock
	private MenuAdminService menuAdminService;

	@InjectMocks
	private MenuAdminController menuAdminController;

	private UUID storeId;
	private UUID menuId;
	private MenuDetailResponse testMenuResponse;

	@BeforeEach
	void setUp() {
		storeId = UUID.randomUUID();
		menuId = UUID.randomUUID();

		testMenuResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("맛있는 순대국밥")
			.price(12000)
			.build();
	}

	@Test
	@DisplayName("메뉴 등록 API - 성공")
	void createMenu_success() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("맛있는 순대국밥")
			.price(12000)
			.aiDescription(false)
			.build();

		when(menuAdminService.createMenu(storeId, request))
			.thenReturn(testMenuResponse);

		// when
		ResponseEntity<MenuDetailResponse> result = menuAdminController.createMenu(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals("순대국밥", result.getBody().getName());
		assertEquals(12000, result.getBody().getPrice());
		verify(menuAdminService, times(1)).createMenu(storeId, request);
	}

	@Test
	@DisplayName("메뉴 등록 API - AI 생성 사용")
	void createMenu_withAiDescription() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description(null)
			.price(12000)
			.aiDescription(true)
			.build();

		MenuDetailResponse aiGeneratedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("AI가 생성한 설명")
			.price(12000)
			.build();

		when(menuAdminService.createMenu(storeId, request))
			.thenReturn(aiGeneratedResponse);

		// when
		ResponseEntity<MenuDetailResponse> result = menuAdminController.createMenu(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertEquals("AI가 생성한 설명", result.getBody().getDescription());
		verify(menuAdminService, times(1)).createMenu(storeId, request);
	}

	@Test
	@DisplayName("전체 메뉴 조회 API - 성공")
	void getAllMenus_success() {
		// given
		MenuDetailResponse menu1 = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("맛있는 국밥")
			.price(12000)
			.build();

		Page<MenuDetailResponse> expectedPage = new PageImpl<>(List.of(menu1));

		when(menuAdminService.getAllMenus(0, 10))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<MenuDetailResponse>> result = menuAdminController.getAllMenus(0, 10);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals(1, result.getBody().getTotalElements());
		assertEquals(menuId, result.getBody().getContent().get(0).getId());
		verify(menuAdminService, times(1)).getAllMenus(0, 10);
	}

	@Test
	@DisplayName("전체 메뉴 조회 API - 빈 결과")
	void getAllMenus_empty() {
		// given
		Page<MenuDetailResponse> emptyPage = new PageImpl<>(List.of());

		when(menuAdminService.getAllMenus(0, 10))
			.thenReturn(emptyPage);

		// when
		ResponseEntity<Page<MenuDetailResponse>> result = menuAdminController.getAllMenus(0, 10);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertEquals(0, result.getBody().getTotalElements());
		verify(menuAdminService, times(1)).getAllMenus(0, 10);
	}

	@Test
	@DisplayName("전체 메뉴 조회 API - 페이지네이션")
	void getAllMenus_pagination() {
		// given
		Page<MenuDetailResponse> expectedPage = new PageImpl<>(List.of(testMenuResponse));

		when(menuAdminService.getAllMenus(1, 5))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<MenuDetailResponse>> result = menuAdminController.getAllMenus(1, 5);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		verify(menuAdminService, times(1)).getAllMenus(1, 5);
	}

	@Test
	@DisplayName("메뉴 상세 조회 API - 성공")
	void getMenu_success() {
		// given
		when(menuAdminService.getMenu(menuId))
			.thenReturn(testMenuResponse);

		// when
		ResponseEntity<MenuDetailResponse> result = menuAdminController.getMenu(menuId);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals(menuId, result.getBody().getId());
		assertEquals("순대국밥", result.getBody().getName());
		verify(menuAdminService, times(1)).getMenu(menuId);
	}

	@Test
	@DisplayName("메뉴 소프트 삭제 API - 성공")
	void deleteMenu_success() {
		// given
		// when
		ResponseEntity<Void> result = menuAdminController.deleteMenu(menuId);

		// then
		assertNotNull(result);
		assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		verify(menuAdminService, times(1)).deleteMenu(menuId);
	}

	@Test
	@DisplayName("메뉴 정보 수정 API - 성공")
	void updateMenu_success() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.name("새로운 메뉴명")
			.description("새로운 설명")
			.price(15000)
			.build();

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("새로운 메뉴명")
			.description("새로운 설명")
			.price(15000)
			.build();

		when(menuAdminService.updateMenu(menuId, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<MenuDetailResponse> result = menuAdminController.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals("새로운 메뉴명", result.getBody().getName());
		assertEquals("새로운 설명", result.getBody().getDescription());
		assertEquals(15000, result.getBody().getPrice());
		verify(menuAdminService, times(1)).updateMenu(menuId, request);
	}

	@Test
	@DisplayName("메뉴 정보 수정 API - 이름만 수정")
	void updateMenu_nameOnly() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.name("변경된 이름")
			.build();

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("변경된 이름")
			.description("맛있는 순대국밥")
			.price(12000)
			.build();

		when(menuAdminService.updateMenu(menuId, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<MenuDetailResponse> result = menuAdminController.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		assertEquals("변경된 이름", result.getBody().getName());
		verify(menuAdminService, times(1)).updateMenu(menuId, request);
	}

	@Test
	@DisplayName("메뉴 정보 수정 API - 설명만 수정")
	void updateMenu_descriptionOnly() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.description("새로운 설명")
			.build();

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("새로운 설명")
			.price(12000)
			.build();

		when(menuAdminService.updateMenu(menuId, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<MenuDetailResponse> result = menuAdminController.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		assertEquals("새로운 설명", result.getBody().getDescription());
		verify(menuAdminService, times(1)).updateMenu(menuId, request);
	}

	@Test
	@DisplayName("메뉴 정보 수정 API - 가격만 수정")
	void updateMenu_priceOnly() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.price(20000)
			.build();

		MenuDetailResponse expectedResponse = MenuDetailResponse.builder()
			.id(menuId)
			.storeId(storeId)
			.name("순대국밥")
			.description("맛있는 순대국밥")
			.price(20000)
			.build();

		when(menuAdminService.updateMenu(menuId, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<MenuDetailResponse> result = menuAdminController.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		assertEquals(20000, result.getBody().getPrice());
		verify(menuAdminService, times(1)).updateMenu(menuId, request);
	}

	@Test
	@DisplayName("메뉴 정보 수정 API - 올바른 응답 형식")
	void updateMenu_correctResponseFormat() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.name("새로운 이름")
			.build();

		when(menuAdminService.updateMenu(any(UUID.class), any(UpdateMenuRequest.class)))
			.thenReturn(testMenuResponse);

		// when
		ResponseEntity<MenuDetailResponse> result = menuAdminController.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		assertTrue(result.getStatusCode().is2xxSuccessful());
		assertNotNull(result.getBody());
	}
}
