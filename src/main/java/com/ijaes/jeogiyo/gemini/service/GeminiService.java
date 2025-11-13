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
	private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

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
			String prompt = menuName + " 메뉴에 대한 설명을 다음 형식으로 작성해줘.\n\n"
				+ "- 반드시 한 문장으로만 작성\n"
				+ "- 번호, 제목, 여러 제안 등 불필요한 내용 제외\n"
				+ "- 설명 문장 하나만 반환\n"
				+ "- 손님이 주문하고 싶게 메뉴의 매력 표현\n\n"
				+ "좋은 예: '진한 육수에 쫄깃한 순대가 어우러진 따뜻하고 든든한 한 그릇'\n"
				+ "나쁜 예: '여기 몇 가지 제안이 있습니다: 1. ..., 2. ...'\n\n"
				+ "설명:";
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
				너는 리뷰 내용의 언어 수위를 평가하는 필터 역할을 해.
				리뷰에 '심한 욕설'이나 '인신공격적인 비난'이 포함된 경우만 true,
				그 외의 가벼운 불만이나 부정적인 표현은 false로 판단해.
				
				출력 형식은 아래 중 하나만 답변해:
				- "true": 심한 욕설 또는 인신공격 표현이 포함됨
				- "false": 일반적인 불만, 비속어는 있으나 심하지 않음
				리뷰 내용: "%s"
				""".formatted(reviewContent);

			//프롬프트를 JSON 구조로 포맷팅
			String requestBody = buildRequestBody(prompt);
			String url = GEMINI_API_URL.replace("{model}", GEMINI_MODEL) + "?key=" + apiKey;

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

	private boolean extractSafetyRating(JsonNode response) {

		if (response.has("promptFeedback")) {
			JsonNode feedback = response.path("promptFeedback");
			String blockReason = feedback.path("blockReason").asText();
			// 프롬프트 자체가 차단된 경우, 안전하게 true로 처리
			if ("SAFETY".equalsIgnoreCase(blockReason)) {
				log.warn("Gemini blocked the prompt for safety reasons: {}", feedback.toPrettyString());
				return true;
			}
		}

		//candidates 배열 존재 여부 확인 - 없으면 생성 실패
		JsonNode candidates = response.path("candidates");
		if (!candidates.isArray() || candidates.isEmpty()) {
			log.warn("Gemini response has no candidates. Full response: {}", response.toPrettyString());
			return true;
		}

		// 텍스트 응답 추출
		String answer = candidates.get(0)
			.path("content")
			.path("parts")
			.get(0)
			.path("text")
			.asText()
			.trim()
			.toLowerCase();

		log.info("Gemini abuse check answer: {}", answer);

		// 텍스트 내용 기반으로 판정
		if (answer.contains("false")) {
			return false;
		}
		if (answer.contains("true")) {
			return true;
		}

		//모호한 경우 보수적으로 true
		log.warn("Unclear abuse check result, treating as unsafe. Full answer: {}", answer);
		return true;
	}
}
