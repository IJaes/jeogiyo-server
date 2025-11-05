package com.ijaes.jeogiyo.menu.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.menu.dto.response.MenuUserResponse;
import com.ijaes.jeogiyo.menu.entity.Menu;
import com.ijaes.jeogiyo.menu.repository.MenuRepository;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuUserService 테스트")
class MenuUserServiceTest {

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private MenuUserService menuUserService;

	private UUID ownerId;
	private UUID storeId;
	private User testOwner;
	private Store testStore;

	@BeforeEach
	void setUp() {
		ownerId = UUID.randomUUID();
		storeId = UUID.randomUUID();

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

		testStore = Store.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category(Category.KOREAN)
			.rate(4.5)
			.owner(testOwner)
			.build();
	}

	@Test
	@DisplayName("매장 메뉴 조회 - 성공 (여러 메뉴)")
	void getMenusByStoreId_success_multipleMenus() {
		// given
		UUID menu1Id = UUID.randomUUID();
		UUID menu2Id = UUID.randomUUID();
		UUID menu3Id = UUID.randomUUID();

		Menu menu1 = Menu.builder()
			.id(menu1Id)
			.store(testStore)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		Menu menu2 = Menu.builder()
			.id(menu2Id)
			.store(testStore)
			.name("내장탕")
			.description("고소한 내장탕")
			.price(13000)
			.build();

		Menu menu3 = Menu.builder()
			.id(menu3Id)
			.store(testStore)
			.name("순대")
			.description("신선한 순대")
			.price(8000)
			.build();

		List<Menu> menus = Arrays.asList(menu1, menu2, menu3);

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(menuRepository.findAllNotDeleted(storeId)).thenReturn(menus);

		// when
		List<MenuUserResponse> result = menuUserService.getMenusByStoreId(storeId);

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

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(menuRepository, times(1)).findAllNotDeleted(storeId);
	}

	@Test
	@DisplayName("매장 메뉴 조회 - 성공 (메뉴 없음)")
	void getMenusByStoreId_success_emptyList() {
		// given
		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(menuRepository.findAllNotDeleted(storeId)).thenReturn(Collections.emptyList());

		// when
		List<MenuUserResponse> result = menuUserService.getMenusByStoreId(storeId);

		// then
		assertNotNull(result);
		assertEquals(0, result.size());
		assertTrue(result.isEmpty());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(menuRepository, times(1)).findAllNotDeleted(storeId);
	}

	@Test
	@DisplayName("매장 메뉴 조회 - 실패 (매장 없음)")
	void getMenusByStoreId_fail_storeNotFound() {
		// given
		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			menuUserService.getMenusByStoreId(storeId);
		});

		assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(menuRepository, times(0)).findAllNotDeleted(storeId);
	}

	@Test
	@DisplayName("매장 메뉴 조회 - 응답 필드 검증")
	void getMenusByStoreId_responseContainsAllFields() {
		// given
		UUID menuId = UUID.randomUUID();
		Menu menu = Menu.builder()
			.id(menuId)
			.store(testStore)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(menuRepository.findAllNotDeleted(storeId)).thenReturn(Arrays.asList(menu));

		// when
		List<MenuUserResponse> result = menuUserService.getMenusByStoreId(storeId);

		// then
		assertEquals(1, result.size());
		MenuUserResponse response = result.get(0);

		assertNotNull(response.getId());
		assertNotNull(response.getStoreId());
		assertNotNull(response.getName());
		assertNotNull(response.getDescription());
		assertNotNull(response.getPrice());
	}

	@Test
	@DisplayName("특정 메뉴 조회 - 성공")
	void getMenuByMenuId_success() {
		// given
		UUID menuId = UUID.randomUUID();
		Menu menu = Menu.builder()
			.id(menuId)
			.store(testStore)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuRepository.findByIdNotDeleted(menuId)).thenReturn(Optional.of(menu));

		// when
		MenuUserResponse result = menuUserService.getMenuByMenuId(menuId);

		// then
		assertNotNull(result);
		assertEquals(menuId, result.getId());
		assertEquals("순대국밥", result.getName());
		assertEquals("뜨끈한 국밥", result.getDescription());
		assertEquals(12000, result.getPrice());
		assertEquals(storeId, result.getStoreId());

		verify(menuRepository, times(1)).findByIdNotDeleted(menuId);
	}

	@Test
	@DisplayName("특정 메뉴 조회 - 실패 (메뉴 없음)")
	void getMenuByMenuId_fail_menuNotFound() {
		// given
		UUID menuId = UUID.randomUUID();

		when(menuRepository.findByIdNotDeleted(menuId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			menuUserService.getMenuByMenuId(menuId);
		});

		assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
		verify(menuRepository, times(1)).findByIdNotDeleted(menuId);
	}

	@Test
	@DisplayName("특정 메뉴 조회 - 응답 필드 검증")
	void getMenuByMenuId_responseContainsAllFields() {
		// given
		UUID menuId = UUID.randomUUID();
		Menu menu = Menu.builder()
			.id(menuId)
			.store(testStore)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(menuRepository.findByIdNotDeleted(menuId)).thenReturn(Optional.of(menu));

		// when
		MenuUserResponse result = menuUserService.getMenuByMenuId(menuId);

		// then
		assertNotNull(result.getId());
		assertNotNull(result.getStoreId());
		assertNotNull(result.getName());
		assertNotNull(result.getDescription());
		assertNotNull(result.getPrice());
	}
}
