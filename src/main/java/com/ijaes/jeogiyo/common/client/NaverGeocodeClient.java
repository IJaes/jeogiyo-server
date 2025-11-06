package com.ijaes.jeogiyo.common.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverGeocodeClient {

	private static final Logger logger = LoggerFactory.getLogger(NaverGeocodeClient.class);

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${naver.client-id}")
	private String clientId;

	@Value("${naver.client-secret}")
	private String clientSecret;

	private static final String GEOCODING_URL = "https://map.naver.com/v5/api/geocoding";

	public Coordinates addressToCoordinates(String address) {
		try {
			logger.debug("주소 변환 요청: {}", address);

			HttpHeaders headers = new HttpHeaders();
			headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
			headers.set("X-NCP-APIGW-API-KEY", clientSecret);

			HttpEntity<String> entity = new HttpEntity<>(headers);

			String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);

			String url = GEOCODING_URL + "?query=" + encodedAddress;

			logger.debug("API 호출 URL: {}", url);

			ResponseEntity<String> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				entity,
				String.class
			);

			logger.debug("API 응답 (Raw): {}", response.getBody());

			if (response.getBody() == null) {
				logger.warn("주소 검색 응답이 비어있음: {}", address);
				throw new CustomException(ErrorCode.ADDRESS_NOT_FOUND);
			}

			// JSON 파싱
			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode addresses = root.get("addresses");

			if (addresses != null && addresses.isArray() && addresses.size() > 0) {
				JsonNode firstAddr = addresses.get(0);
				String x = firstAddr.get("x").asText();
				String y = firstAddr.get("y").asText();

				Coordinates coordinates = Coordinates.builder()
					.latitude(Double.parseDouble(y))
					.longitude(Double.parseDouble(x))
					.build();
				logger.debug("변환된 좌표: lat={}, lon={}", coordinates.getLatitude(), coordinates.getLongitude());
				return coordinates;
			} else {
				logger.warn("주소 검색 결과 없음: {}", address);
				throw new CustomException(ErrorCode.ADDRESS_NOT_FOUND);
			}


		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			logger.error("지오코딩 API 호출 중 오류 발생", e);
			throw new CustomException(ErrorCode.GEOCODING_API_ERROR);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Coordinates {
		private Double latitude;
		private Double longitude;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GeocodeResponse {
		private List<Address> addresses;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Address {
		private String x;
		private String y;
	}
}
