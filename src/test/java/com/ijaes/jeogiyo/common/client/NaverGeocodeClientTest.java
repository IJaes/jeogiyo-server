package com.ijaes.jeogiyo.common.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("NaverGeocodeClient 테스트")
class NaverGeocodeClientTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private NaverGeocodeClient naverGeocodeClient;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(naverGeocodeClient, "objectMapper", objectMapper);
		ReflectionTestUtils.setField(naverGeocodeClient, "clientId", "test-id");
		ReflectionTestUtils.setField(naverGeocodeClient, "clientSecret", "test-secret");
	}

	@Test
	@DisplayName("주소를 좌표로 정상 변환")
	void addressToCoordinates_success() {
		// given
		String address = "서울시 강남구 역삼동";
		String mockResponse = """
			{
				"status": "OK",
				"meta": {"totalCount": 1, "count": 1},
				"addresses": [
					{
						"roadAddress": "서울특별시 강남구 테헤란로 123",
						"jibunAddress": "서울특별시 강남구 역삼동 123",
						"englishAddress": "123, Teheran-ro, Gangnam-gu, Seoul, South Korea",
						"x": "127.02761",
						"y": "37.49793",
						"distance": 0
					}
				],
				"errorMessage": ""
			}
			""";

		when(restTemplate.exchange(
			org.mockito.ArgumentMatchers.any(java.net.URI.class),
			eq(HttpMethod.GET),
			org.mockito.ArgumentMatchers.any(),
			eq(String.class)
		)).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

		// when
		NaverGeocodeClient.Coordinates result = naverGeocodeClient.addressToCoordinates(address);

		// then
		assertNotNull(result);
		assertEquals(37.49793, result.getLatitude());
		assertEquals(127.02761, result.getLongitude());
	}

	@Test
	@DisplayName("주소 검색 결과 없을 때 ADDRESS_NOT_FOUND 에러")
	void addressToCoordinates_notFound() {
		// given
		String address = "존재하지않는주소";
		String mockResponse = """
			{
				"status": "OK",
				"meta": {"totalCount": 0, "count": 0},
				"addresses": [],
				"errorMessage": ""
			}
			""";

		when(restTemplate.exchange(
			org.mockito.ArgumentMatchers.any(java.net.URI.class),
			eq(HttpMethod.GET),
			org.mockito.ArgumentMatchers.any(),
			eq(String.class)
		)).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			naverGeocodeClient.addressToCoordinates(address);
		});

		assertEquals(ErrorCode.ADDRESS_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("API 호출 실패 시 GEOCODING_API_ERROR 에러")
	void addressToCoordinates_apiError() {
		// given
		String address = "서울시 강남구";

		when(restTemplate.exchange(
			org.mockito.ArgumentMatchers.any(java.net.URI.class),
			eq(HttpMethod.GET),
			org.mockito.ArgumentMatchers.any(),
			eq(String.class)
		)).thenThrow(new RuntimeException("API Connection Error"));

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			naverGeocodeClient.addressToCoordinates(address);
		});

		assertEquals(ErrorCode.GEOCODING_API_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("API가 HTML 응답을 반환할 때 GEOCODING_API_ERROR 에러")
	void addressToCoordinates_htmlResponse() {
		// given
		String address = "서울시 강남구";
		String htmlResponse = "<html><body>Error</body></html>";

		when(restTemplate.exchange(
			anyString(),
			eq(HttpMethod.GET),
			org.mockito.ArgumentMatchers.any(),
			eq(String.class)
		)).thenReturn(new ResponseEntity<>(htmlResponse, HttpStatus.OK));

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			naverGeocodeClient.addressToCoordinates(address);
		});

		assertEquals(ErrorCode.GEOCODING_API_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("API가 INVALID_REQUEST 상태를 반환할 때 ADDRESS_NOT_FOUND 에러")
	void addressToCoordinates_invalidRequest() {
		// given
		String address = "서울시 강남구";
		String mockResponse = """
			{
				"status": "INVALID_REQUEST",
				"errorMessage": "query is INVALID"
			}
			""";

		when(restTemplate.exchange(
			org.mockito.ArgumentMatchers.any(java.net.URI.class),
			eq(HttpMethod.GET),
			org.mockito.ArgumentMatchers.any(),
			eq(String.class)
		)).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			naverGeocodeClient.addressToCoordinates(address);
		});

		assertEquals(ErrorCode.ADDRESS_NOT_FOUND, exception.getErrorCode());
	}
}
