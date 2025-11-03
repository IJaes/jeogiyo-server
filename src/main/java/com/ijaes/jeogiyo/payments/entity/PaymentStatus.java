package com.ijaes.jeogiyo.payments.entity;

public enum PaymentStatus {
	REQUESTED, //결제요청
	SUCCESS, //결제성공
	CANCEL, // 결제취소
	FAIL,    // 결제실패, 거절
	REFUND, // 환불
	REFUND_FAIL, // 환불실패
	EXPIRED, //타임아웃
}
