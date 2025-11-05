package com.ijaes.jeogiyo.menu.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
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
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuOwnerService {

	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;
	private final GeminiService geminiService;

	@Transactional
	public MenuDetailResponse createMenu(CreateMenuRequest request, Authentication authentication) {
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

	@Transactional(readOnly = true)
	public List<MenuDetailResponse> getMyMenus(Authentication authentication) {
		User owner = (User)authentication.getPrincipal();

		List<Menu> menus = menuRepository.findByOwnerId(owner.getId());

		return menus.stream().map(this::toMenuResponse).toList();
	}

	@Transactional(readOnly = true)
	public MenuDetailResponse getMyMenu(UUID menuId, Authentication authentication) {
		User owner = (User)authentication.getPrincipal();

		Menu menu = menuRepository.findByIdAndOwnerId(menuId, owner.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

		return toMenuResponse(menu);
	}

	@Transactional
	public MenuDetailResponse updateMenu(UUID menuId, UpdateMenuRequest request, Authentication authentication) {
		User owner = (User)authentication.getPrincipal();

		Menu menu = menuRepository.findByIdAndOwnerId(menuId, owner.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

		menu.update(request.getName(), request.getDescription(), request.getPrice());

		return toMenuResponse(menu);
	}

	@Transactional
	public void deleteMenu(UUID menuId, Authentication authentication) {
		User owner = (User)authentication.getPrincipal();

		Menu menu = menuRepository.findByIdAndOwnerId(menuId, owner.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

		if (menu.isDeleted()) {
			throw new CustomException(ErrorCode.MENU_ALREADY_DELETED);
		}

		menu.softDelete();
	}

	private MenuDetailResponse toMenuResponse(Menu menu) {
		return MenuDetailResponse.builder()
			.id(menu.getId())
			.storeId(menu.getStore().getId())
			.name(menu.getName())
			.description(menu.getDescription())
			.price(menu.getPrice())
			.createdAt(menu.getCreatedAt())
			.updatedAt(menu.getUpdatedAt())
			.deletedAt(menu.getDeletedAt())
			.build();
	}
}
