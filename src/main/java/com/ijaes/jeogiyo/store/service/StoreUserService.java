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
	public Page<StoreResponse> getAllStores(int page, int size, String sortBy, Authentication authentication) {
		User user = (User)authentication.getPrincipal();
		Double userLatitude = user.getLatitude();
		Double userLongitude = user.getLongitude();

		if (userLatitude == null || userLongitude == null) {
			throw new CustomException(ErrorCode.USER_COORDINATES_NOT_FOUND);
		}

		if ("distance".equalsIgnoreCase(sortBy)) {
			return getStoresSortedByDistance(page, size, userLatitude, userLongitude);
		} else if ("rate".equalsIgnoreCase(sortBy)) {
			return getStoresSortedByRate(page, size, userLatitude, userLongitude);
		} else {
			return getStoresSortedByDistance(page, size, userLatitude, userLongitude);
		}
	}

	@Transactional(readOnly = true)
	public StoreDetailResponse getStoreDetail(UUID storeId) {
		return storeRepository.findStoreDetailById(storeId)
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
	}

	private Page<StoreResponse> getStoresSortedByDistance(int page, int size, Double userLat, Double userLon) {
		Pageable largePageable = PageRequest.of(0, Integer.MAX_VALUE);
		Page<Store> allStores = storeRepository.findAllNotDeleted(largePageable);

		List<StoreResponse> sortedStores = allStores.getContent().stream()
			.filter(store -> store.getLatitude() != null && store.getLongitude() != null)
			.map(store -> {
				double distance = calculateDistance(userLat, userLon, store.getLatitude(), store.getLongitude());
				return StoreResponse.fromEntity(store, distance);
			})
			.sorted(Comparator.comparingDouble(StoreResponse::getDistance))
			.collect(Collectors.toList());

		int start = page * size;
		int end = Math.min(start + size, sortedStores.size());
		List<StoreResponse> pageResults = start <= sortedStores.size() ? sortedStores.subList(start, end) : List.of();

		return new PageImpl<>(pageResults, PageRequest.of(page, size), sortedStores.size());
	}

	private Page<StoreResponse> getStoresSortedByRate(int page, int size, Double userLat, Double userLon) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rate"));
		Page<Store> stores = storeRepository.findAllNotDeleted(pageable);

		List<StoreResponse> storesWithDistance = stores.getContent().stream()
			.map(store -> {
				double distance = (store.getLatitude() != null && store.getLongitude() != null)
					? calculateDistance(userLat, userLon, store.getLatitude(), store.getLongitude())
					: Double.MAX_VALUE;
				return StoreResponse.fromEntity(store, distance);
			})
			.collect(Collectors.toList());

		return new PageImpl<>(storesWithDistance, pageable, stores.getTotalElements());
	}

	private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
		if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
			return Double.MAX_VALUE;
		}

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
