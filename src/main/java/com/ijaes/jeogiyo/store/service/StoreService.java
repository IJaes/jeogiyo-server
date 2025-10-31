package com.ijaes.jeogiyo.store.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.store.dto.CreateStoreRequest;
import com.ijaes.jeogiyo.store.dto.StoreResponse;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;

	public StoreResponse createStore(Authentication authentication, CreateStoreRequest request) {
		User owner = (User) authentication.getPrincipal();

		if (!owner.getRole().equals(Role.OWNER)) {
			throw new CustomException(ErrorCode.OWNER_ROLE_REQUIRED);
		}

		if (storeRepository.existsByOwnerId(owner.getId())) {
			throw new CustomException(ErrorCode.DUPLICATE_STORE);
		}

		try {
			Category category = Category.valueOf(request.getCategory());

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

			return StoreResponse.builder()
				.id(savedStore.getId())
				.businessNumber(savedStore.getBusinessNumber())
				.name(savedStore.getName())
				.address(savedStore.getAddress())
				.description(savedStore.getDescription())
				.category(savedStore.getCategory().name())
				.rate(savedStore.getRate())
				.ownerId(owner.getId())
				.build();
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
	}

	public StoreResponse myStore(Authentication authentication) {
		User user = (User)authentication.getPrincipal();

		if (!user.getRole().equals(Role.OWNER)) {
			throw new CustomException(ErrorCode.OWNER_ROLE_REQUIRED);
		}

		try {
			Store myStore = storeRepository.findByOwnerId(user.getId());

			return StoreResponse.builder()
				.id(myStore.getId())
				.businessNumber(myStore.getBusinessNumber())
				.name(myStore.getName())
				.address(myStore.getAddress())
				.description(myStore.getDescription())
				.category(myStore.getCategory().name())
				.rate(myStore.getRate())
				.ownerId(user.getId())
				.build();

		} catch (IllegalStateException e) {
			throw new CustomException(ErrorCode.INVALID_ADDRESS);
		}
	}
}
