package com.ijaes.jeogiyo.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("새로운 가게명", result.getName());
		assertEquals("서울시 마포구 홍대", result.getAddress());
		assertEquals("새로운 설명", result.getDescription());
		assertEquals("JAPANESE", result.getCategory());

		verify(storeRepository, times(1)).findById(storeId);
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

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("변경된 이름", result.getName());
		assertEquals(testStore.getAddress(), result.getAddress());
		assertEquals(testStore.getDescription(), result.getDescription());

		verify(storeRepository, times(1)).findById(storeId);
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

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(testStore.getName(), result.getName());
		assertEquals("서울시 서초구", result.getAddress());

		verify(storeRepository, times(1)).findById(storeId);
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

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("새로운 설명", result.getDescription());
		assertEquals(testStore.getName(), result.getName());

		verify(storeRepository, times(1)).findById(storeId);
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

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("CHINESE", result.getCategory());
		assertEquals(testStore.getName(), result.getName());

		verify(storeRepository, times(1)).findById(storeId);
	}

	@Test
	@DisplayName("매장 정보 수정 - 매장이 없음")
	void updateStore_storeNotFound() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("새로운 이름")
			.build();

		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

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

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));

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

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(testStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(testStore.getName(), result.getName());
		assertEquals(testStore.getAddress(), result.getAddress());
		assertEquals(testStore.getDescription(), result.getDescription());
		assertEquals("KOREAN", result.getCategory());

		verify(storeRepository, times(1)).findById(storeId);
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

			when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
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

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals("KOREAN", result.getCategory());

		verify(storeRepository, times(1)).findById(storeId);
		verify(storeRepository, times(1)).save(any(Store.class));
	}

	@Test
	@DisplayName("매장 정보 수정 - 빈 문자열은 수정하지 않음 (이름)")
	void updateStore_emptyNameNotUpdated() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("")
			.build();

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(testStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(testStore.getName(), result.getName());

		verify(storeRepository, times(1)).findById(storeId);
	}

	@Test
	@DisplayName("매장 정보 수정 - 여백만 있는 문자열은 수정하지 않음 (주소)")
	void updateStore_whitespaceAddressNotUpdated() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.address("   ")
			.build();

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(testStore));
		when(storeRepository.save(any(Store.class))).thenReturn(testStore);

		// when
		StoreResponse result = storeAdminService.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(testStore.getAddress(), result.getAddress());

		verify(storeRepository, times(1)).findById(storeId);
	}
}
