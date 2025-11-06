package com.ijaes.jeogiyo.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.service.StoreUserService;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreUserController 테스트")
class StoreUserControllerTest {

	@Mock
	private StoreUserService storeUserService;

	@InjectMocks
	private StoreUserController storeUserController;

	private UUID storeId;
	private UUID ownerId;
	private User testOwner;

	@BeforeEach
	void setUp() {
		storeId = UUID.randomUUID();
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
	}

	@Test
	@DisplayName("전체 매장 조회 API - 성공")
	void getAllStores_success() {
		// given
		StoreResponse store = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구")
			.description("맛있는 국밥")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of(store));

		when(storeUserService.getAllStores(0, 10, "rate", "DESC"))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.getAllStores(0, 10, "rate", "DESC");

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals(1, result.getBody().getTotalElements());
		assertEquals(storeId, result.getBody().getContent().get(0).getId());
		assertEquals("소문난 국밥집", result.getBody().getContent().get(0).getName());
		verify(storeUserService, times(1)).getAllStores(0, 10, "rate", "DESC");
	}

	@Test
	@DisplayName("전체 매장 조회 API - 기본 파라미터")
	void getAllStores_defaultParameters() {
		// given
		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of());

		when(storeUserService.getAllStores(0, 10, "rate", "DESC"))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.getAllStores(0, 10, "rate", "DESC");

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertEquals(0, result.getBody().getTotalElements());
	}

	@Test
	@DisplayName("전체 매장 조회 API - 다양한 정렬 기준")
	void getAllStores_differentSortBy() {
		// given
		StoreResponse store = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("국밥")
			.address("서울")
			.description("맛있음")
			.category("KOREAN")
			.rate(3.5)
			.ownerId(ownerId)
			.build();

		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of(store));

		when(storeUserService.getAllStores(0, 10, "name", "ASC"))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.getAllStores(0, 10, "name", "ASC");

		// then
		assertNotNull(result.getBody());
		assertEquals(1, result.getBody().getTotalElements());
		verify(storeUserService, times(1)).getAllStores(0, 10, "name", "ASC");
	}

	@Test
	@DisplayName("매장 상세 조회 API - 성공")
	void getStoreDetail_success() {
		// given
		StoreDetailResponse.OwnerInfo ownerInfo = StoreDetailResponse.OwnerInfo.builder()
			.id(ownerId)
			.name("사장님")
			.username("owner@test.com")
			.phoneNumber("010-1234-5678")
			.address("서울시 강남구")
			.build();

		StoreDetailResponse expectedResponse = StoreDetailResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구")
			.description("맛있는 국밥")
			.category("KOREAN")
			.rate(4.5)
			.owner(ownerInfo)
			.build();

		when(storeUserService.getStoreDetail(storeId))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreDetailResponse> result = storeUserController.getStoreDetail(storeId);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals(storeId, result.getBody().getId());
		assertEquals("소문난 국밥집", result.getBody().getName());
		assertEquals("사장님", result.getBody().getOwner().getName());
		verify(storeUserService, times(1)).getStoreDetail(storeId);
	}

	@Test
	@DisplayName("매장 상세 조회 API - 사장님 정보 포함")
	void getStoreDetail_ownerInfoIncluded() {
		// given
		StoreDetailResponse.OwnerInfo ownerInfo = StoreDetailResponse.OwnerInfo.builder()
			.id(ownerId)
			.name("김사장")
			.username("owner123@test.com")
			.phoneNumber("010-0000-0000")
			.address("서울시 마포구")
			.build();

		StoreDetailResponse expectedResponse = StoreDetailResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("국밥집")
			.address("서울")
			.description("맛있음")
			.category("KOREAN")
			.rate(4.5)
			.owner(ownerInfo)
			.build();

		when(storeUserService.getStoreDetail(storeId))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreDetailResponse> result = storeUserController.getStoreDetail(storeId);

		// then
		assertNotNull(result.getBody());
		assertEquals("김사장", result.getBody().getOwner().getName());
		assertEquals("010-0000-0000", result.getBody().getOwner().getPhoneNumber());
		assertEquals("서울시 마포구", result.getBody().getOwner().getAddress());
	}

	@Test
	@DisplayName("전체 매장 조회 API - 다양한 페이지 크기")
	void getAllStores_differentPageSize() {
		// given
		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of());

		when(storeUserService.getAllStores(0, 20, "rate", "DESC"))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.getAllStores(0, 20, "rate", "DESC");

		// then
		assertNotNull(result);
		verify(storeUserService, times(1)).getAllStores(0, 20, "rate", "DESC");
	}

	@Test
	@DisplayName("전체 매장 조회 API - 올바른 응답 형식")
	void getAllStores_correctResponseFormat() {
		// given
		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of());

		when(storeUserService.getAllStores(anyInt(), anyInt(), anyString(), anyString()))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.getAllStores(0, 10, "rate", "DESC");

		// then
		assertNotNull(result);
		assertTrue(result.getStatusCode().is2xxSuccessful());
		assertNotNull(result.getBody());
	}

	@Test
	@DisplayName("매장 상세 조회 API - 필수 정보 포함")
	void getStoreDetail_requiredFieldsIncluded() {
		// given
		StoreDetailResponse.OwnerInfo ownerInfo = StoreDetailResponse.OwnerInfo.builder()
			.id(ownerId)
			.name("사장")
			.username("owner@test.com")
			.phoneNumber("010-0000-0000")
			.address("서울")
			.build();

		StoreDetailResponse expectedResponse = StoreDetailResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("국밥")
			.address("서울")
			.description("맛있음")
			.category("KOREAN")
			.rate(4.5)
			.owner(ownerInfo)
			.build();

		when(storeUserService.getStoreDetail(storeId))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreDetailResponse> result = storeUserController.getStoreDetail(storeId);

		// then
		assertNotNull(result.getBody().getId());
		assertNotNull(result.getBody().getName());
		assertNotNull(result.getBody().getAddress());
		assertNotNull(result.getBody().getCategory());
		assertNotNull(result.getBody().getRate());
		assertNotNull(result.getBody().getOwner().getName());
	}

	@Test
	@DisplayName("매장 검색 API - 매장명으로 검색 성공")
	void searchStores_byStoreName_success() {
		// given
		StoreResponse store = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구")
			.description("맛있는 국밥")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of(store));

		when(storeUserService.searchStores("국밥", 0, 10))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.searchStores("국밥", 0, 10);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals(1, result.getBody().getTotalElements());
		assertEquals("소문난 국밥집", result.getBody().getContent().get(0).getName());
		verify(storeUserService, times(1)).searchStores("국밥", 0, 10);
	}

	@Test
	@DisplayName("매장 검색 API - 메뉴명으로 검색 성공")
	void searchStores_byMenuName_success() {
		// given
		StoreResponse store = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구")
			.description("맛있는 국밥")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of(store));

		when(storeUserService.searchStores("김밥", 0, 10))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.searchStores("김밥", 0, 10);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertEquals(1, result.getBody().getTotalElements());
		verify(storeUserService, times(1)).searchStores("김밥", 0, 10);
	}

	@Test
	@DisplayName("매장 검색 API - 검색 결과 없음")
	void searchStores_noResult() {
		// given
		Page<StoreResponse> emptyPage = new PageImpl<>(java.util.List.of());

		when(storeUserService.searchStores("존재하지않음", 0, 10))
			.thenReturn(emptyPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.searchStores("존재하지않음", 0, 10);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertEquals(0, result.getBody().getTotalElements());
		assertTrue(result.getBody().getContent().isEmpty());
	}

	@Test
	@DisplayName("매장 검색 API - 여러 검색 결과")
	void searchStores_multipleResults() {
		// given
		StoreResponse store1 = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구")
			.description("맛있는 국밥")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		UUID store2Id = UUID.randomUUID();
		StoreResponse store2 = StoreResponse.builder()
			.id(store2Id)
			.businessNumber("456-78-90123")
			.name("유명한 국수당")
			.address("서울시 마포구")
			.description("맛있는 국수")
			.category("KOREAN")
			.rate(4.2)
			.ownerId(UUID.randomUUID())
			.build();

		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of(store1, store2));

		when(storeUserService.searchStores("국", 0, 10))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.searchStores("국", 0, 10);

		// then
		assertEquals(2, result.getBody().getTotalElements());
		assertEquals("소문난 국밥집", result.getBody().getContent().get(0).getName());
		assertEquals("유명한 국수당", result.getBody().getContent().get(1).getName());
	}

	@Test
	@DisplayName("매장 검색 API - 페이지네이션")
	void searchStores_withPagination() {
		// given
		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of(), PageRequest.of(1, 5), 10);

		when(storeUserService.searchStores("국", 1, 5))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.searchStores("국", 1, 5);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertEquals(10, result.getBody().getTotalElements());
		verify(storeUserService, times(1)).searchStores("국", 1, 5);
	}

	@Test
	@DisplayName("매장 검색 API - 기본 페이지 파라미터")
	void searchStores_defaultPageParameters() {
		// given
		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of());

		when(storeUserService.searchStores("국", 0, 10))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.searchStores("국", 0, 10);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		verify(storeUserService, times(1)).searchStores("국", 0, 10);
	}

	@Test
	@DisplayName("매장 검색 API - 올바른 응답 형식")
	void searchStores_correctResponseFormat() {
		// given
		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of());

		when(storeUserService.searchStores(anyString(), anyInt(), anyInt()))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeUserController.searchStores("검색어", 0, 10);

		// then
		assertNotNull(result);
		assertTrue(result.getStatusCode().is2xxSuccessful());
		assertNotNull(result.getBody());
	}
}
