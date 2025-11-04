package com.ijaes.jeogiyo.menu.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.menu.dto.response.MenuUserResponse;
import com.ijaes.jeogiyo.menu.entity.Menu;
import com.ijaes.jeogiyo.menu.repository.MenuRepository;
import com.ijaes.jeogiyo.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuUserService {

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;

	@Transactional(readOnly = true)
	public List<MenuUserResponse> getMenusByStoreId(UUID storeId) {
		storeRepository.findByIdNotDeleted(storeId)
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

		List<Menu> menus = menuRepository.findAllNotDeleted(storeId);
		return menus.stream().map(this::toMenuUserResponse).toList();
	}

	private MenuUserResponse toMenuUserResponse(Menu menu) {
		return MenuUserResponse.builder()
			.id(menu.getId())
			.storeId(menu.getStore().getId())
			.name(menu.getName())
			.description(menu.getDescription())
			.price(menu.getPrice())
			.build();
	}
}
