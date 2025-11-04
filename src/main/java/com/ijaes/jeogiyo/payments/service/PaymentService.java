package com.ijaes.jeogiyo.payments.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ijaes.jeogiyo.common.exception.CustomException;
import com.ijaes.jeogiyo.common.exception.ErrorCode;
import com.ijaes.jeogiyo.orders.dto.request.OrderEvent;
import com.ijaes.jeogiyo.payments.entity.Payment;
import com.ijaes.jeogiyo.payments.entity.PaymentStatus;
import com.ijaes.jeogiyo.payments.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final ObjectMapper objectMapper;

	@Value("${toss.secret-key}")
	private String secretKey;

	@Async
	@TransactionalEventListener
	public void createPaymentKey(OrderEvent event) {
		try {
			// 결제키 발급
			String paymentKey = createPayment(event.getOrderId(), event.getAmount());

			//결제키 받은 후 결제요청 상태로 DB저장
			Payment payment = Payment.builder()
				.orderId(event.getOrderId())
				.paymentKey(paymentKey)
				.paymentAmount(event.getAmount())
				.status(PaymentStatus.REQUESTED)
				.build();
			paymentRepository.save(payment);

		} catch (Exception e) {
			throw new CustomException(ErrorCode.PAYMENT_KEY_GENERATION_FAILED);
		}
	}

	//결제키 발급 로직
	private String createPayment(UUID orderId, int amount) throws Exception {
		String url = "https://api.tosspayments.com/v1/payments";
		String auth = secretKey.trim() + ":";
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

		String bodyJson = String.format(
			"{\"method\":\"CARD\", \"amount\":%d, \"orderId\":\"%s\", \"orderName\":\"테스트 결제\", " +
				"\"successUrl\":\"http://localhost:8080/v1/payments/resp/success\", " +
				"\"failUrl\":\"http://localhost:8080/v1/payments/resp/fail\"}",
			amount, orderId
		);

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("Authorization", "Basic " + encodedAuth)
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(bodyJson))
			.build();

		HttpResponse<String> response = HttpClient.newHttpClient()
			.send(request, HttpResponse.BodyHandlers.ofString());
		JsonNode jsonNode = objectMapper.readTree(response.body());

		System.out.println("결제 UI : " + jsonNode.get("checkout").get("url").asText());

		if (response.statusCode() != 200) {
			throw new CustomException(ErrorCode.PAYMENT_KEY_GENERATION_FAILED);
		}

		return jsonNode.get("paymentKey").asText();
	}

	//결제 승인 처리
	@SuppressWarnings("checkstyle:WhitespaceAfter")
	public void confirmPayment(String paymentKey, UUID orderId, int amount) throws
		Exception {
		String url = "https://api.tosspayments.com/v1/payments/confirm";
		String auth = secretKey.trim() + ":";
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

		String bodyJson = objectMapper.writeValueAsString(Map.of(
			"paymentKey", paymentKey,
			"orderId", orderId.toString(),
			"amount", amount
		));

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("Authorization", "Basic " + encodedAuth)
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(bodyJson))
			.build();

		HttpResponse<String> response = HttpClient.newHttpClient()
			.send(request, HttpResponse.BodyHandlers.ofString());

		Payment payment = (Payment)paymentRepository.findByPaymentKey(paymentKey)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		JsonNode jsonNode = objectMapper.readTree(response.body());

		String bank = jsonNode.path("easyPay").path("provider").asText(null);
		String method = jsonNode.path("method").asText(null);

		String log = jsonNode.path("message").asText(null);
		String logMessage = (log != null) ? log : ErrorCode.PAYMENT_CONFIRMATION_FAILED.getMessage();

		try {
			if (response.statusCode() == 200 && "DONE".equals(jsonNode.path("status").asText())) {
				try {
					// 결제 승인 완료 시
					LocalDateTime approvedAt = OffsetDateTime.parse(jsonNode.get("approvedAt").asText(null))
						.toLocalDateTime();
					payment.updatePaymentApprove(approvedAt, bank, method);
					paymentRepository.save(payment);
				} catch (Exception dbEx) {
					// 결제는 완료되었으나 DB 저장 실패 시
					payment.updateLog("DB 저장 실패: " + dbEx.getMessage());
					throw new CustomException(ErrorCode.PAYMENT_DB_SAVE_FAILED);
				}

			} else {

				payment.updatePaymentFail(bank, method, logMessage);
				paymentRepository.save(payment);
			}
		} catch (Exception e) {
			payment.updatePaymentFail(bank, method, logMessage);
			paymentRepository.save(payment);
			throw e;
		}
	}
}

