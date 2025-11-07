package com.ijaes.jeogiyo.orders.entity;

public enum RejectReasonCode {
	OUT_OF_STOCK,   // 재료소진
	CLOSED_EARLY,   // 조기 영업 종료
	STORE_ISSUE,    // 가게 사정으로 인한 취소
	DELIVERY_ISSUE,    // 배달 사정으로 인한 취소
	WEATHER_ISSUE,    // 기상 악화로 인한 취소
}
