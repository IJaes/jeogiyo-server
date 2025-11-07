package com.ijaes.jeogiyo.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RestTemplateConfig {

	private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

	@Bean
	public ClientHttpRequestFactory clientHttpRequestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5000);
		factory.setReadTimeout(5000);

		return new BufferingClientHttpRequestFactory(factory);
	}

	@Bean
	public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

		// 요청 인터셉터 추가 - 실제 전송되는 URL 로깅
		restTemplate.getInterceptors().add((request, body, execution) -> {
			logger.info("실제 요청 Query: {}", request.getURI().getRawQuery());

			return execution.execute(request, body);
		});

		return restTemplate;
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}
}
