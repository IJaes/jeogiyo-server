package com.ijaes.jeogiyo.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.ijaes.jeogiyo.store.dto.request.CreateStoreRequest;
import com.ijaes.jeogiyo.store.dto.request.UpdateStoreRequest;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.service.StoreOwnerService;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreOwnerController 테스트")
class StoreOwnerControllerTest {

	@Mock
	private StoreOwnerService storeOwnerService;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private StoreOwnerController storeOwnerController;

	private UUID ownerId;
	private UUID storeId;
	private User testOwner;

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
	}

	@Test
	@DisplayName("매장 생성 API - 성공")
	void createStore_success() {
		// given
		CreateStoreRequest request = CreateStoreRequest.builder()
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category("KOREAN")
			.build();

		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category("KOREAN")
			.rate(0.0)
			.ownerId(ownerId)
			.build();

		when(storeOwnerService.createStore(authentication, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeOwnerController.createStore(authentication, request);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals(storeId, result.getBody().getId());
		assertEquals("소문난 국밥집", result.getBody().getName());
		assertEquals("KOREAN", result.getBody().getCategory());
		assertEquals(0.0, result.getBody().getRate());
	}

	@Test
	@DisplayName("매장 조회 API - 성공")
	void myStore_success() {
		// given
		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("소문난 국밥집")
			.address("서울시 강남구 역삼동")
			.description("뜨근뜨끈한 국물 한 사발 먹고 가세요")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		when(storeOwnerService.myStore(authentication))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeOwnerController.myStore(authentication);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals(storeId, result.getBody().getId());
		assertEquals("소문난 국밥집", result.getBody().getName());
		assertEquals(4.5, result.getBody().getRate());
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

		when(storeOwnerService.updateStore(authentication, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeOwnerController.updateStore(authentication, request);

		// then
		assertNotNull(result);
		assertEquals(200, result.getStatusCodeValue());
		assertNotNull(result.getBody());
		assertEquals("새로운 국밥집", result.getBody().getName());
		assertEquals("더 맛있는 국물", result.getBody().getDescription());
		assertEquals("서울시 서초구", result.getBody().getAddress());
	}

	@Test
	@DisplayName("매장 생성 API - 응답에 필수 정보 포함")
	void createStore_responseContainsRequiredFields() {
		// given
		CreateStoreRequest request = CreateStoreRequest.builder()
			.businessNumber("987-65-43210")
			.name("맛있는 국밥")
			.address("서울시 마포구")
			.description("최고의 국밥")
			.category("KOREAN")
			.build();

		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("987-65-43210")
			.name("맛있는 국밥")
			.address("서울시 마포구")
			.description("최고의 국밥")
			.category("KOREAN")
			.rate(0.0)
			.ownerId(ownerId)
			.build();

		when(storeOwnerService.createStore(authentication, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeOwnerController.createStore(authentication, request);

		// then
		assertNotNull(result.getBody().getId());
		assertNotNull(result.getBody().getBusinessNumber());
		assertNotNull(result.getBody().getName());
		assertNotNull(result.getBody().getAddress());
		assertNotNull(result.getBody().getDescription());
		assertNotNull(result.getBody().getCategory());
		assertNotNull(result.getBody().getRate());
		assertNotNull(result.getBody().getOwnerId());
	}

	@Test
	@DisplayName("매장 생성 API - 이름 반영")
	void createStore_nameReflection() {
		// given
		CreateStoreRequest request = CreateStoreRequest.builder()
			.businessNumber("123-45-67890")
			.name("특별한 국밥집")
			.address("서울시 강남구")
			.description("맛있는 국밥")
			.category("KOREAN")
			.build();

		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("특별한 국밥집")
			.address("서울시 강남구")
			.description("맛있는 국밥")
			.category("KOREAN")
			.rate(0.0)
			.ownerId(ownerId)
			.build();

		when(storeOwnerService.createStore(authentication, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeOwnerController.createStore(authentication, request);

		// then
		assertEquals("특별한 국밥집", result.getBody().getName());
	}

	@Test
	@DisplayName("매장 수정 API - 이름 반영")
	void updateStore_nameReflection() {
		// given
		UpdateStoreRequest request = UpdateStoreRequest.builder()
			.name("최고의 국밥집")
			.build();

		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("최고의 국밥집")
			.address("서울시 강남구")
			.description("뜨근뜨끈한 국물")
			.category("KOREAN")
			.rate(4.5)
			.ownerId(ownerId)
			.build();

		when(storeOwnerService.updateStore(authentication, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeOwnerController.updateStore(authentication, request);

		// then
		assertEquals("최고의 국밥집", result.getBody().getName());
	}

	@Test
	@DisplayName("매장 생성 API - 올바른 응답 형식")
	void createStore_correctResponseFormat() {
		// given
		CreateStoreRequest request = CreateStoreRequest.builder()
			.businessNumber("123-45-67890")
			.name("국밥")
			.address("서울")
			.description("맛있음")
			.category("KOREAN")
			.build();

		StoreResponse expectedResponse = StoreResponse.builder()
			.id(storeId)
			.businessNumber("123-45-67890")
			.name("국밥")
			.address("서울")
			.description("맛있음")
			.category("KOREAN")
			.rate(0.0)
			.ownerId(ownerId)
			.build();

		when(storeOwnerService.createStore(authentication, request))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<StoreResponse> result = storeOwnerController.createStore(authentication, request);

		// then
		assertNotNull(result);
		assertTrue(result.getStatusCode().is2xxSuccessful());
	}
}
