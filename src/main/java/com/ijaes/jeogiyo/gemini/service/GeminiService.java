package com.ijaes.jeogiyo.gemini.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GeminiService {
	private static final String GEMINI_MODEL = "gemini-2.5-flash";
	private static final String GEMINI_MODEL_REVIEW = "gemini-1.5-flash-latest";
	private static final String GEMINI_API_URL_REVIEW = "https://generativelanguage.googleapis.com/v1/models/{model}:generateContent";
	private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
	private static final String SAFETY_THRESHOLD = "HIGH"; //4단계 중 가장 높은 HIGH일 때를 임계치로 설정

	@Value("${gemini.api-key}")
	private String apiKey;

	private final RestClient restClient;

	public GeminiService(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
	}

	public String generateMenuDescription(String menuName) {

		if (menuName == null || menuName.trim().isEmpty()) {
			return null;
		}

		try {
			String prompt = "너는 식당의 특정 메뉴에 대한 설명을 작성하는 역할이야. 손님이 메뉴를 고르고 싶은 마음이 들게끔 메뉴의 매력이 잘 드러나게 작성해주면 돼." + menuName
				+ "라는 메뉴에 대한 간단한 설명을 한 문장으로 작성해줘.";
			String requestBody = buildRequestBody(prompt);

			String url = GEMINI_API_URL.replace("{model}", GEMINI_MODEL) + "?key=" + apiKey;

			JsonNode response = restClient.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.body(requestBody)
				.retrieve()
				.body(JsonNode.class);

			if (response == null) {
				log.warn("Gemini API returned null response for menu: {}", menuName);
				return null;
			}

			String description = extractTextFromResponse(response);
			if (description != null) {
				log.info("Successfully generated description for menu: {}", menuName);
				return description.trim();
			}

			return null;
		} catch (Exception e) {
			log.error("Failed to generate description from Gemini API for menu: {}", menuName, e);
			return null;
		}
	}

	private String buildRequestBody(String prompt) {
		return """
			{
			  "contents": [{
			    "parts": [{
			      "text": "%s"
			    }]
			  }]
			}
			""".formatted(prompt.replace("\"", "\\\""));
	}

	private String extractTextFromResponse(JsonNode response) {
		try {
			return response
				.path("candidates")
				.get(0)
				.path("content")
				.path("parts")
				.get(0)
				.path("text")
				.asText();
		} catch (Exception e) {
			log.warn("Failed to extract text from Gemini response", e);
			return null;
		}
	}

	//리뷰 내용에 비속어 포함되어 있는지 여부 판단
	public boolean checkAbuseInReview(String reviewContent) {

		//리뷰 내용이 없을 때
		if (reviewContent == null || reviewContent.trim().isEmpty()) {
			return false;
		}

		try {
			//사용자가 작성한 리뷰 내용을 프롬프트에 포함
			// String prompt = "Review text: " + reviewContent;
			String prompt = """
				다음 리뷰 텍스트에 욕설, 차별, 혐오 발언 또는 괴롭힘 표현이 포함되어 있는지 판단해줘.
				리뷰 내용: "%s"
				""".formatted(reviewContent);

			//프롬프트를 JSON 구조로 포맷팅
			String requestBody = buildRequestBodyForReview(prompt);
			String url = GEMINI_API_URL_REVIEW.replace("{model}", GEMINI_MODEL_REVIEW) + "?key=" + apiKey;

			JsonNode response = restClient.post() //post 시작
				.uri(url) //보낼 주소 지정
				.contentType(MediaType.APPLICATION_JSON) //형식 지정
				.body(requestBody) //본문 데이터
				.retrieve() //빌더 체인 끝 -> 실제 네트워크 호출 실행
				.body(JsonNode.class); //자바 객체 타입으로 변환

			//호출 실패 시 일단 숨김 처리
			if (response == null) {
				log.warn("Gemini API returned null response for abuse check.");
				return true;
			}

			return extractSafetyRating(response);

		} catch (Exception e) { //외부 서비스 오류 시 일단 숨김 처리
			log.error("Failed to check abuse from Gemini API for review: {}", reviewContent, e);
			return true;
		}
	}

	//리뷰 내용의 위험도가 임계치(SAFETY_THRESHOLD) 이상인지 확인
	private boolean extractSafetyRating(JsonNode response) {

		if (response.has("promptFeedback")) {
			JsonNode feedback = response.path("promptFeedback");
			String blockReason = feedback.path("blockReason").asText();
			if ("SAFETY".equalsIgnoreCase(blockReason)) {
				log.warn("Gemini blocked the prompt for safety reasons: {}", feedback.toPrettyString());
				return true; // 차단된 경우, 안전하게 true로 처리
			}
		}

		//1. candidates 배열 존재 여부 확인 - 없으면 생성 실패
		//candidates[0].safetyRatings 배열 = 안전 필터링 결과 배열
		JsonNode candidates = response.path("candidates");
		if (!candidates.isArray() || candidates.isEmpty()) {
			log.warn("Gemini response has no candidates. Full response: {}", response.toPrettyString());
			return true;
		}

		//2. safetyRatings 배열 접근 시도
		JsonNode safetyRatings = candidates.get(0).path("safetyRatings");

		if (!safetyRatings.isArray()) {
			log.warn("Gemini response: safetyRatings is not an array. Full response: {}", response.toPrettyString());
			return true;
		}

		for (JsonNode rating : safetyRatings) {
			String category = rating.path("category").asText(); //'HATE_SPEECH', 'HARASSMENT'
			String probability = rating.path("probability").asText(); // LOW, MEDIUM, HIGH, NEGLIGIBLE

			// 임계값을 초과하면 true 반환
			if (probability.equals(SAFETY_THRESHOLD) &&
				(category.contains("HATE_SPEECH") || category.contains("HARASSMENT"))) {
				log.warn("Review flagged: Category={}, Probability={}", category, probability);
				return true;
			}
		}

		return false;
	}

	private String buildRequestBodyForReview(String prompt) {
		return """
			{
			  "contents": [{
			    "parts": [{
			      "text": "%s"
			    }]
			  }],
			  "safetySettings": [
			    {
			      "category": "HARM_CATEGORY_HARASSMENT",
			      "threshold": "BLOCK_ONLY_HIGH"
			    },
			    {
			      "category": "HARM_CATEGORY_HATE_SPEECH",
			      "threshold": "BLOCK_ONLY_HIGH"
			    }
			  ]
			}
			""".formatted(prompt.replace("\"", "\\\""));
	}

}
