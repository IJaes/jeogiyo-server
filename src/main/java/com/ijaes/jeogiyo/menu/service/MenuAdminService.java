package com.ijaes.jeogiyo.menu.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.gemini.service.GeminiService;
import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.request.UpdateMenuRequest;
import com.ijaes.jeogiyo.menu.dto.response.MenuDetailResponse;
import com.ijaes.jeogiyo.menu.entity.Menu;
import com.ijaes.jeogiyo.menu.repository.MenuRepository;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuAdminService {

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;
	private final GeminiService geminiService;

	@Transactional
	public MenuDetailResponse createMenu(UUID storeId, @Valid CreateMenuRequest request) {
		Store store = storeRepository.findById(storeId)
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

		return MenuDetailResponse.from(savedMenu);
	}

	@Transactional(readOnly = true)
	public Page<MenuDetailResponse> getAllMenus(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Menu> menus = menuRepository.findAllIncludingDeleted(pageable);

		return menus.map(MenuDetailResponse::from);
	}

	@Transactional(readOnly = true)
	public MenuDetailResponse getMenu(UUID menuId) {
		Menu menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
		return MenuDetailResponse.from(menu);
	}

	@Transactional
	public void deleteMenu(UUID menuId) {
		Menu menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

		if (menu.isDeleted()) {
			throw new CustomException(ErrorCode.MENU_ALREADY_DELETED);
		}

		menu.softDelete();
	}

	@Transactional
	public MenuDetailResponse updateMenu(UUID menuId, UpdateMenuRequest request) {
		Menu menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

		menu.update(request.getName(), request.getDescription(), request.getPrice());

		return MenuDetailResponse.from(menu);
	}
}
