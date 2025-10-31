package com.ijaes.jeogiyo.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.data.domain.Sort;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreUserService 테스트")
class StoreUserServiceTest {

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private StoreUserService storeUserService;

	private Store testStore;
	private UUID storeId;
	private UUID ownerId;

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
	@DisplayName("모든 매장 조회 - 페이지네이션")
	void getAllStores_withPagination() {
		// given
		int page = 0;
		int size = 10;
		String sortBy = "rate";
		String direction = "DESC";

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
		Page<Store> storePage = new PageImpl<>(List.of(testStore), pageable, 1);

		when(storeRepository.findAll(any(Pageable.class))).thenReturn(storePage);

		// when
		Page<StoreResponse> result = storeUserService.getAllStores(page, size, sortBy, direction);

		// then
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("소문난 국밥집", result.getContent().get(0).getName());
		assertEquals(4.5, result.getContent().get(0).getRate());
	}

	@Test
	@DisplayName("모든 매장 조회 - 오름차순 정렬")
	void getAllStores_withAscendingSort() {
		// given
		int page = 0;
		int size = 10;
		String sortBy = "name";
		String direction = "ASC";

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));
		Page<Store> storePage = new PageImpl<>(List.of(testStore), pageable, 1);

		when(storeRepository.findAll(any(Pageable.class))).thenReturn(storePage);

		// when
		Page<StoreResponse> result = storeUserService.getAllStores(page, size, sortBy, direction);

		// then
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("소문난 국밥집", result.getContent().get(0).getName());
	}

	@Test
	@DisplayName("모든 매장 조회 - 빈 페이지")
	void getAllStores_emptyPage() {
		// given
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "rate"));
		Page<Store> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(storeRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

		// when
		Page<StoreResponse> result = storeUserService.getAllStores(0, 10, "rate", "DESC");

		// then
		assertNotNull(result);
		assertEquals(0, result.getTotalElements());
		assertTrue(result.getContent().isEmpty());
	}

	@Test
	@DisplayName("매장 상세 조회 - 성공")
	void getStoreDetail_success() {
		// given
		StoreDetailResponse mockDetailResponse = StoreDetailResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category("KOREAN")
			.rate(4.5)
			.owner(StoreDetailResponse.OwnerInfo.builder()
				.id(ownerId)
				.name("김사장")
				.username("owner123")
				.phoneNumber("010-1234-5678")
				.address("서울시 강남구 역삼동")
				.build())
			.build();

		when(storeRepository.findStoreDetailById(storeId)).thenReturn(Optional.of(mockDetailResponse));

		// when
		StoreDetailResponse result = storeUserService.getStoreDetail(storeId);

		// then
		assertNotNull(result);
		assertEquals(storeId, result.getId());
		assertEquals("소문난 국밥집", result.getName());
		assertEquals("김사장", result.getOwner().getName());
	}

	@Test
	@DisplayName("매장 상세 조회 - 존재하지 않는 매장")
	void getStoreDetail_notFound() {
		// given
		UUID nonExistentId = UUID.randomUUID();
		when(storeRepository.findStoreDetailById(nonExistentId)).thenReturn(Optional.empty());

		// when & then
		assertThrows(CustomException.class, () -> {
			storeUserService.getStoreDetail(nonExistentId);
		});
	}

	@Test
	@DisplayName("매장 상세 조회 - 에러 코드 확인")
	void getStoreDetail_errorCode() {
		// given
		UUID nonExistentId = UUID.randomUUID();
		when(storeRepository.findStoreDetailById(nonExistentId)).thenReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			storeUserService.getStoreDetail(nonExistentId);
		});

		assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("모든 매장 조회 - 여러 매장")
	void getAllStores_multipleStores() {
		// given
		Store store2 = Store.builder()
			.id(UUID.randomUUID())
			.businessNumber("456-78-90123")
			.name("유명한 라면집")
			.address("서울시 마포구 홍대")
			.description("맛있는 라면")
			.category(Category.JAPANESE)
			.rate(4.2)
			.ownerId(UUID.randomUUID())
			.build();

		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "rate"));
		Page<Store> storePage = new PageImpl<>(List.of(testStore, store2), pageable, 2);

		when(storeRepository.findAll(any(Pageable.class))).thenReturn(storePage);

		// when
		Page<StoreResponse> result = storeUserService.getAllStores(0, 10, "rate", "DESC");

		// then
		assertEquals(2, result.getTotalElements());
		assertEquals("소문난 국밥집", result.getContent().get(0).getName());
		assertEquals("유명한 라면집", result.getContent().get(1).getName());
	}
}
