package com.ijaes.jeogiyo.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ijaes.jeogiyo.store.dto.request.UpdateStoreRequest;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.service.StoreAdminService;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreAdminController 테스트")
class StoreAdminControllerTest {

	@Mock
	private StoreAdminService storeAdminService;

	@InjectMocks
	private StoreAdminController storeAdminController;

	private UUID storeId;
	private UUID ownerId;

	@BeforeEach
	void setUp() {
		storeId = UUID.randomUUID();
		ownerId = UUID.randomUUID();
	}

	@Test
	@DisplayName("전체 매장 조회 API - 성공")
	void getAllStores_success() {
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

		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of(store1));

		when(storeAdminService.getAllStores(0, 10, "createdAt", "DESC"))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeAdminController.getAllStores(0, 10, "createdAt", "DESC");

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals(1, result.getBody().getTotalElements());
		assertEquals(storeId, result.getBody().getContent().get(0).getId());
		verify(storeAdminService, times(1)).getAllStores(0, 10, "createdAt", "DESC");
	}

	@Test
	@DisplayName("전체 매장 조회 API - 다양한 정렬 기준")
	void getAllStores_differentSortBy() {
		// given
		StoreResponse store = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("국밥집")
			.address("서울")
			.description("맛있음")
			.category("KOREAN")
			.rate(3.5)
			.ownerId(ownerId)
			.build();

		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of(store));

		when(storeAdminService.getAllStores(0, 10, "name", "ASC"))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeAdminController.getAllStores(0, 10, "name", "ASC");

		// then
		assertNotNull(result.getBody());
		assertEquals(1, result.getBody().getTotalElements());
		verify(storeAdminService, times(1)).getAllStores(0, 10, "name", "ASC");
	}

	@Test
	@DisplayName("매장 정보 수정 API - 성공")
	void updateStore_success() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("새로운 국밥집")
			.description("더 맛있는 국물")
			.address("서울시 서초구")
			.build();

		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("새로운 국밥집")
			.address("서울시 서초구")
			.description("더 맛있는 국물")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		when(storeAdminService.updateStore(storeId, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeAdminController.updateStore(storeId, request);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals("새로운 국밥집", result.getBody().getName());
		assertEquals("더 맛있는 국물", result.getBody().getDescription());
		verify(storeAdminService, times(1)).updateStore(storeId, request);
	}

	@Test
	@DisplayName("매장 소프트 삭제 API - 성공")
	void deleteStore_success() {
		// given
		// when
		ResponseEntity<Void> result = storeAdminController.deleteStore(storeId);

		// then
		assertNotNull(result);
		assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
		verify(storeAdminService, times(1)).deleteStore(storeId);
	}

	@Test
	@DisplayName("전체 매장 조회 API - 기본 페이지네이션 파라미터")
	void getAllStores_defaultPagination() {
		// given
		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of());

		when(storeAdminService.getAllStores(0, 10, "createdAt", "DESC"))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeAdminController.getAllStores(0, 10, "createdAt", "DESC");

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertEquals(0, result.getBody().getTotalElements());
	}

	@Test
	@DisplayName("매장 정보 수정 API - 이름만 수정")
	void updateStore_nameOnly() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("변경된 이름")
			.build();

		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("변경된 이름")
			.address("서울시 강남구")
			.description("기존 설명")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		when(storeAdminService.updateStore(storeId, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeAdminController.updateStore(storeId, request);

		// then
		assertEquals("변경된 이름", result.getBody().getName());
	}

	@Test
	@DisplayName("매장 정보 수정 API - 설명만 수정")
	void updateStore_descriptionOnly() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.description("새로운 설명")
			.build();

		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구")
			.description("새로운 설명")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		when(storeAdminService.updateStore(storeId, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeAdminController.updateStore(storeId, request);

		// then
		assertEquals("새로운 설명", result.getBody().getDescription());
	}

	@Test
	@DisplayName("전체 매장 조회 API - 올바른 응답 형식")
	void getAllStores_correctResponseFormat() {
		// given
		Page<StoreResponse> expectedPage = new PageImpl<>(java.util.List.of());

		when(storeAdminService.getAllStores(anyInt(), anyInt(), anyString(), anyString()))
			.thenReturn(expectedPage);

		// when
		ResponseEntity<Page<StoreResponse>> result = storeAdminController.getAllStores(0, 10, "createdAt", "DESC");

		// then
		assertNotNull(result);
		assertTrue(result.getStatusCode().is2xxSuccessful());
		assertNotNull(result.getBody());
	}
}
