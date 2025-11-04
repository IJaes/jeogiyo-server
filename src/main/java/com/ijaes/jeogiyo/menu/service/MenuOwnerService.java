package com.ijaes.jeogiyo.menu.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.gemini.service.GeminiService;
import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuResponse;
import com.ijaes.jeogiyo.menu.entity.Menu;
import com.ijaes.jeogiyo.menu.repository.MenuRepository;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuOwnerService {

	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;
	private final GeminiService geminiService;

	@Transactional
	public MenuResponse createMenu(CreateMenuRequest request, Authentication authentication) {
		User owner = (User)authentication.getPrincipal();
		Store store = storeRepository.findByOwnerId(owner.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

		String description = request.getDescription();
		if (request.isAiDescription() && description == null) {
			description = geminiService.generateMenuDescription(request.getName());
		}

		Menu menu = Menu.builder()
			.store(store)
			.name(request.getName())
			.description(description)
			.price(request.getPrice())
			.build();

		Menu savedMenu = menuRepository.save(menu);

		return toMenuResponse(savedMenu);
	}

	private MenuResponse toMenuResponse(Menu menu) {
		return MenuResponse.builder()
			.id(menu.getId())
			.storeId(menu.getStore().getId())
			.name(menu.getName())
			.description(menu.getDescription())
			.price(menu.getPrice())
			.createdAt(menu.getCreatedAt())
			.updatedAt(menu.getUpdatedAt())
			.build();
	}
}
