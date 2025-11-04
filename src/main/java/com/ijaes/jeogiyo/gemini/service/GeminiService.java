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
			String prompt = "너는 식당의 특정 메뉴에 대한 설명을 작성하는 역할이야. 손님이 메뉴를 고르고 싶은 마음이 들게끔 메뉴의 매력이 잘 드러나게 작성해주면 돼." + menuName + "라는 메뉴에 대한 간단한 설명을 한 문장으로 작성해줘.";
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
}
