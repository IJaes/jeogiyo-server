package com.ijaes.jeogiyo.review.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewEvent {

	//매장 식별자
	UUID storeId;

	//재계산된 평점 평균
	Double averageRate;
}