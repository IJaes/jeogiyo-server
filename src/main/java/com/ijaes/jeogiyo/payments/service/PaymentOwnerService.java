package com.ijaes.jeogiyo.payments.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.orders.dto.request.OrderOwnerCancelRequest;
import com.ijaes.jeogiyo.payments.entity.Payment;
import com.ijaes.jeogiyo.payments.repository.PaymentRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class PaymentOwnerService {

	private final PaymentRepository paymentRepository;

	@Value("${toss.secret-key}")
	private String secretKey;

	private final ObjectMapper objectMapper;

	// 결제취소처리
	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void cancelPayment(OrderOwnerCancelRequest orderCancelEvent) {

		getValidatedOwner();

		Payment payment = (Payment)paymentRepository.findByPaymentKey(orderCancelEvent.getPaymentKey())
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		try {
			String auth = secretKey.trim() + ":";
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(
					"https://api.tosspayments.com/v1/payments/" + orderCancelEvent.getPaymentKey() + "/cancel"))
				.header("Authorization", "Basic " + encodedAuth)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(
					"{\"cancelReason\":\"" + orderCancelEvent.getCanCelReason() + "\"}"))
				.build();

			HttpResponse<String> response = HttpClient.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

			JsonNode jsonNode = objectMapper.readTree(response.body());
			String log = jsonNode.path("message").asText(null);
			String logMessage = (log != null) ? log : ErrorCode.PAYMENT_CONFIRMATION_FAILED.getMessage();

			if (response.statusCode() == 200 && "CANCELED".equals(jsonNode.path("status").asText())) {
				payment.updateOwnerPaymentCancel();
			} else {
				payment.updateCancelPaymentFail(logMessage);
			}

			paymentRepository.save(payment);

		} catch (Exception e) {
			payment.updateCancelPaymentFail(e.getMessage());
			paymentRepository.save(payment);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	// 로그인, 권한 확인 메소드
	private void getValidatedOwner() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}

		User owner = (User)authentication.getPrincipal();

		if (owner.getRole() == null || !owner.getRole().equals(Role.OWNER)) {
			throw new CustomException(ErrorCode.OWNER_ROLE_REQUIRED);
		}
	}

}
