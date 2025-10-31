package com.ijaes.jeogiyo.store.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.store.dto.request.CreateStoreRequest;
import com.ijaes.jeogiyo.store.dto.request.UpdateStoreRequest;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreOwnerService {

	private final StoreRepository storeRepository;

	@Transactional
	public StoreResponse createStore(Authentication authentication, CreateStoreRequest request) {
		User owner = getOwnerFromAuthentication(authentication);
		validateOwnerRole(owner);

		if (storeRepository.existsByOwnerId(owner.getId())) {
			throw new CustomException(ErrorCode.DUPLICATE_STORE);
		}

		try {
			Category category = Category.valueOf(request.getCategory().toUpperCase());

			Store store = Store.builder()
				.businessNumber(request.getBusinessNumber())
				.name(request.getName())
				.address(request.getAddress())
				.description(request.getDescription())
				.category(category)
				.rate(0.0)
				.ownerId(owner.getId())
				.build();

			Store savedStore = storeRepository.save(store);

			return toStoreResponse(savedStore);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.INVALID_CATEGORY);
		}
	}

	@Transactional(readOnly = true)
	public StoreResponse myStore(Authentication authentication) {
		User owner = getOwnerFromAuthentication(authentication);
		validateOwnerRole(owner);

		Store myStore = storeRepository.findByOwnerId(owner.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

		return toStoreResponse(myStore);
	}

	@Transactional
	public StoreResponse updateStore(Authentication authentication, UpdateStoreRequest request) {
		User owner = getOwnerFromAuthentication(authentication);
		validateOwnerRole(owner);

		Store store = storeRepository.findByOwnerId(owner.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

		if (request.getName() != null && !request.getName().isBlank()) {
			store.updateName(request.getName());
		}

		if (request.getAddress() != null && !request.getAddress().isBlank()) {
			store.updateAddress(request.getAddress());
		}

		if (request.getDescription() != null && !request.getDescription().isBlank()) {
			store.updateDescription(request.getDescription());
		}

		if (request.getCategory() != null && !request.getCategory().isBlank()) {
			try {
				Category category = Category.valueOf(request.getCategory().toUpperCase());
				store.updateCategory(category);
			} catch (IllegalArgumentException e) {
				throw new CustomException(ErrorCode.INVALID_CATEGORY);
			}
		}

		storeRepository.save(store);
		return toStoreResponse(store);
	}

	private User getOwnerFromAuthentication(Authentication authentication) {
		return (User) authentication.getPrincipal();
	}

	private void validateOwnerRole(User user) {
		if (!user.getRole().equals(Role.OWNER)) {
			throw new CustomException(ErrorCode.OWNER_ROLE_REQUIRED);
		}
	}

	private StoreResponse toStoreResponse(Store store) {
		return StoreResponse.fromEntity(store);
	}
}