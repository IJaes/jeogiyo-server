package com.ijaes.jeogiyo.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.store.dto.request.CreateStoreRequest;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreOwnerService 테스트")
class StoreOwnerServiceTest {

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private StoreOwnerService storeOwnerService;

	@Mock
	private Authentication authentication;

	private User ownerUser;
	private UUID ownerId;
	private CreateStoreRequest createStoreRequest;
	private Store testStore;

	@BeforeEach
	void setUp() {
		ownerId = UUID.randomUUID();
		ownerUser = User.builder()
			.id(ownerId)
			.username("owner123")
			.password("password")
			.name("김사장")
			.phoneNumber("010-1234-5678")
			.address("서울시 강남구")
			.isOwner(true)
			.role(Role.OWNER)
			.build();

		createStoreRequest = CreateStoreRequest.builder()
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category("KOREAN")
			.build();

		testStore = Store.builder()
			.id(UUID.randomUUID())
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category(Category.KOREAN)
			.rate(0.0)
			.ownerId(ownerId)
			.build();
	}

	@Test
	@DisplayName("매장 생성 - 성공")
	void createStore_success() {
		// given
		when(authentication.getPrincipal()).thenReturn(ownerUser);
		when(storeRepository.existsByOwnerId(ownerId)).thenReturn(false);
		when(storeRepository.save(any(Store.class))).thenReturn(testStore);

		// when
		StoreResponse result = storeOwnerService.createStore(authentication, createStoreRequest);

		// then
		assertNotNull(result);
		assertEquals(testStore.getId(), result.getId());
		assertEquals("소문난 국밥집", result.getName());
		assertEquals("KOREAN", result.getCategory());
		assertEquals(0.0, result.getRate());
		assertEquals(ownerId, result.getOwnerId());

		verify(storeRepository, times(1)).existsByOwnerId(ownerId);
		verify(storeRepository, times(1)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 생성 - 권한 없음 (USER 역할)")
	void createStore_withoutOwnerRole() {
		// given
		User nonOwnerUser = User.builder()
			.id(UUID.randomUUID())
			.username("user123")
			.password("password")
			.name("일반사용자")
			.phoneNumber("010-1234-5678")
			.address("서울시 강남구")
			.isOwner(false)
			.role(Role.USER)
			.build();

		when(authentication.getPrincipal()).thenReturn(nonOwnerUser);

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeOwnerService.createStore(authentication, createStoreRequest);
		});

		assertEquals(ErrorCode.OWNER_ROLE_REQUIRED, exception.getErrorCode());
		verify(storeRepository, times(0)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 생성 - 중복된 매장")
	void createStore_duplicateStore() {
		// given
		when(authentication.getPrincipal()).thenReturn(ownerUser);
		when(storeRepository.existsByOwnerId(ownerId)).thenReturn(true);

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeOwnerService.createStore(authentication, createStoreRequest);
		});

		assertEquals(ErrorCode.DUPLICATE_STORE, exception.getErrorCode());
		verify(storeRepository, times(0)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 생성 - 잘못된 카테고리")
	void createStore_invalidCategory() {
		// given
		when(authentication.getPrincipal()).thenReturn(ownerUser);
		when(storeRepository.existsByOwnerId(ownerId)).thenReturn(false);

		CreateStoreRequest invalidRequest = CreateStoreRequest.builder()
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category("INVALID_CATEGORY")
			.build();

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeOwnerService.createStore(authentication, invalidRequest);
		});

		assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
		verify(storeRepository, times(0)).save(any(Store.class));
	}

	@Test
	@DisplayName("본인의 매장 조회 - 성공")
	void myStore_success() {
		// given
		when(authentication.getPrincipal()).thenReturn(ownerUser);
		when(storeRepository.findByOwnerId(ownerId)).thenReturn(testStore);

		// when
		StoreResponse result = storeOwnerService.myStore(authentication);

		// then
		assertNotNull(result);
		assertEquals(testStore.getId(), result.getId());
		assertEquals("소문난 국밥집", result.getName());
		assertEquals(ownerId, result.getOwnerId());

		verify(storeRepository, times(1)).findByOwnerId(ownerId);
	}

	@Test
	@DisplayName("본인의 매장 조회 - 권한 없음 (USER 역할)")
	void myStore_withoutOwnerRole() {
		// given
		User nonOwnerUser = User.builder()
			.id(UUID.randomUUID())
			.username("user123")
			.password("password")
			.name("일반사용자")
			.phoneNumber("010-1234-5678")
			.address("서울시 강남구")
			.isOwner(false)
			.role(Role.USER)
			.build();

		when(authentication.getPrincipal()).thenReturn(nonOwnerUser);

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeOwnerService.myStore(authentication);
		});

		assertEquals(ErrorCode.OWNER_ROLE_REQUIRED, exception.getErrorCode());
		verify(storeRepository, times(0)).findByOwnerId(any());
	}

	@Test
	@DisplayName("본인의 매장 조회 - 매장 조회 실패")
	void myStore_queryFailure() {
		// given
		when(authentication.getPrincipal()).thenReturn(ownerUser);
		when(storeRepository.findByOwnerId(ownerId)).thenThrow(new IllegalStateException("Query failed"));

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeOwnerService.myStore(authentication);
		});

		assertEquals(ErrorCode.INVALID_ADDRESS, exception.getErrorCode());
	}

	@Test
	@DisplayName("매장 생성 - 모든 카테고리 지원")
	void createStore_allCategories() {
		// given
		String[] categories = {"KOREAN", "JAPANESE", "CHINESE", "ITALIAN"};

		for (String category : categories) {
			when(authentication.getPrincipal()).thenReturn(ownerUser);
			when(storeRepository.existsByOwnerId(ownerId)).thenReturn(false);

			CreateStoreRequest request = CreateStoreRequest.builder()
				.businessNumber("123-45-67890")
				.name("소문난 국밥집")
				.address("서울시 강남구 역삼동")
				.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
				.category(category)
				.build();

			Store store = Store.builder()
				.id(UUID.randomUUID())
				.businessNumber("123-45-67890")
				.name("소문난 국밥집")
				.address("서울시 강남구 역삼동")
				.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
				.category(Category.valueOf(category))
				.rate(0.0)
				.ownerId(ownerId)
				.build();

			when(storeRepository.save(any(Store.class))).thenReturn(store);

			// when
			StoreResponse result = storeOwnerService.createStore(authentication, request);

			// then
			assertNotNull(result);
			assertEquals(category, result.getCategory());
		}
	}
}
