package com.ijaes.jeogiyo.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.ijaes.jeogiyo.store.dto.request.UpdateStoreRequest;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreAdminService 테스트")
class StoreAdminServiceTest {

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private StoreAdminService storeAdminService;

	private UUID storeId;
	private UUID ownerId;
	private Store testStore;

	@BeforeEach
	void setUp() {
		storeId = UUID.randomUUID();
		ownerId = UUID.randomUUID();

		testStore = Store.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category(Category.KOREAN)
			.rate(4.5)
			.ownerId(ownerId)
			.build();
	}

	@Test
	@DisplayName("매장 정보 수정 - 성공 (모든 필드 수정)")
	void updateStore_success_allFields() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("새로운 가게명")
			.address("서울시 마포구 홍대")
			.description("새로운 설명")
			.category("JAPANESE")
			.build();

		Store updatedStore = Store.builder()
			.id(storeId)
			.businessNumber(testStore.getBusinessNumber())
			.name("새로운 가게명")
			.address("서울시 마포구 홍대")
			.description("새로운 설명")
			.category(Category.JAPANESE)
			.rate(testStore.getRate())
			.ownerId(ownerId)
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("새로운 가게명", result.getName());
		assertEquals("서울시 마포구 홍대", result.getAddress());
		assertEquals("새로운 설명", result.getDescription());
		assertEquals("JAPANESE", result.getCategory());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(storeRepository, times(1)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 정보 수정 - 부분 수정 (이름만)")
	void updateStore_partialUpdate_nameOnly() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("변경된 이름")
			.build();

		Store updatedStore = Store.builder()
			.id(storeId)
			.businessNumber(testStore.getBusinessNumber())
			.name("변경된 이름")
			.address(testStore.getAddress())
			.description(testStore.getDescription())
			.category(testStore.getCategory())
			.rate(testStore.getRate())
			.ownerId(ownerId)
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("변경된 이름", result.getName());
		assertEquals(testStore.getAddress(), result.getAddress());
		assertEquals(testStore.getDescription(), result.getDescription());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(storeRepository, times(1)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 정보 수정 - 부분 수정 (주소만)")
	void updateStore_partialUpdate_addressOnly() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.address("서울시 서초구")
			.build();

		Store updatedStore = Store.builder()
			.id(storeId)
			.businessNumber(testStore.getBusinessNumber())
			.name(testStore.getName())
			.address("서울시 서초구")
			.description(testStore.getDescription())
			.category(testStore.getCategory())
			.rate(testStore.getRate())
			.ownerId(ownerId)
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(testStore.getName(), result.getName());
		assertEquals("서울시 서초구", result.getAddress());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
	}

	@Test
	@DisplayName("매장 정보 수정 - 부분 수정 (설명만)")
	void updateStore_partialUpdate_descriptionOnly() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.description("새로운 설명")
			.build();

		Store updatedStore = Store.builder()
			.id(storeId)
			.businessNumber(testStore.getBusinessNumber())
			.name(testStore.getName())
			.address(testStore.getAddress())
			.description("새로운 설명")
			.category(testStore.getCategory())
			.rate(testStore.getRate())
			.ownerId(ownerId)
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("새로운 설명", result.getDescription());
		assertEquals(testStore.getName(), result.getName());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
	}

	@Test
	@DisplayName("매장 정보 수정 - 부분 수정 (카테고리만)")
	void updateStore_partialUpdate_categoryOnly() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.category("CHINESE")
			.build();

		Store updatedStore = Store.builder()
			.id(storeId)
			.businessNumber(testStore.getBusinessNumber())
			.name(testStore.getName())
			.address(testStore.getAddress())
			.description(testStore.getDescription())
			.category(Category.CHINESE)
			.rate(testStore.getRate())
			.ownerId(ownerId)
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("CHINESE", result.getCategory());
		assertEquals(testStore.getName(), result.getName());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
	}

	@Test
	@DisplayName("매장 정보 수정 - 매장이 없음")
	void updateStore_storeNotFound() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("새로운 이름")
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeAdminService.updateStore(storeId, request);
		});

		assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
		verify(storeRepository, times(0)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 정보 수정 - 잘못된 카테고리")
	void updateStore_invalidCategory() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.category("INVALID_CATEGORY")
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeAdminService.updateStore(storeId, request);
		});

		assertEquals(ErrorCode.INVALID_CATEGORY, exception.getErrorCode());
		verify(storeRepository, times(0)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 정보 수정 - 모든 필드 null (요청)")
	void updateStore_allFieldsNull() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(testStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(testStore.getName(), result.getName());
		assertEquals(testStore.getAddress(), result.getAddress());
		assertEquals(testStore.getDescription(), result.getDescription());
		assertEquals("KOREAN", result.getCategory());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(storeRepository, times(1)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 정보 수정 - 모든 카테고리 지원")
	void updateStore_allCategories() {
		// given
		String[] categories = {"KOREAN", "JAPANESE", "CHINESE", "ITALIAN"};

		for (String category : categories) {
			UpdateStoreRequest request = UpdateStoreRequest.builder()
				.category(category)
				.build();

			Store updatedStore = Store.builder()
				.id(storeId)
				.businessNumber(testStore.getBusinessNumber())
				.name(testStore.getName())
				.address(testStore.getAddress())
				.description(testStore.getDescription())
				.category(Category.valueOf(category))
				.rate(testStore.getRate())
				.ownerId(ownerId)
				.build();

			when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
			when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

			// when
			StoreResponse result = storeAdminService.updateStore(storeId, request);

			// then
			assertNotNull(result);
			assertEquals(category, result.getCategory());
		}
	}

	@Test
	@DisplayName("매장 정보 수정 - 소문자 카테고리 (자동 변환)")
	void updateStore_lowercaseCategory() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.category("korean")
			.build();

		Store updatedStore = Store.builder()
			.id(storeId)
			.businessNumber(testStore.getBusinessNumber())
			.name(testStore.getName())
			.address(testStore.getAddress())
			.description(testStore.getDescription())
			.category(Category.KOREAN)
			.rate(testStore.getRate())
			.ownerId(ownerId)
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("KOREAN", result.getCategory());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(storeRepository, times(1)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 정보 수정 - 빈 문자열은 수정하지 않음 (이름)")
	void updateStore_emptyNameNotUpdated() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("")
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(testStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(testStore.getName(), result.getName());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
	}

	@Test
	@DisplayName("매장 정보 수정 - 여백만 있는 문자열은 수정하지 않음 (주소)")
	void updateStore_whitespaceAddressNotUpdated() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.address("   ")
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(testStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(testStore.getAddress(), result.getAddress());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
	}

	// ==================== 소프트 삭제 테스트 ====================

	@Test
	@DisplayName("매장 소프트 삭제 - 성공")
	void deleteStore_success() {
		// given
		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(testStore);

		// when
		storeAdminService.deleteStore(storeId);

		// then
		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(storeRepository, times(1)).save(any(Store.class));
		assertTrue(testStore.isDeleted());
		assertNotNull(testStore.getDeletedAt());
	}

	@Test
	@DisplayName("매장 소프트 삭제 - 매장이 없음")
	void deleteStore_storeNotFound() {
		// given
		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeAdminService.deleteStore(storeId);
		});

		assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
		verify(storeRepository, times(0)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 소프트 삭제 - deletedAt 필드 확인")
	void deleteStore_verifyDeletedFlag() {
		// given
		Store deletedStore = Store.builder()
			.id(storeId)
			.businessNumber(testStore.getBusinessNumber())
			.name(testStore.getName())
			.address(testStore.getAddress())
			.description(testStore.getDescription())
			.category(testStore.getCategory())
			.rate(testStore.getRate())
			.ownerId(ownerId)
			.build();

		when(storeRepository.findByIdNotDeleted(storeId)).thenReturn(Optional.of(deletedStore));
		when(storeRepository.save(any(Store.class))).thenReturn(deletedStore);

		// when
		storeAdminService.deleteStore(storeId);

		// then
		assertTrue(deletedStore.isDeleted());
		assertNotNull(deletedStore.getDeletedAt());

		verify(storeRepository, times(1)).findByIdNotDeleted(storeId);
		verify(storeRepository, times(1)).save(any(Store.class));
	}

	// ==================== 전체 매장 조회 테스트 ====================

	@Test
	@DisplayName("전체 매장 조회 - 성공 (삭제된 매장 포함)")
	void getAllStores_success() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		Store deletedStore = Store.builder()
			.id(UUID.randomUUID())
			.businessNumber("987-65-43210")
			.name("폐점한 매장")
			.address("서울시 성동구")
			.description("이미 폐점함")
			.category(Category.JAPANESE)
			.rate(3.0)
			.ownerId(UUID.randomUUID())
			.build();
		deletedStore.softDelete();

		List<Store> stores = List.of(testStore, deletedStore);
		Page<Store> storePage = new PageImpl<>(stores, pageable, 2);

		when(storeRepository.findAllIncludingDeleted(any(Pageable.class))).thenReturn(storePage);

		// when
		Page<StoreResponse> result = storeAdminService.getAllStores(0, 10, "createdAt", "DESC");

		// then
		assertNotNull(result);
		assertEquals(2, result.getTotalElements());
		assertEquals(1, result.getTotalPages());
		verify(storeRepository, times(1)).findAllIncludingDeleted(any(Pageable.class));
	}

	@Test
	@DisplayName("전체 매장 조회 - 빈 결과")
	void getAllStores_empty() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		Page<Store> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(storeRepository.findAllIncludingDeleted(any(Pageable.class))).thenReturn(emptyPage);

		// when
		Page<StoreResponse> result = storeAdminService.getAllStores(0, 10, "createdAt", "DESC");

		// then
		assertNotNull(result);
		assertEquals(0, result.getTotalElements());
		verify(storeRepository, times(1)).findAllIncludingDeleted(any(Pageable.class));
	}

	@Test
	@DisplayName("전체 매장 조회 - 페이지네이션")
	void getAllStores_pagination() {
		// given
		Pageable pageable = PageRequest.of(1, 5);
		List<Store> storeList = List.of(testStore);
		Page<Store> storePage = new PageImpl<>(storeList, pageable, 15);

		when(storeRepository.findAllIncludingDeleted(any(Pageable.class))).thenReturn(storePage);

		// when
		Page<StoreResponse> result = storeAdminService.getAllStores(1, 5, "createdAt", "DESC");

		// then
		assertNotNull(result);
		assertEquals(15, result.getTotalElements());
		assertEquals(3, result.getTotalPages());
		assertEquals(1, result.getNumber());
		assertEquals(5, result.getSize());
		verify(storeRepository, times(1)).findAllIncludingDeleted(any(Pageable.class));
	}
}
