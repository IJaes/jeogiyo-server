package com.ijaes.jeogiyo.payments.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

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
import com.ijaes.jeogiyo.orders.dto.request.OrderRequest;
import com.ijaes.jeogiyo.orders.dto.request.OrderUserCancelRequest;
import com.ijaes.jeogiyo.payments.entity.Payment;
import com.ijaes.jeogiyo.payments.entity.PaymentStatus;
import com.ijaes.jeogiyo.payments.repository.PaymentRepository;
import com.ijaes.jeogiyo.user.entity.Role;
import com.ijaes.jeogiyo.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentUserService {

	private final PaymentRepository paymentRepository;
	private final ObjectMapper objectMapper;

	@Value("${toss.secret-key}")
	private String secretKey;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void createPaymentKey(OrderRequest event) {

		// 로그인, 권한 확인
		getValidatedUser();

		// 오더 관련 기능과 합쳐서 완료 시 주석 해제
		// Order order = orderRepository.findById(event.getOrderId())
		// 	.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		// int orderAmount = order.getTotalPrice();

		// 테스트
		int orderAmount = 1;

		if (orderAmount != event.getAmount()) {
			throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}

		try {
			// 결제키 발급
			String paymentKey = createPayment(event.getOrderId(), orderAmount);

			//결제키 받은 후 결제요청 상태로 DB저장
			Payment payment = Payment.builder()
				.orderId(event.getOrderId())
				.paymentKey(paymentKey)
				.paymentAmount(orderAmount)
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
	public void confirmPayment(String paymentKey, UUID orderId, int amount) throws Exception {

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
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	// 결제취소처리
	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void cancelPayment(OrderUserCancelRequest orderCancelEvent) throws IOException, InterruptedException {

		getValidatedUser();

		Payment payment = (Payment)paymentRepository.findByPaymentKey(orderCancelEvent.getPaymentKey())
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		try {
			LocalDateTime approvedAt = payment.getApprovedAt();
			LocalDateTime now = LocalDateTime.now();
			Duration duration = Duration.between(approvedAt, now);
			long diff = duration.toMinutes();

			// 사용자가 5분 이후 취소할 경우
			if (diff > 5) {
				throw new CustomException(ErrorCode.PAYMENT_CALNCEL_EXPIRE);
			} else {
				String auth = secretKey.trim() + ":";
				String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

				HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(
						"https://api.tosspayments.com/v1/payments/" + orderCancelEvent.getPaymentKey() + "/cancel"))
					.header("Authorization", "Basic " + encodedAuth)
					.header("Content-Type", "application/json")
					.method("POST", HttpRequest.BodyPublishers.ofString(
						"{\"cancelReason\":\"" + orderCancelEvent.getCanCelReason() + "\"}"))
					.build();

				HttpResponse<String> response = HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString());

				JsonNode jsonNode = objectMapper.readTree(response.body());
				String log = jsonNode.path("message").asText(null);
				String logMessage = (log != null) ? log : ErrorCode.PAYMENT_CONFIRMATION_FAILED.getMessage();

				if (response.statusCode() == 200 && "CANCELED".equals(jsonNode.path("status").asText())) {
					payment.updateUserPaymentCancel();
				} else {
					payment.updatePaymentFail(logMessage);
				}

				paymentRepository.save(payment);

			}
		} catch (Exception e) {
			payment.updatePaymentFail(e.getMessage());
			paymentRepository.save(payment);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}

	// 로그인, 권한 확인 메소드
	private void getValidatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}

		User user = (User)authentication.getPrincipal();

		if (user.getRole() == null || !user.getRole().equals(Role.USER)) {
			throw new CustomException(ErrorCode.USER_ROLE_REQUIRED);
		}
	}
}

