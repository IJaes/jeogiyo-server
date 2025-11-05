package com.ijaes.jeogiyo.menu.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.gemini.service.GeminiService;
import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.request.UpdateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuDetailResponse;
import com.ijaes.jeogiyo.menu.entity.Menu;
import com.ijaes.jeogiyo.menu.repository.MenuRepository;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuAdminService 테스트")
class MenuAdminServiceTest {

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private GeminiService geminiService;

	@InjectMocks
	private MenuAdminService menuAdminService;

	private UUID storeId;
	private UUID menuId;
	private UUID ownerId;
	private Store testStore;
	private Menu testMenu;
	private User testOwner;

	@BeforeEach
	void setUp() {
		storeId = UUID.randomUUID();
		menuId = UUID.randomUUID();
		ownerId = UUID.randomUUID();

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

		testMenu = Menu.builder()
			.id(menuId)
			.store(testStore)
			.name("순대국밥")
			.description("속이 꽉 찬 순대와 1200시간 이상 끓인 육수로 최고의 건강과 맛을 선사합니다.")
			.price(12000)
			.build();
	}

	@Test
	@DisplayName("메뉴 등록 - 성공 (일반 등록)")
	void createMenu_success() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("맛있는 순대국밥")
			.price(12000)
			.aiDescription(false)
			.build();

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(testMenu);

		// when
		MenuDetailResponse result = menuAdminService.createMenu(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("순대국밥", result.getName());
		assertEquals(12000, result.getPrice());
		verify(storeRepository, times(1)).findById(storeId);
		verify(menuRepository, times(1)).save(any(Menu.class));
		verify(geminiService, times(0)).generateMenuDescription(any());
	}

	@Test
	@DisplayName("메뉴 등록 - AI 생성 사용")
	void createMenu_withAiDescription() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description(null)
			.price(12000)
			.aiDescription(true)
			.build();

		String aiDescription = "AI가 생성한 메뉴 설명입니다.";
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(geminiService.generateMenuDescription("순대국밥")).thenReturn(aiDescription);
		when(menuRepository.save(any(Menu.class))).thenReturn(testMenu);

		// when
		MenuDetailResponse result = menuAdminService.createMenu(storeId, request);

		// then
		assertNotNull(result);
		verify(geminiService, times(1)).generateMenuDescription("순대국밥");
		verify(menuRepository, times(1)).save(any(Menu.class));
	}

	@Test
	@DisplayName("메뉴 등록 - AI 플래그가 true여도 description이 있으면 AI 호출 안함")
	void createMenu_aiTrueButDescriptionExists() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("이미 있는 설명")
			.price(12000)
			.aiDescription(true)
			.build();

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(menuRepository.save(any(Menu.class))).thenReturn(testMenu);

		// when
		MenuDetailResponse result = menuAdminService.createMenu(storeId, request);

		// then
		assertNotNull(result);
		verify(geminiService, times(0)).generateMenuDescription(any());
	}

	@Test
	@DisplayName("메뉴 등록 - 가게가 없음")
	void createMenu_storeNotFound() {
		// given
		CreateMenuRequest request = CreateMenuRequest.builder()
			.name("순대국밥")
			.description("맛있는 순대국밥")
			.price(12000)
			.build();

		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			menuAdminService.createMenu(storeId, request);
		});

		assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
		verify(menuRepository, times(0)).save(any(Menu.class));
	}

	@Test
	@DisplayName("전체 메뉴 조회 - 성공 (삭제된 메뉴 포함)")
	void getAllMenus_success() {
		// given
		Pageable pageable = PageRequest.of(0, 10);

		Menu deletedMenu = Menu.builder()
			.id(UUID.randomUUID())
			.store(testStore)
			.name("삭제된 메뉴")
			.description("이미 삭제됨")
			.price(10000)
			.build();
		deletedMenu.softDelete();

		List<Menu> menus = List.of(testMenu, deletedMenu);
		Page<Menu> menuPage = new PageImpl<>(menus, pageable, 2);

		when(menuRepository.findAllIncludingDeleted(any(Pageable.class))).thenReturn(menuPage);

		// when
		Page<MenuDetailResponse> result = menuAdminService.getAllMenus(0, 10);

		// then
		assertNotNull(result);
		assertEquals(2, result.getTotalElements());
		assertEquals(1, result.getTotalPages());
		verify(menuRepository, times(1)).findAllIncludingDeleted(any(Pageable.class));
	}

	@Test
	@DisplayName("전체 메뉴 조회 - 빈 결과")
	void getAllMenus_empty() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		Page<Menu> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(menuRepository.findAllIncludingDeleted(any(Pageable.class))).thenReturn(emptyPage);

		// when
		Page<MenuDetailResponse> result = menuAdminService.getAllMenus(0, 10);

		// then
		assertNotNull(result);
		assertEquals(0, result.getTotalElements());
		verify(menuRepository, times(1)).findAllIncludingDeleted(any(Pageable.class));
	}

	@Test
	@DisplayName("전체 메뉴 조회 - 페이지네이션")
	void getAllMenus_pagination() {
		// given
		Pageable pageable = PageRequest.of(1, 5);
		List<Menu> menuList = List.of(testMenu);
		Page<Menu> menuPage = new PageImpl<>(menuList, pageable, 15);

		when(menuRepository.findAllIncludingDeleted(any(Pageable.class))).thenReturn(menuPage);

		// when
		Page<MenuDetailResponse> result = menuAdminService.getAllMenus(1, 5);

		// then
		assertNotNull(result);
		assertEquals(15, result.getTotalElements());
		assertEquals(3, result.getTotalPages());
		assertEquals(1, result.getNumber());
		assertEquals(5, result.getSize());
		verify(menuRepository, times(1)).findAllIncludingDeleted(any(Pageable.class));
	}

	@Test
	@DisplayName("메뉴 상세 조회 - 성공")
	void getMenu_success() {
		// given
		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when
		MenuDetailResponse result = menuAdminService.getMenu(menuId);

		// then
		assertNotNull(result);
		assertEquals(menuId, result.getId());
		assertEquals("순대국밥", result.getName());
		assertEquals(12000, result.getPrice());
		verify(menuRepository, times(1)).findById(menuId);
	}

	@Test
	@DisplayName("메뉴 상세 조회 - 메뉴가 없음")
	void getMenu_menuNotFound() {
		// given
		when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			menuAdminService.getMenu(menuId);
		});

		assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("메뉴 소프트 삭제 - 성공")
	void deleteMenu_success() {
		// given
		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when
		menuAdminService.deleteMenu(menuId);

		// then
		verify(menuRepository, times(1)).findById(menuId);
		assertTrue(testMenu.isDeleted());
		assertNotNull(testMenu.getDeletedAt());
	}

	@Test
	@DisplayName("메뉴 소프트 삭제 - 메뉴가 없음")
	void deleteMenu_menuNotFound() {
		// given
		when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			menuAdminService.deleteMenu(menuId);
		});

		assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("메뉴 소프트 삭제 - 이미 삭제된 메뉴")
	void deleteMenu_alreadyDeleted() {
		// given
		testMenu.softDelete();
		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			menuAdminService.deleteMenu(menuId);
		});

		assertEquals(ErrorCode.MENU_ALREADY_DELETED, exception.getErrorCode());
	}

	@Test
	@DisplayName("메뉴 정보 수정 - 성공 (모든 필드 수정)")
	void updateMenu_success_allFields() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.name("새로운 메뉴명")
			.description("새로운 설명")
			.price(15000)
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when
		MenuDetailResponse result = menuAdminService.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		verify(menuRepository, times(1)).findById(menuId);
	}

	@Test
	@DisplayName("메뉴 정보 수정 - 부분 수정 (이름만)")
	void updateMenu_partialUpdate_nameOnly() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.name("변경된 이름")
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when
		MenuDetailResponse result = menuAdminService.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		verify(menuRepository, times(1)).findById(menuId);
	}

	@Test
	@DisplayName("메뉴 정보 수정 - 부분 수정 (가격만)")
	void updateMenu_partialUpdate_priceOnly() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.price(20000)
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when
		MenuDetailResponse result = menuAdminService.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		verify(menuRepository, times(1)).findById(menuId);
	}

	@Test
	@DisplayName("메뉴 정보 수정 - 메뉴가 없음")
	void updateMenu_menuNotFound() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.name("새로운 이름")
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			menuAdminService.updateMenu(menuId, request);
		});

		assertEquals(ErrorCode.MENU_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("메뉴 정보 수정 - 모든 필드 null (요청)")
	void updateMenu_allFieldsNull() {
		// given
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when
		MenuDetailResponse result = menuAdminService.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		assertEquals(testMenu.getName(), result.getName());
		assertEquals(testMenu.getDescription(), result.getDescription());
		assertEquals(testMenu.getPrice(), result.getPrice());
		verify(menuRepository, times(1)).findById(menuId);
	}

	@Test
	@DisplayName("메뉴 정보 수정 - 빈 문자열은 수정하지 않음 (이름)")
	void updateMenu_emptyNameNotUpdated() {
		// given
		String originalName = testMenu.getName();
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.name("")
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when
		MenuDetailResponse result = menuAdminService.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		assertEquals(originalName, result.getName());
		verify(menuRepository, times(1)).findById(menuId);
	}

	@Test
	@DisplayName("메뉴 정보 수정 - 여백만 있는 문자열은 수정하지 않음 (설명)")
	void updateMenu_whitespaceDescriptionNotUpdated() {
		// given
		String originalDescription = testMenu.getDescription();
		UpdateMenuRequest request = UpdateMenuRequest.builder()
			.description("   ")
			.build();

		when(menuRepository.findById(menuId)).thenReturn(Optional.of(testMenu));

		// when
		MenuDetailResponse result = menuAdminService.updateMenu(menuId, request);

		// then
		assertNotNull(result);
		assertEquals(originalDescription, result.getDescription());
		verify(menuRepository, times(1)).findById(menuId);
	}
}
