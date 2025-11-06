package com.ijaes.jeogiyo.common.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

	private final RestTemplate restTemplate;

	@Value("${naver.client-id}")
	private String clientId;

	@Value("${naver.client-secret}")
	private String clientSecret;

	private static final String GEOCODING_URL = "https://map.naver.com/v5/api/geocoding";

	public Coordinates addressToCoordinates(String address) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
			headers.set("X-NCP-APIGW-API-KEY", clientSecret);

			HttpEntity<String> entity = new HttpEntity<>(headers);

			String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);

			String url = GEOCODING_URL + "?query=" + encodedAddress;

			ResponseEntity<GeocodeResponse> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				entity,
				GeocodeResponse.class
			);

			if (response.getBody() != null && response.getBody().getAddresses() != null
				&& !response.getBody().getAddresses().isEmpty()) {
				Address addr = response.getBody().getAddresses().get(0);
				return Coordinates.builder()
					.latitude(Double.parseDouble(addr.getY()))
					.longitude(Double.parseDouble(addr.getX()))
					.build();
			} else {
				throw new CustomException(ErrorCode.ADDRESS_NOT_FOUND);
			}


		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GEOCODING_API_ERROR);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Coordinates {
		private Double latitude;
		private Double longitude;

		@Builder
		public Coordinates(Double latitude, Double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
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
