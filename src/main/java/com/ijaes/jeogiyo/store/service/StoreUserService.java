package com.ijaes.jeogiyo.store.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreUserService {

	private final StoreRepository storeRepository;

	@Transactional(readOnly = true)
	public Page<StoreResponse> getAllStores(int page, int size, String sortBy, String direction) {
		Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

		Page<Store> stores = storeRepository.findAllNotDeleted(pageable);

		return stores.map(StoreResponse::fromEntity);
	}

	@Transactional(readOnly = true)
	public StoreDetailResponse getStoreDetail(UUID storeId) {
		return storeRepository.findStoreDetailById(storeId)
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
	}
}
