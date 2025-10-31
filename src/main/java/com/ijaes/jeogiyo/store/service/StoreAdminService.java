package com.ijaes.jeogiyo.store.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.store.dto.request.UpdateStoreRequest;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.entity.Category;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreAdminService {

	private final StoreRepository storeRepository;

	public StoreResponse updateStore(UUID storeId, UpdateStoreRequest request) {
		Store store = storeRepository.findById(storeId)
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
		return StoreResponse.fromEntity(store);
	}
}

