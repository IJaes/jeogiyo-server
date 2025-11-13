package com.ijaes.jeogiyo.common.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

	private static final String GEOCODING_URL = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode";

	public Coordinates addressToCoordinates(String address) {
		try {
			String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
			String urlString = GEOCODING_URL + "?query=" + encodedAddress;

			HttpHeaders headers = new HttpHeaders();
			headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
			headers.set("X-NCP-APIGW-API-KEY", clientSecret);
			headers.set("Accept", "application/json");
			headers.set("Accept-Charset", "UTF-8");
			headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
			headers.set("Accept-Encoding", "gzip, deflate");
			headers.set("Accept-Language", "ko-KR,ko;q=0.9");

			HttpEntity<String> entity = new HttpEntity<>(headers);

			logger.info("원본 주소: {}", address);
			logger.info("URLEncoder.encode() 결과: {}", encodedAddress);
			logger.info("인코딩된 URL: {}", urlString);
			java.net.URI uri = java.net.URI.create(urlString);
			ResponseEntity<String> response2 = restTemplate.exchange(
				uri,
				HttpMethod.GET,
				entity,
				String.class
			);

			String responseBody2 = response2.getBody();

			logger.info("NaverGeocodeClient API Response: {}\n", responseBody2);

			if (responseBody2 == null || responseBody2.trim().startsWith("<")) {
				throw new CustomException(ErrorCode.GEOCODING_API_ERROR);
			}

			JsonNode root = objectMapper.readTree(responseBody2);
			JsonNode status = root.get("status");

			if (!"OK".equals(status != null ? status.asText() : "")) {
				throw new CustomException(ErrorCode.ADDRESS_NOT_FOUND);
			}

			JsonNode addresses = root.get("addresses");

			if (addresses != null && addresses.isArray() && addresses.size() > 0) {
				JsonNode firstAddr = addresses.get(0);
				String x = firstAddr.get("x").asText();
				String y = firstAddr.get("y").asText();

				logger.info("\n### 최종 결과: URI 객체 방식 사용 ###");
				logger.info("찾은 주소: {}", firstAddr.get("roadAddress").asText());
				logger.info("좌표: x={}, y={}\n", x, y);

				return Coordinates.builder()
					.latitude(Double.parseDouble(y))
					.longitude(Double.parseDouble(x))
					.build();
			} else {
				logger.warn("NaverGeocodeClient: No addresses found for: {}", address);
				throw new CustomException(ErrorCode.ADDRESS_NOT_FOUND);
			}

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			logger.error("NaverGeocodeClient Error: ", e);
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
}
