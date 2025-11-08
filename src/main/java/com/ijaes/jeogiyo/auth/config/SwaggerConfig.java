package com.ijaes.jeogiyo.auth.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
@Import(SpringDocConfiguration.class)
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(apiInfo())
			.components(new Components()
				.addSecuritySchemes("bearer-jwt",
					new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("JWT 토큰을 입력하세요")));
	}

	private Info apiInfo() {
		return new Info()
			.title("Jeogiyo API")
			.description("주문 관리 플랫폼 저기요 API 문서")
			.version("1.0.0")
			.contact(new Contact());
	}

	private static final List<String> TAG_ORDER = List.of(
		"인증",
		"사용자",
		"사장님",
		"관리자",
		"리뷰 관련 관리자",
		"사용자 결제",
		"주문",
		"매장"
	);

	// ✅ 태그 정렬을 실제로 적용하는 커스터마이저
	@Bean
	public OpenApiCustomizer sortTagsCustomiser() {
		return openAPI -> {
			if (openAPI.getTags() == null)
				return;

			// 현재 태그를 이름 -> Tag 맵으로
			Map<String, Tag> byName = openAPI.getTags().stream()
				.collect(Collectors.toMap(Tag::getName, t -> t, (a, b) -> a, LinkedHashMap::new));

			// 1) TAG_ORDER 순서대로 배치
			List<Tag> ordered = new ArrayList<>();
			for (String name : TAG_ORDER) {
				Tag t = byName.remove(name);
				if (t != null)
					ordered.add(t);
			}
			// 2) 정의에 없는 나머지는 이름순으로 뒤에
			List<Tag> rest = new ArrayList<>(byName.values());
			rest.sort(Comparator.comparing(Tag::getName));
			ordered.addAll(rest);

			openAPI.setTags(ordered);
		};
	}
}

