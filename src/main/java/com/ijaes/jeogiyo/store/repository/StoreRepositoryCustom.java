package com.ijaes.jeogiyo.store.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;
import com.ijaes.jeogiyo.store.entity.Store;

public interface StoreRepositoryCustom {

	Optional<StoreDetailResponse> findStoreDetailById(UUID storeId);

	Optional<Store> findByOwnerId(UUID ownerId);

	boolean existsByOwnerId(UUID ownerId);

	Optional<Store> findByIdNotDeleted(UUID id);

	Page<Store> findAllNotDeleted(Pageable pageable);

	Page<Store> findAllIncludingDeleted(Pageable pageable);
}
