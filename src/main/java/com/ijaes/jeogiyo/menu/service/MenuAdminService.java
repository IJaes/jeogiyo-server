package com.ijaes.jeogiyo.menu.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.menu.dto.request.CreateMenuRequest;
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

	@Transactional
	public MenuDetailResponse createMenu(UUID storeId, @Valid CreateMenuRequest request) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

		Menu menu = Menu.builder()
			.store(store)
			.name(request.getName())
			.description(request.getDescription())
			.price(request.getPrice())
			.build();

		Menu savedMenu = menuRepository.save(menu);

		return toMenuResponse(savedMenu);
	}

	@Transactional(readOnly = true)
	public Page<MenuDetailResponse> getAllMenus(int page, int size, String sortBy, String direction) {
		Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
		Page<Menu> menus = menuRepository.findAllIncludingDeleted(pageable);

		return menus.map(MenuDetailResponse::fromEntity);
	}

	@Transactional(readOnly = true)
	public MenuDetailResponse getMenu(UUID menuId) {
		Menu menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
		return toMenuResponse(menu);
	}

	@Transactional
	public void deleteMenu(UUID menuId) {
		Menu menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

		if (menu.isDeleted()) {
			throw new CustomException(ErrorCode.MENU_ALREADY_DELETED);
		}

		menu.softDelete();
		menuRepository.save(menu);
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
