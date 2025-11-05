package com.ijaes.jeogiyo.review.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewEvent {

	//매장 식별자
	UUID storeId;

	//이벤트 발생 원인(생성, 수정, 삭제)
	EventType type;

	//변경된 평점 값
	Integer newRating;

	//이전 평점 값
	Integer oldRating;
}
