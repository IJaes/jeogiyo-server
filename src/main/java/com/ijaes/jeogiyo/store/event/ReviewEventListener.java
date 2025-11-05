package com.ijaes.jeogiyo.store.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.review.event.ReviewEvent;
import com.ijaes.jeogiyo.review.repository.ReviewRepositoryCustom;
import com.ijaes.jeogiyo.store.entity.Store;
import com.ijaes.jeogiyo.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewEventListener {

	private final StoreRepository storeRepository;
	private final ReviewRepositoryCustom reviewRepositoryCustom;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	public void handleReviewEvent(ReviewEvent event) {
		Store store = storeRepository.findById(event.getStoreId())
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

		Double averageRate = reviewRepositoryCustom.calculateAverageRateByStoreId(event.getStoreId());
		store.updateRate(averageRate != null ? averageRate : 0.0);
		storeRepository.save(store);
	}
}
