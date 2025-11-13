package com.ijaes.jeogiyo.review.service;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.dto.response.ReviewResponse;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewOwnerService {

	private final ReviewService reviewService;
	private final StoreRepository storeRepository;

	@Transactional(readOnly = true)
	public Page<ReviewResponse> getMyStoreReviews(Authentication authentication, int page, int size, String sortType) {
		User owner = (User)authentication.getPrincipal();
		Store store = storeRepository.findByOwnerId(owner.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
		return reviewService.getStoreReviews(store.getId(), page, size, sortType);
	}
}
