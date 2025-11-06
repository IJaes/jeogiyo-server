package com.ijaes.jeogiyo.store.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.store.dto.response.StoreDetailResponse;
import com.ijaes.jeogiyo.store.dto.response.StoreResponse;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreUserService {

	private final StoreRepository storeRepository;

	@Transactional(readOnly = true)
	public Page<StoreResponse> getAllStores(int page, int size, String sortBy, String direction, Authentication authentication) {
		User user = (User)authentication.getPrincipal();
		Double userLatitude = user.getLatitude();
		Double userLongitude = user.getLongitude();

		Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
		Page<Store> stores = storeRepository.findAllNotDeleted(pageable);

		List<StoreResponse> storesWithDistance = stores.stream()
			.map(store -> {
				double distance = calculateDistance(userLatitude, userLongitude, store.getLatitude(), store.getLongitude());
				return StoreResponse.fromEntity(store, distance);
			})
			.collect(Collectors.toList());

		if ("distance".equalsIgnoreCase(sortBy)) {
			storesWithDistance.sort(Comparator.comparingDouble(StoreResponse::getDistance));
			if ("DESC".equalsIgnoreCase(direction)) {
				storesWithDistance.sort(Comparator.comparingDouble(StoreResponse::getDistance).reversed());
			}
		} else {
			Pageable sortedPageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
			Page<Store> sortedStores = storeRepository.findAllNotDeleted(sortedPageable);

			storesWithDistance = sortedStores.stream()
				.map(store -> {
					double distance = calculateDistance(userLatitude, userLongitude, store.getLatitude(),
						store.getLongitude());
					return StoreResponse.fromEntity(store, distance);
				})
				.collect(Collectors.toList());
		}



		return new PageImpl<>(storesWithDistance, pageable, stores.getTotalElements());
	}

	@Transactional(readOnly = true)
	public StoreDetailResponse getStoreDetail(UUID storeId) {
		return storeRepository.findStoreDetailById(storeId)
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
	}

	private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
		double earthRadiusKm = 6371.0;

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
				Math.sin(dLon / 2) * Math.sin(dLon / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return earthRadiusKm * c;
	}
}
