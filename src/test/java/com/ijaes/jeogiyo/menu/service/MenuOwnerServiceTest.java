package com.ijaes.jeogiyo.menu.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.security.core.Authentication;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.gemini.service.GeminiService;
import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuResponse;
import com.ijaes.jeogiyo.menu.entity.Menu;
import com.ijaes.jeogiyo.menu.repository.MenuRepository;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuOwnerService 테스트")
class MenuOwnerServiceTest {

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private GeminiService geminiService;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private MenuOwnerService menuOwnerService;

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
	@DisplayName("메뉴 등록 - 성공")
	void createMenu_success() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("속이 꽌 찬 순대와 1200시간 이상 끓인 육수")
			.price(12000)
			.build();

		Menu savedMenu = Menu.builder()
			.id(UUID.randomUUID())
			.store(testStore)
			.name(request.getName())
			.description(request.getDescription())
			.price(request.getPrice())
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

		// when
		MenuResponse result = menuOwnerService.createMenu(request, authentication);

		// then
		assertNotNull(result);
		assertEquals(savedMenu.getId(), result.getId());
		assertEquals("순대국밥", result.getName());
		assertEquals("속이 꽌 찬 순대와 1200시간 이상 끓인 육수", result.getDescription());
		assertEquals(12000, result.getPrice());
		assertEquals(storeId, result.getStoreId());

		verify(storeRepository, times(1)).findByOwnerId(ownerId);
		verify(menuRepository, times(1)).save(any(Menu.class));
	}

	@Test
	@DisplayName("메뉴 등록 - 매장이 없음 (오너지만 매장 미등록)")
	void createMenu_storeNotFound() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("속이 꽌 찬 순대와 1200시간 이상 끓인 육수")
			.price(12000)
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			menuOwnerService.createMenu(request, authentication);
		});

		assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
		verify(storeRepository, times(1)).findByOwnerId(ownerId);
		verify(menuRepository, times(0)).save(any(Menu.class));
	}

	@Test
	@DisplayName("메뉴 등록 - 다양한 가격대 (1000원)")
	void createMenu_lowPrice() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("김밥")
			.description("맛있는 김밥")
			.price(1000)
			.build();

		Menu savedMenu = Menu.builder()
			.id(UUID.randomUUID())
			.store(testStore)
			.name(request.getName())
			.description(request.getDescription())
			.price(request.getPrice())
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

		// when
		MenuResponse result = menuOwnerService.createMenu(request, authentication);

		// then
		assertNotNull(result);
		assertEquals(1000, result.getPrice());
	}

	@Test
	@DisplayName("메뉴 등록 - 다양한 가격대 (50000원)")
	void createMenu_highPrice() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("한우 특선")
			.description("최고급 한우")
			.price(50000)
			.build();

		Menu savedMenu = Menu.builder()
			.id(UUID.randomUUID())
			.store(testStore)
			.name(request.getName())
			.description(request.getDescription())
			.price(request.getPrice())
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

		// when
		MenuResponse result = menuOwnerService.createMenu(request, authentication);

		// then
		assertNotNull(result);
		assertEquals(50000, result.getPrice());
	}

	@Test
	@DisplayName("메뉴 등록 - 긴 메뉴명")
	void createMenu_longMenuName() {
		// given
		String longName = "우리집 자랑 특제 순대국밥 (집비결 양념장 추가)";
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name(longName)
			.description("맛있는 국밥")
			.price(15000)
			.build();

		Menu savedMenu = Menu.builder()
			.id(UUID.randomUUID())
			.store(testStore)
			.name(request.getName())
			.description(request.getDescription())
			.price(request.getPrice())
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

		// when
		MenuResponse result = menuOwnerService.createMenu(request, authentication);

		// then
		assertNotNull(result);
		assertEquals(longName, result.getName());
	}

	@Test
	@DisplayName("메뉴 등록 - 메뉴 응답에 모든 정보 포함")
	void createMenu_responseContainsAllInfo() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("속이 꽌 찬 순대와 1200시간 이상 끓인 육수")
			.price(12000)
			.build();

		UUID menuId = UUID.randomUUID();
		Menu savedMenu = Menu.builder()
			.id(menuId)
			.store(testStore)
			.name(request.getName())
			.description(request.getDescription())
			.price(request.getPrice())
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

		// when
		MenuResponse result = menuOwnerService.createMenu(request, authentication);

		// then
		assertNotNull(result.getId());
		assertNotNull(result.getStoreId());
		assertNotNull(result.getName());
		assertNotNull(result.getDescription());
		assertNotNull(result.getPrice());
		assertEquals(menuId, result.getId());
		assertEquals(storeId, result.getStoreId());
	}

	@Test
	@DisplayName("메뉴 등록 - 0원 가격")
	void createMenu_zeroPrice() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("무료 메뉴")
			.description("무료로 제공하는 메뉴")
			.price(0)
			.build();

		Menu savedMenu = Menu.builder()
			.id(UUID.randomUUID())
			.store(testStore)
			.name(request.getName())
			.description(request.getDescription())
			.price(request.getPrice())
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

		// when
		MenuResponse result = menuOwnerService.createMenu(request, authentication);

		// then
		assertNotNull(result);
		assertEquals(0, result.getPrice());
	}

	@Test
	@DisplayName("메뉴 등록 - 설명 없음 (선택사항)")
	void createMenu_noDescription() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.price(12000)
			.build();

		Menu savedMenu = Menu.builder()
			.id(UUID.randomUUID())
			.store(testStore)
			.name(request.getName())
			.description(null)
			.price(request.getPrice())
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

		// when
		MenuResponse result = menuOwnerService.createMenu(request, authentication);

		// then
		assertNotNull(result);
		assertEquals("순대국밥", result.getName());
		assertNull(result.getDescription());
		assertEquals(12000, result.getPrice());
	}

	@Test
	@DisplayName("메뉴 조회 - 성공 (여러 메뉴)")
	void getMyMenus_success_multipleMenus() {
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

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(menuRepository.findByOwnerId(ownerId)).thenReturn(menus);

		// when
		List<MenuResponse> result = menuOwnerService.getMyMenus(authentication);

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

		verify(menuRepository, times(1)).findByOwnerId(ownerId);
	}

	@Test
	@DisplayName("메뉴 조회 - 성공 (메뉴 없음)")
	void getMyMenus_success_emptyList() {
		// given
		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(menuRepository.findByOwnerId(ownerId)).thenReturn(Collections.emptyList());

		// when
		List<MenuResponse> result = menuOwnerService.getMyMenus(authentication);

		// then
		assertNotNull(result);
		assertEquals(0, result.size());
		assertTrue(result.isEmpty());

		verify(menuRepository, times(1)).findByOwnerId(ownerId);
	}

	@Test
	@DisplayName("메뉴 조회 - 성공 (단일 메뉴)")
	void getMyMenus_success_singleMenu() {
		// given
		UUID menuId = UUID.randomUUID();
		Menu menu = Menu.builder()
			.id(menuId)
			.store(testStore)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(menuRepository.findByOwnerId(ownerId)).thenReturn(Arrays.asList(menu));

		// when
		List<MenuResponse> result = menuOwnerService.getMyMenus(authentication);

		// then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(menuId, result.get(0).getId());
		assertEquals("순대국밥", result.get(0).getName());
		assertEquals(storeId, result.get(0).getStoreId());

		verify(menuRepository, times(1)).findByOwnerId(ownerId);
	}

	@Test
	@DisplayName("메뉴 조회 - 응답에 모든 필드 포함 확인")
	void getMyMenus_responseContainsAllFields() {
		// given
		UUID menuId = UUID.randomUUID();
		Menu menu = Menu.builder()
			.id(menuId)
			.store(testStore)
			.name("순대국밥")
			.description("뜨끈한 국밥")
			.price(12000)
			.build();

		when(authentication.getPrincipal()).thenReturn(testOwner);
		when(menuRepository.findByOwnerId(ownerId)).thenReturn(Arrays.asList(menu));

		// when
		List<MenuResponse> result = menuOwnerService.getMyMenus(authentication);

		// then
		assertEquals(1, result.size());
		MenuResponse response = result.get(0);

		assertNotNull(response.getId());
		assertNotNull(response.getStoreId());
		assertNotNull(response.getName());
		assertNotNull(response.getDescription());
		assertNotNull(response.getPrice());
	}
}
