package com.ijaes.jeogiyo.payments.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
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
import com.ijaes.jeogiyo.orders.entity.Order;
import com.ijaes.jeogiyo.orders.repository.OrderRepository;
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
	private final OrderRepository orderRepository;
	private final ObjectMapper objectMapper;
	private final TaskScheduler taskScheduler;

	// 결제시도 횟수
	private final int MAX_RETRIES = 2;
	// 20초마다 시도
	private final long RETRY_DELAY_MS = 5 * 1000;

	@Value("${toss.secret-key}")
	private String secretKey;

	@Async
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void createBaillingKey(OrderRequest event) throws Exception {

		// 로그인, 권한 확인
		getValidatedUser();
		UUID orderId = event.getOrderId();

		if (orderId == null) {
			throw new CustomException(ErrorCode.ORDER_NOT_FOUND);  // 바로 예외 던지기
		}

		Order order = orderRepository.findById(event.getOrderId())
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		int orderAmount = order.getTotalPrice();

		if (orderAmount != event.getAmount()) {
			throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
		}
		String customerKey = event.getUserId().toString();

		// 빌링 인증 authKey 발급
		String billingKey = requestBillingAuthKey(customerKey);

		Payment payment = Payment.builder()
			.orderId(event.getOrderId())
			.billingKey(billingKey)
			.paymentAmount(event.getAmount())
			.status(PaymentStatus.REQUESTED)
			.build();
		paymentRepository.save(payment);

		// 빌링키로 결제 요청
		requestBillingPayment(billingKey, event.getAmount(), event.getUserId(), event.getOrderId());

	}

	// billingKey 발급
	private String requestBillingAuthKey(String customerKey) throws IOException, InterruptedException {
		String url = "https://api.tosspayments.com/v1/billing/authorizations/card";
		String auth = secretKey.trim() + ":";
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

		// 테스트 카드 정보 (Toss 테스트용)
		Map<String, Object> body = Map.of(
			"customerKey", customerKey,
			"cardNumber", "5365105152833310",           // Toss 테스트 카드
			"cardExpirationYear", "28",                // YY 형식
			"cardExpirationMonth", "03",               // MM 형식
			"customerIdentityNumber", "990424"    // 생년월일 YYMMDD

		);

		String bodyJson = objectMapper.writeValueAsString(body);

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("Authorization", "Basic " + encodedAuth)
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(bodyJson))
			.build();

		HttpResponse<String> response = HttpClient.newHttpClient()
			.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new CustomException(ErrorCode.PAYMENT_KEY_GENERATION_FAILED);
		}

		JsonNode node = objectMapper.readTree(response.body());

		return node.get("billingKey").asText();
	}

	//  결제 요청
	public void requestBillingPayment(String billingKey, int amount, UUID userId, UUID orderId) throws Exception {
		Payment payment = (Payment)paymentRepository.findByBillingKey(billingKey)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		String url = "https://api.tosspayments.com/v1/billing/" + billingKey;
		String auth = secretKey.trim() + ":";
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

		String bodyJson = objectMapper.writeValueAsString(Map.of(
			"amount", amount,
			"orderId", orderId.toString(),
			"customerKey", userId.toString(),
			"orderName", "테스트주문"
		));

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("Authorization", "Basic " + encodedAuth)
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(bodyJson))
			.build();

		HttpResponse<String> response = HttpClient.newHttpClient()
			.send(request, HttpResponse.BodyHandlers.ofString());

		JsonNode jsonNode = objectMapper.readTree(response.body());

		String log = jsonNode.path("message").asText(null);
		String logMessage = (log != null) ? log : ErrorCode.PAYMENT_CONFIRMATION_FAILED.getMessage();
		String paymentKey = jsonNode.path("paymentKey").asText();

		try {
			if (response.statusCode() == 200 && "DONE".equals(jsonNode.path("status").asText())) {
				try {
					// 결제 성공 시 DB 저장
					payment.updatePaymentSuccess(paymentKey);
					paymentRepository.save(payment);

					Order order = orderRepository.findById(orderId)
						.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

					order.updateOrderStatus(paymentKey);
					orderRepository.save(order);

				} catch (Exception e) {
					// 결제는 완료되었으나 DB 저장 실패 시
					payment.updateLog("DB 저장 실패: " + e.getMessage());
					// processPaymentWithRetry(billingKey, amount, userId, orderId);
					throw new CustomException(ErrorCode.PAYMENT_DB_SAVE_FAILED);
				}

			} else {
				// String failMsg = jsonNode.path("message").asText(null);
				payment.updateApprovePaymentFail(log, paymentKey);
				paymentRepository.save(payment);
				processPaymentWithRetry(billingKey, amount, userId, orderId, log);

				throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
			}
		} catch (Exception e) {
			payment.updateApprovePaymentFail(logMessage, paymentKey);
			paymentRepository.save(payment);
			// processPaymentWithRetry(billingKey, amount, userId, orderId, logMessage);

			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}

	public void processPaymentWithRetry(String billingKey, int amount, UUID userId, UUID orderId, String log) throws
		Exception {
		Runnable retryTask = new Runnable() {
			@Override
			public void run() {
				try {
					Payment payment = (Payment)paymentRepository.findByOrderId(orderId)
						.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

					// 이미 최대 재시도 횟수를 초과했는지 확인
					if (payment.getRetryCount() >= MAX_RETRIES) {
						System.out.println("최대 재시도 횟수 초과, 결제 실패");
						return;
					}

					// 결제 요청 시도
					requestBillingPayment(billingKey, amount, userId, orderId);

				} catch (Exception e) {
					int currentRetry = 0;

					// 재시도 로직 처리
					if (currentRetry < MAX_RETRIES) {

						Payment payment = (Payment)paymentRepository.findByOrderId(orderId)
							.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

						payment.increaseRetryCount();
						paymentRepository.saveAndFlush(payment);

						// 1분 이내에 재시도하려면
						taskScheduler.schedule(this, new java.util.Date(System.currentTimeMillis() + RETRY_DELAY_MS));
					} else {
						// 최대 재시도 횟수에 도달한 경우
						Payment payment = (Payment)paymentRepository.findByOrderId(orderId)
							.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
						payment.updateCancelPaymentFail(log);
						paymentRepository.saveAndFlush(payment);
					}
				}
			}
		};

		taskScheduler.schedule(retryTask, new java.util.Date(System.currentTimeMillis() + RETRY_DELAY_MS));
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
					payment.updateCancelPaymentFail(logMessage);
				}

				paymentRepository.save(payment);

			}
		} catch (Exception e) {
			payment.updateCancelPaymentFail(e.getMessage());
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

