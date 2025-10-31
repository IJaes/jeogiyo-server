package com.ijaes.jeogiyo.store.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

	public Page<StoreResponse> getAllStores(int page, int size, String sortBy, String direction) {
		Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

		Page<Store> stores = storeRepository.findAll(pageable);

		return stores.map(store -> StoreResponse.builder()
			.id(store.getId())
			.businessNumber(store.getBusinessNumber())
			.name(store.getName())
			.address(store.getAddress())
			.description(store.getDescription())
			.category(store.getCategory().name())
			.rate(store.getRate())
			.ownerId(store.getOwnerId())
			.build());
	}

	public StoreDetailResponse getStoreDetail(UUID storeId) {
		return storeRepository.findStoreDetailById(storeId)
			.orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
	}
}
