package com.ijaes.jeogiyo.store.repository;

import java.util.Optional;
import java.util.UUID;

import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;

public interface StoreRepositoryCustom {

	Optional<StoreDetailResponse> findStoreDetailById(UUID storeId);
}
